from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import urllib.request
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import os
from werkzeug.exceptions import InternalServerError, BadRequest

from common.auth_tools import (
    ORG_ROLE_LITERAL,
    RESOURCE_TYPE,
    require_permission,
)


def get_notification_data(user_email):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT u.email, u.first_name, u.last_name, e.email_type "
        "FROM public.user_email_notification "
        "JOIN public.users AS u ON u.user_id = user_email_notification.user_id "
        "JOIN public.email_type AS e ON e.email_type_id = user_email_notification.email_type_id "
        "WHERE user_email_notification.user_id IN (SELECT user_id FROM public.users WHERE email = :user_email)"
        ") as row"
    )
    params = {"user_email": user_email}
    data = pgquery.query_db(query, params=params)

    notification_dict = {}
    for row in data:
        row = dict(row[0])
        notification_dict[row["email_type"]] = {
            "email": row["email"],
            "first_name": row["first_name"],
            "last_name": row["last_name"],
            "email_type": row["email_type"],
        }

    notification_list = list(notification_dict.values())
    # If list is empty and a single user was requested, return empty object
    if len(notification_list) == 0:
        return []
    else:
        return notification_list


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.USER,
)
def get_modify_notification_data_authorized(user_email):
    return {"notification_data": get_notification_data(user_email)}


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
        any(c in special_characters for c in notification_spec["old_email_type"])
        or "--" in notification_spec["old_email_type"]
    ):
        return False
    if (
        any(c in special_characters for c in notification_spec["new_email_type"])
        or "--" in notification_spec["new_email_type"]
    ):
        return False
    return True


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.USER,
)
def modify_notification_authorized(email, notification_spec):

    # Check for special characters for potential SQL injection
    if not check_safe_input(notification_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    try:
        # Modify the existing user data
        query = (
            "UPDATE public.user_email_notification SET "
            "email_type_id = (SELECT email_type_id FROM public.email_type WHERE email_type = :new_email_type) "
            "WHERE user_id = (SELECT user_id FROM public.users WHERE email = :user_email)  "
            "AND email_type_id = (SELECT email_type_id FROM public.email_type WHERE email_type = :old_email_type)"
        )
        params = {
            "new_email_type": notification_spec["new_email_type"],
            "user_email": email,
            "old_email_type": notification_spec["old_email_type"],
        }
        pgquery.write_db(query, params=params)

    except IntegrityError as e:
        if e.orig is None:
            raise InternalServerError("Encountered unknown issue") from e
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        logging.error(f"Exception encountered: {failed_value}")
        raise InternalServerError(failed_value) from e
    except SQLAlchemyError as e:
        logging.error(f"SQL Exception encountered: {e}")
        raise InternalServerError("Encountered unknown issue executing query") from e

    return {"message": "Email notification successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.USER,
)
def delete_notification_authorized(user_email, email_type):
    notification_remove_query = (
        "DELETE FROM public.user_email_notification WHERE "
        "user_id IN (SELECT user_id FROM public.users WHERE email = :user_email) "
        "AND email_type_id IN (SELECT email_type_id FROM public.email_type WHERE email_type = :email_type)"
    )
    params = {
        "user_email": user_email,
        "email_type": email_type,
    }
    pgquery.write_db(notification_remove_query, params=params)

    return {"message": "Email notification successfully deleted"}


# REST endpoint resource class
class AdminNotificationGetSchema(Schema):
    user_email = fields.Str(required=True)


class AdminNotificationDeleteSchema(Schema):
    email = fields.Str(required=True)
    email_type = fields.Str(required=True)


class AdminNotificationPatchSchema(Schema):
    email = fields.Str(required=True)
    old_email_type = fields.Str(required=True)
    new_email_type = fields.Str(required=True)


class AdminNotification(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET,PATCH,DELETE",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    @require_permission(required_role=None)
    def get(self):
        logging.debug("AdminNotification GET requested")
        schema = AdminNotificationGetSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        user_email = urllib.request.unquote(request.args["user_email"])
        return (
            get_modify_notification_data_authorized(user_email),
            200,
            self.headers,
        )

    @require_permission(required_role=None)
    def patch(self):
        logging.debug("AdminUser PATCH requested")
        # Check for main body values
        schema = AdminNotificationPatchSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        return (
            modify_notification_authorized(request.json["email"], request.json),
            200,
            self.headers,
        )

    @require_permission(required_role=None)
    def delete(self):
        logging.debug("AdminNotification DELETE requested")
        schema = AdminNotificationDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        user_email = urllib.request.unquote(request.args["email"])
        email_type = urllib.request.unquote(request.args["email_type"])
        return (
            delete_notification_authorized(user_email, email_type),
            200,
            self.headers,
        )
