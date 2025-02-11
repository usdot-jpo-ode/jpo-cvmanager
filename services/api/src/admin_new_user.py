from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError
import os
from werkzeug.exceptions import InternalServerError, BadRequest, Forbidden
from common.auth_tools import (
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    PermissionResult,
    get_qualified_org_list,
    require_permission,
)


def get_allowed_selections(user: EnvironWithOrg):
    allowed = {}

    allowed["organizations"] = get_qualified_org_list(
        user, ORG_ROLE_LITERAL.ADMIN, include_super_user=True
    )

    roles_query = "SELECT name FROM public.roles ORDER BY name"
    allowed["roles"] = pgquery.query_and_return_list(roles_query)

    return allowed


def check_email(email):
    email_special_characters = [
        "!!",
        "##",
        "$$",
        "%%",
        "&&",
        "''",
        "\\\\",
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
    special_characters = "!\"#$%&'()*@-+,./:;<=>?[\\]^`{|}~"
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


def add_user(user_spec: dict):
    # Check for special characters for potential SQL injection
    if not check_email(user_spec["email"]):
        raise BadRequest("Email is not valid")
    if not check_safe_input(user_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

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
    except IntegrityError as e:
        if e.orig is None:
            raise InternalServerError("Encountered unknown issue") from e
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        logging.error(f"Exception encountered: {failed_value}")
        raise InternalServerError(failed_value) from e
    except InternalServerError:
        # Re-raise InternalServerError without catching it
        raise
    except Exception as e:
        logging.error(f"Exception encountered: {e}")
        raise InternalServerError("Encountered unknown issue") from e

    return {"message": "New user successfully added"}


# REST endpoint resource class
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


def enforce_add_user_org_permissions(
    user: EnvironWithOrg,
    qualified_orgs: list[str],
    user_spec: dict,
):
    if not user.user_info.super_user:
        unqualified_orgs = [
            org
            for org in user_spec.get("organizations", [])
            if org not in qualified_orgs
        ]
        if unqualified_orgs:
            raise Forbidden(
                f"Unauthorized added organizations: {','.join(unqualified_orgs)}"
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

    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
    )
    def get(self, permission_result: PermissionResult):
        logging.debug("AdminNewUser GET requested")
        return (get_allowed_selections(permission_result.user), 200, self.headers)

    @require_permission(
        required_role=ORG_ROLE_LITERAL.ADMIN,
    )
    def post(self, permission_result: PermissionResult):
        logging.debug("AdminNewUser POST requested")

        # Check for main body values
        schema = AdminNewUserSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))
        enforce_add_user_org_permissions(
            permission_result.user, permission_result.qualified_orgs, request.json
        )

        return (add_user(request.json), 200, self.headers)
