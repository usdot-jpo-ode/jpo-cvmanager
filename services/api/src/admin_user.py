from typing import Any
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import urllib.request
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import admin_new_user
import api_environment
from werkzeug.exceptions import InternalServerError, BadRequest
from common.auth_tools import (
    ORG_ROLE_LITERAL,
    RESOURCE_TYPE,
    EnvironWithOrg,
    PermissionResult,
    enforce_organization_restrictions,
    require_permission,
    generate_sql_placeholders_for_list,
)


def get_user_data(user_email: str, user: EnvironWithOrg, qualified_orgs: list[str]):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT u.email, u.first_name, u.last_name, u.super_user, org.name, roles.name AS role "
        "FROM public.users u "
        "LEFT JOIN public.user_organization AS uo ON uo.user_id = u.user_id "
        "LEFT JOIN public.organizations AS org ON org.organization_id = uo.organization_id "
        "LEFT JOIN public.roles ON roles.role_id = uo.role_id"
    )

    where_clauses = []
    params: dict[str, Any] = {}
    if not user.user_info.super_user:
        org_names_placeholder, _ = generate_sql_placeholders_for_list(
            qualified_orgs, params_to_update=params
        )
        where_clauses.append(f"org.name IN ({org_names_placeholder})")
    if user_email != "all":
        where_clauses.append("u.email = :user_email")
        params["user_email"] = user_email
    if where_clauses:
        query += " WHERE " + " AND ".join(where_clauses)
    query += ") as row"

    data = pgquery.query_db(query, params=params)

    user_dict = {}
    for row in data:
        row = dict(row[0])
        if row["email"] not in user_dict:
            user_dict[row["email"]] = {
                "email": row["email"],
                "first_name": row["first_name"],
                "last_name": row["last_name"],
                "super_user": True if row["super_user"] == "1" else False,
                "organizations": [],
            }
        if row["name"] is not None and row["role"] is not None:
            user_dict[row["email"]]["organizations"].append(
                {"name": row["name"], "role": row["role"]}
            )

    user_list = list(user_dict.values())
    # If list is empty and a single user was requested, return empty object
    if len(user_list) == 0 and user_email != "all":
        return {}
    # If list is not empty and a single user was requested, return the first index of the list
    elif len(user_list) == 1 and user_email != "all":
        return user_list[0]
    else:
        return user_list


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.USER,
)
def get_modify_user_data_authorized(
    user_email: str, permission_result: PermissionResult
):
    modify_user_obj = {}
    modify_user_obj["user_data"] = get_user_data(
        user_email, permission_result.user, permission_result.qualified_orgs
    )
    if user_email != "all":
        modify_user_obj["allowed_selections"] = admin_new_user.get_allowed_selections(
            permission_result.user
        )
    return modify_user_obj


def check_safe_input(user_spec):
    special_characters = "!\"#$%&'()*@-+,./:;<=>?[\\]^`{|}~"
    # Check all string based fields for special characters
    if any(c in special_characters for c in user_spec["first_name"]):
        return False
    if any(c in special_characters for c in user_spec["last_name"]):
        return False
    for org in user_spec["organizations_to_add"]:
        if any(c in special_characters for c in org["name"]):
            return False
        if any(c in special_characters for c in org["role"]):
            return False
    for org in user_spec["organizations_to_modify"]:
        if any(c in special_characters for c in org["name"]):
            return False
        if any(c in special_characters for c in org["role"]):
            return False
    for org in user_spec["organizations_to_remove"]:
        if any(c in special_characters for c in org["name"]):
            return False
        if any(c in special_characters for c in org["role"]):
            return False
    return True


def modify_user(orig_email: str, user_spec: dict):
    # Check for special characters for potential SQL injection
    if not admin_new_user.check_email(
        user_spec["email"]
    ) or not admin_new_user.check_email(orig_email):
        raise BadRequest("Email is not valid")
    if not check_safe_input(user_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    try:
        # Modify the existing user data
        query = (
            "UPDATE public.users SET "
            "email=:email, "
            "first_name=:first_name, "
            "last_name=:last_name, "
            "super_user=:super_user "
            "WHERE email=:orig_email"
        )
        params = {
            "email": user_spec["email"],
            "first_name": user_spec["first_name"],
            "last_name": user_spec["last_name"],
            "super_user": "1" if user_spec["super_user"] else "0",
            "orig_email": orig_email,
        }
        pgquery.write_db(query, params=params)

        # Add the user-to-organization relationships
        if len(user_spec["organizations_to_add"]) > 0:
            query_rows: list[tuple[str, dict]] = []
            for index, organization in enumerate(user_spec["organizations_to_add"]):
                org_name_placeholder = f"org_name_{index}"
                org_role_placeholder = f"org_role_{index}"
                query_rows.append(
                    (
                        "("
                        "(SELECT user_id FROM public.users WHERE email = :email), "
                        f"(SELECT organization_id FROM public.organizations WHERE name = :{org_name_placeholder}), "
                        f"(SELECT role_id FROM public.roles WHERE name = :{org_role_placeholder})"
                        ")",
                        {
                            org_name_placeholder: organization["name"],
                            org_role_placeholder: organization["role"],
                        },
                    )
                )

            query_prefix = "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES "
            pgquery.write_db_batched(
                query_prefix,
                query_rows,
                base_params={"email": user_spec["email"]},
            )

        # Modify the user-to-organization relationships
        for organization in user_spec["organizations_to_modify"]:
            org_modify_query = (
                "UPDATE public.user_organization "
                "SET role_id = (SELECT role_id FROM public.roles WHERE name = :role) "
                "WHERE user_id = (SELECT user_id FROM public.users WHERE email = :email) "
                "AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
            )
            params = {
                "role": organization["role"],
                "email": user_spec["email"],
                "org_name": organization["name"],
            }
            pgquery.write_db(org_modify_query, params=params)

        # Remove the user-to-organization relationships
        if len(user_spec["organizations_to_remove"]) > 0:
            params = {"email": user_spec["email"]}
            # Generate placeholders for each organization name
            org_placeholders = []
            for idx, org in enumerate(user_spec["organizations_to_remove"]):
                key = f"org_name_{idx}"
                org_placeholders.append(f":{key}")
                params[key] = org["name"]

            query = (
                "DELETE FROM public.user_organization WHERE "
                "user_id = (SELECT user_id FROM public.users WHERE email = :email) "
                f"AND organization_id IN (SELECT organization_id FROM public.organizations WHERE name IN ({', '.join(org_placeholders)}))"
            )
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

    return {"message": "User successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.USER,
)
def delete_user_authorized(user_email: str):
    # Delete user-to-organization relationships
    org_remove_query = (
        "DELETE FROM public.user_organization WHERE "
        "user_id = (SELECT user_id FROM public.users WHERE email = :email)"
    )
    params = {"email": user_email}
    pgquery.write_db(org_remove_query, params=params)

    # Delete user data
    user_remove_query = "DELETE FROM public.users WHERE email = :email"
    params = {"email": user_email}
    pgquery.write_db(user_remove_query, params=params)

    return {"message": "User successfully deleted"}


# REST endpoint resource class
class AdminUserGetDeleteSchema(Schema):
    user_email = fields.Str(required=True)


class UserOrganizationSchema(Schema):
    name = fields.Str(required=True)
    role = fields.Str(required=True)


class AdminUserPatchSchema(Schema):
    orig_email = fields.Str(required=True)
    email = fields.Str(required=True)
    first_name = fields.Str(required=True)
    last_name = fields.Str(required=True)
    super_user = fields.Bool(required=True)
    organizations_to_add = fields.List(
        fields.Nested(UserOrganizationSchema), required=True
    )
    organizations_to_modify = fields.List(
        fields.Nested(UserOrganizationSchema), required=True
    )
    organizations_to_remove = fields.List(
        fields.Nested(UserOrganizationSchema), required=True
    )


class AdminUser(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET,PATCH,DELETE",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    @require_permission(required_role=ORG_ROLE_LITERAL.ADMIN)
    def get(self):
        logging.debug("AdminUser GET requested")

        schema = AdminUserGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        user_email = urllib.request.unquote(request.args["user_email"])
        return (
            get_modify_user_data_authorized(user_email),
            200,
            self.headers,
        )

    @require_permission(required_role=ORG_ROLE_LITERAL.ADMIN)
    def patch(self, permission_result: PermissionResult):
        logging.debug("AdminUser PATCH requested")

        # Check for main body values
        if request.json is None:
            raise BadRequest("No JSON body found")
        body: dict[str, Any] = request.json

        schema = AdminUserPatchSchema()
        errors = schema.validate(body)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        enforce_organization_restrictions(
            user=permission_result.user,
            qualified_orgs=permission_result.qualified_orgs,
            spec=body,
            keys_to_check=[
                "organizations_to_add",
                "organizations_to_modify",
                "organizations_to_remove",
            ],
        )

        return (
            modify_user(body["orig_email"], body),
            200,
            self.headers,
        )

    @require_permission(required_role=ORG_ROLE_LITERAL.ADMIN)
    def delete(self):
        logging.debug("AdminUser DELETE requested")
        schema = AdminUserGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        user_email = urllib.request.unquote(request.args["user_email"])
        return (delete_user_authorized(user_email), 200, self.headers)
