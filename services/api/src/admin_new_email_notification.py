import logging
import common.pgquery as pgquery
import sqlalchemy
import os

from common.auth_tools import (
    ENVIRON_USER_KEY,
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    check_user_with_org,
    get_qualified_org_list,
)


def query_and_return_list(query):
    data = pgquery.query_db(query)
    return_list = []
    for row in data:
        return_list.append(" ".join(row))
    return return_list


def get_allowed_types(user_email, user: EnvironWithOrg):
    if user_email != user.user_info.email:
        qualified_orgs = get_qualified_org_list(user, ORG_ROLE_LITERAL.ADMIN)
        if not user.user_info.super_user or not check_user_with_org(
            user_email, qualified_orgs
        ):
            return (
                f"User does not have access to view notifications for user {user_email}",
                403,
            )

    allowed = {}

    email_types_query = (
        "SELECT email_type FROM public.email_type WHERE email_type_id NOT IN ("
        "SELECT email_type_id FROM public.user_email_notification WHERE user_id ="
        f"(SELECT user_id FROM public.users WHERE email = '{user_email}'))"
    )

    allowed["email_types"] = query_and_return_list(email_types_query)

    return allowed


def check_safe_input(notification_spec):
    special_characters = "!\"#$%&'()*+,./:;<=>?@[\\]^`{|}~"
    special_characters_email = "!\"#$%&'()*+,/:;<=>?[\\]^`{|}~"
    # Check all string based fields for special characters
    if (
        any(c in special_characters_email for c in notification_spec["email"])
        or "--" in notification_spec["email"]
    ):
        return False
    if (
        any(c in special_characters for c in notification_spec["email_type"])
        or "--" in notification_spec["email_type"]
    ):
        return False
    return True


def add_notification(notification_spec, user: EnvironWithOrg):
    email = notification_spec["email"]
    if email != user.user_info.email:
        qualified_orgs = get_qualified_org_list(user, ORG_ROLE_LITERAL.ADMIN)
        if not user.user_info.super_user or not check_user_with_org(
            email, qualified_orgs
        ):
            return (
                f"User does not have access to view notifications for user {email}",
                403,
            )

    # Check for special characters for potential SQL injection
    if not check_safe_input(notification_spec):
        return {
            "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        }, 500
    try:
        notification_insert_query = (
            "INSERT into public.user_email_notification(user_id, email_type_id) VALUES ("
            f"(SELECT user_id FROM public.users WHERE email='{email}'), "
            f"(SELECT email_type_id FROM public.email_type WHERE email_type='{notification_spec['email_type']}'))"
        )
        pgquery.write_db(notification_insert_query)

    except sqlalchemy.exc.IntegrityError as e:
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        logging.error(f"Exception encountered: {failed_value}")
        return {"message": failed_value}, 500
    except Exception as e:
        logging.error(f"Exception encountered: {e}")
        return {"message": "Encountered unknown issue"}, 500

    return {"message": "New email notification successfully added"}, 200


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import urllib.request


class AdminNewNotificationSchema(Schema):
    email = fields.Str(required=True)
    email_type = fields.Str(required=True)


class AdminGetNotificationSchema(Schema):
    user_email = fields.Str(required=True)


class AdminNewNotification(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET,POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def get(self):
        logging.debug("AdminNewNotification GET requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]

        # Check for main body values
        schema = AdminGetNotificationSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))
        user_email = urllib.request.unquote(request.args["user_email"])
        return (get_allowed_types(user_email, user), 200, self.headers)

    def post(self):
        logging.debug("AdminNewNotification POST requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]

        # Check for main body values
        schema = AdminNewNotificationSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        data, code = add_notification(request.json, user)
        return (data, code, self.headers)
