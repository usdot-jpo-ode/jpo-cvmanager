import logging
import common.pgquery as pgquery
import sqlalchemy
import os


def query_and_return_list(query):
    data = pgquery.query_db(query)
    return_list = []
    for row in data:
        return_list.append(" ".join(row))
    return return_list


def get_allowed_selections():
    allowed = {}

    organizations_query = "SELECT name FROM public.organizations ORDER BY name ASC"
    roles_query = "SELECT name FROM public.roles ORDER BY name"

    allowed["organizations"] = query_and_return_list(organizations_query)
    allowed["roles"] = query_and_return_list(roles_query)

    return allowed


def check_email(email):
    email_special_characters = [
        "!!",
        "##",
        "$$",
        "%%",
        "&&",
        "''",
        "\\",
        "**",
        "++",
        "--",
        "//",
        "==",
        "??",
        "^^",
        "__",
        "``",
        "{{",
        "||",
        '"',
        "(",
        ")",
        "}",
        ",",
        ":",
        ";",
        "<",
        ">",
        "[",
        "]",
    ]
    if (
        any(check in email for check in email_special_characters)
        or email.count("@") != 1
    ):
        return False
    return True


def check_safe_input(user_spec):
    special_characters = "!\"#$%&'()*@-+,./:;<=>?[\]^`{|}~"
    # Check all string based fields for special characters
    if any(c in special_characters for c in user_spec["first_name"]):
        return False
    if any(c in special_characters for c in user_spec["last_name"]):
        return False
    for org in user_spec["organizations"]:
        if any(c in special_characters for c in org["name"]):
            return False
        if any(c in special_characters for c in org["role"]):
            return False
    return True


def add_user(user_spec):
    # Check for special characters for potential SQL injection
    if not check_email(user_spec["email"]):
        return {"message": "Email is not valid"}, 500
    if not check_safe_input(user_spec):
        return {
            "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\]^`{|}~. No sequences of '-' characters are allowed"
        }, 500

    try:
        user_insert_query = (
            "INSERT INTO public.users(email, first_name, last_name, super_user) "
            f"VALUES ('{user_spec['email']}', '{user_spec['first_name']}', '{user_spec['last_name']}', '{'1' if user_spec['super_user'] else '0'}')"
        )
        pgquery.write_db(user_insert_query)

        user_org_insert_query = "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES"
        for organization in user_spec["organizations"]:
            user_org_insert_query += (
                " ("
                f"(SELECT user_id FROM public.users WHERE email = '{user_spec['email']}'), "
                f"(SELECT organization_id FROM public.organizations WHERE name = '{organization['name']}'), "
                f"(SELECT role_id FROM public.roles WHERE name = '{organization['role']}')"
                "),"
            )
        user_org_insert_query = user_org_insert_query[:-1]
        pgquery.write_db(user_org_insert_query)
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

    return {"message": "New user successfully added"}, 200


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate


class UserOrganizationSchema(Schema):
    name = fields.Str(required=True)
    role = fields.Str(required=True)


class AdminNewUserSchema(Schema):
    email = fields.Str(required=True)
    first_name = fields.Str(required=True)
    last_name = fields.Str(required=True)
    super_user = fields.Bool(required=True)
    organizations = fields.List(
        fields.Nested(UserOrganizationSchema),
        required=True,
        validate=validate.Length(min=1),
    )


class AdminNewUser(Resource):
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
        logging.debug("AdminNewUser GET requested")
        return (get_allowed_selections(), 200, self.headers)

    def post(self):
        logging.debug("AdminNewUser POST requested")
        # Check for main body values
        schema = AdminNewUserSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        data, code = add_user(request.json)
        return (data, code, self.headers)
