from typing import Any
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import urllib.request
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import admin_new_user
import os
from werkzeug.exceptions import InternalServerError, BadRequest, Conflict, Forbidden
from common.auth_tools import (
    ORG_ROLE_LITERAL,
    RESOURCE_TYPE,
    PermissionResult,
    check_role_above,
    require_permission,
    generate_sql_placeholders_for_list,
)


def get_all_orgs(organizations: list[str] | None):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT org.name, org.email, "
        "(SELECT COUNT(*) FROM public.user_organization uo WHERE uo.organization_id = org.organization_id) num_users, "
        "(SELECT COUNT(*) FROM public.rsu_organization ro WHERE ro.organization_id = org.organization_id) num_rsus, "
        "(SELECT COUNT(*) FROM public.intersection_organization io WHERE io.organization_id = org.organization_id) num_intersections "
    )
    params: dict[str, Any] = {}
    if organizations is None:
        query += "FROM public.organizations org "
    else:
        org_names_placeholder, _ = generate_sql_placeholders_for_list(
            organizations, params_to_update=params
        )
        query += f"FROM public.organizations org WHERE org.name IN ({org_names_placeholder}) "
    query += ") as row"

    data = pgquery.query_db(query, params=params)

    return_obj = []
    for row in data:
        row = dict(row[0])
        org_obj = {}
        org_obj["name"] = row["name"]
        org_obj["email"] = row["email"]
        org_obj["user_count"] = row["num_users"]
        org_obj["rsu_count"] = row["num_rsus"]
        org_obj["intersection_count"] = row["num_intersections"]
        return_obj.append(org_obj)

    return return_obj


def get_org_data(org_name: str, is_admin_in_org: bool):
    org_obj: dict = {"org_users": [], "org_rsus": [], "org_intersections": []}

    if is_admin_in_org:
        # Get all user members of the organization
        user_query = (
            "SELECT to_jsonb(row) "
            "FROM ("
            "SELECT u.email, u.first_name, u.last_name, u.name role_name "
            "FROM public.organizations AS org "
            "JOIN ("
            "SELECT uo.organization_id, users.email, users.first_name, users.last_name, roles.name "
            "FROM public.user_organization uo "
            "JOIN public.users ON uo.user_id = users.user_id "
            "JOIN public.roles ON uo.role_id = roles.role_id"
            ") u ON u.organization_id = org.organization_id "
            "WHERE org.name = :org_name"
            ") as row"
        )
        params = {"org_name": org_name}
        data = pgquery.query_db(user_query, params=params)
        for row in data:
            row = dict(row[0])
            user_obj = {}
            user_obj["email"] = row["email"]
            user_obj["first_name"] = row["first_name"]
            user_obj["last_name"] = row["last_name"]
            user_obj["role"] = row["role_name"]
            org_obj["org_users"].append(user_obj)

    # Get all RSU members of the organization
    rsu_query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT r.ipv4_address, r.primary_route, r.milepost "
        "FROM public.organizations AS org "
        "JOIN ("
        "SELECT ro.organization_id, rsus.ipv4_address, rsus.primary_route, rsus.milepost "
        "FROM public.rsu_organization ro "
        "JOIN public.rsus ON ro.rsu_id = rsus.rsu_id"
        ") r ON r.organization_id = org.organization_id "
        "WHERE org.name = :org_name"
        ") as row"
    )
    params = {"org_name": org_name}
    data = pgquery.query_db(rsu_query, params=params)
    for row in data:
        row = dict(row[0])
        rsu_obj = {}
        rsu_obj["ip"] = str(row["ipv4_address"])
        rsu_obj["primary_route"] = row["primary_route"]
        rsu_obj["milepost"] = row["milepost"]
        org_obj["org_rsus"].append(rsu_obj)

    # Get all Intersection members of the organization
    intersection_query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT i.intersection_number, i.intersection_name, i.origin_ip "
        "FROM public.organizations AS org "
        "JOIN ("
        "SELECT io.organization_id, intersections.intersection_number, intersections.intersection_name, intersections.origin_ip "
        "FROM public.intersection_organization io "
        "JOIN public.intersections ON io.intersection_id = intersections.intersection_id"
        ") i ON i.organization_id = org.organization_id "
        "WHERE org.name = :org_name"
        ") as row"
    )
    params = {"org_name": org_name}
    data = pgquery.query_db(intersection_query, params=params)
    for row in data:
        row = dict(row[0])
        intersection_obj = {}
        intersection_obj["intersection_id"] = row["intersection_number"]
        intersection_obj["intersection_name"] = row["intersection_name"]
        intersection_obj["origin_ip"] = row["origin_ip"]
        org_obj["org_intersections"].append(intersection_obj)

    return org_obj


def get_allowed_selections():
    obj = {"user_roles": []}
    query = "SELECT to_jsonb(row) FROM (SELECT name FROM public.roles) as row"
    data = pgquery.query_db(query)
    for row in data:
        row = dict(row[0])
        obj["user_roles"].append(row["name"])
    return obj


@require_permission(
    required_role=ORG_ROLE_LITERAL.USER,
    resource_type=RESOURCE_TYPE.ORGANIZATION,
)
def get_modify_org_data_authorized(org_name: str, permission_result: PermissionResult):
    modify_org_obj = {}
    # Get list of all organizations or details of a singular organization
    # Only requires "user" role to access this endpoint, as it is just counts
    if org_name == "all":
        if permission_result.user.user_info.super_user:
            modify_org_obj["org_data"] = get_all_orgs(None)
        else:
            modify_org_obj["org_data"] = get_all_orgs(permission_result.qualified_orgs)
    else:
        # Only requires "user" role to access this endpoint, as it is just counts
        role = permission_result.user.user_info.organizations.get(org_name)
        if role is None:
            raise Forbidden(
                f"User does not have access to the requested organization: {org_name}"
            )
        is_admin_in_org = (
            permission_result.user.user_info.super_user
            or check_role_above(
                role,
                ORG_ROLE_LITERAL.ADMIN,
            )
        )
        modify_org_obj["org_data"] = get_org_data(org_name, is_admin_in_org)
        modify_org_obj["allowed_selections"] = get_allowed_selections()

    return modify_org_obj


def check_safe_input(org_spec):
    special_characters = "!\"#$%&'()*@-+,/:;<=>?[\\]^`{|}~"
    # Check all string based fields for special characters
    if any(c in special_characters for c in org_spec["orig_name"]):
        return False
    if any(c in special_characters for c in org_spec["name"]):
        return False
    if org_spec["email"]:
        if org_spec["email"] != "" and not admin_new_user.check_email(
            org_spec["email"]
        ):
            return False
    for user in org_spec["users_to_add"]:
        if not admin_new_user.check_email(user["email"]):
            return False
        if any(c in special_characters for c in user["role"]):
            return False
    for user in org_spec["users_to_modify"]:
        if not admin_new_user.check_email(user["email"]):
            return False
        if any(c in special_characters for c in user["role"]):
            return False
    for user in org_spec["users_to_remove"]:
        if not admin_new_user.check_email(user["email"]):
            return False
        if any(c in special_characters for c in user["role"]):
            return False
    return True


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.ORGANIZATION,
)
def modify_org_authorized(orig_name: str, org_spec: dict):
    # Check for special characters for potential SQL injection
    if not check_safe_input(org_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    try:
        # Modify the existing organization data
        query = (
            "UPDATE public.organizations SET "
            "name = :name, "
            "email = :email "
            "WHERE name = :orig_name"
        )
        params = {
            "name": org_spec["name"],
            "email": org_spec["email"],
            "orig_name": orig_name,
        }
        pgquery.write_db(query, params=params)

        if len(org_spec["users_to_add"]) > 0:
            query_rows: list[tuple[str, dict]] = []
            for index, user in enumerate(org_spec["users_to_add"]):
                email_placeholder = f"user_email_{index}"
                role_placeholder = f"user_role_{index}"
                query_rows.append(
                    (
                        "("
                        f"(SELECT user_id FROM public.users WHERE email = :{email_placeholder}), "
                        "(SELECT organization_id FROM public.organizations WHERE name = :org_name), "
                        f"(SELECT role_id FROM public.roles WHERE name = :{role_placeholder})"
                        ")",
                        {
                            email_placeholder: user["email"],
                            role_placeholder: user["role"],
                        },
                    )
                )

            query_prefix = "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES "
            pgquery.write_db_batched(
                query_prefix,
                query_rows,
                base_params={"org_name": org_spec["name"]},
            )

        # Modify the user-to-organization relationships
        for user in org_spec["users_to_modify"]:
            user_modify_query = (
                "UPDATE public.user_organization "
                "SET role_id = (SELECT role_id FROM public.roles WHERE name = :role) "
                "WHERE user_id = (SELECT user_id FROM public.users WHERE email = :email) "
                "AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
            )
            params = {
                "role": user["role"],
                "email": user["email"],
                "org_name": org_spec["name"],
            }
            pgquery.write_db(user_modify_query, params=params)

        # Remove the user-to-organization relationships
        if len(org_spec["users_to_remove"]) > 0:
            params = {"org_name": org_spec["name"]}
            # Generate placeholders for each organization name
            email_placeholders = []
            for idx, user in enumerate(org_spec["users_to_remove"]):
                key = f"email_{idx}"
                email_placeholders.append(f":{key}")
                params[key] = user["email"]

            query = (
                "DELETE FROM public.user_organization WHERE "
                f"user_id IN (SELECT user_id FROM public.users WHERE email IN ({', '.join(email_placeholders)})) "
                "AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
            )
            pgquery.write_db(query, params=params)

        # Add the rsu-to-organization relationships
        if len(org_spec["rsus_to_add"]) > 0:
            query_rows = []
            for index, rsu_ip in enumerate(org_spec["rsus_to_add"]):
                ip_placeholder = f"rsu_ip_{index}"
                query_rows.append(
                    (
                        "("
                        f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :{ip_placeholder}), "
                        "(SELECT organization_id FROM public.organizations WHERE name = :org_name)"
                        ")",
                        {ip_placeholder: rsu_ip},
                    )
                )

            query_prefix = (
                "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES "
            )
            pgquery.write_db_batched(
                query_prefix,
                query_rows,
                base_params={"org_name": org_spec["name"]},
            )

        # Remove the rsu-to-organization relationships
        if len(org_spec["rsus_to_remove"]) > 0:
            params = {"org_name": org_spec["name"]}
            # Generate placeholders for each rsu IP
            ip_placeholders = []
            for idx, rsu_ip in enumerate(org_spec["rsus_to_remove"]):
                key = f"rsu_ip_{idx}"
                ip_placeholders.append(f":{key}")
                params[key] = rsu_ip

            query = (
                "DELETE FROM public.rsu_organization WHERE "
                f"rsu_id IN (SELECT rsu_id FROM public.rsus WHERE ipv4_address IN ({', '.join(ip_placeholders)})) "
                "AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
            )
            pgquery.write_db(query, params=params)

        # Add the intersection-to-organization relationships
        if len(org_spec["intersections_to_add"]) > 0:
            query_rows = []
            for index, intersection_id in enumerate(org_spec["intersections_to_add"]):
                id_placeholder = f"intersection_id_{index}"
                query_rows.append(
                    (
                        "("
                        f"(SELECT intersection_id FROM public.intersections WHERE intersection_number = :{id_placeholder}), "
                        "(SELECT organization_id FROM public.organizations WHERE name = :org_name)"
                        ")",
                        {id_placeholder: str(intersection_id)},
                    )
                )

            query_prefix = "INSERT INTO public.intersection_organization(intersection_id, organization_id) VALUES "
            pgquery.write_db_batched(
                query_prefix,
                query_rows,
                base_params={"org_name": org_spec["name"]},
            )

        # Remove the intersection-to-organization relationships
        if len(org_spec["intersections_to_remove"]) > 0:
            params = {"org_name": org_spec["name"]}
            # Generate placeholders for each intersection ID
            id_placeholders = []
            for idx, intersection_id in enumerate(org_spec["intersections_to_remove"]):
                key = f"intersection_id_{idx}"
                id_placeholders.append(f":{key}")
                params[key] = str(intersection_id)

            query = (
                "DELETE FROM public.intersection_organization WHERE "
                f"intersection_id IN (SELECT intersection_id FROM public.intersections WHERE intersection_number IN ({', '.join(id_placeholders)})) "
                "AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
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

    return {"message": "Organization successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.ADMIN,
    resource_type=RESOURCE_TYPE.ORGANIZATION,
)
def delete_org_authorized(org_name: str):
    if check_orphan_rsus(org_name):
        raise Conflict(
            "Cannot delete organization that has one or more RSUs only associated with this organization"
        )

    if check_orphan_intersections(org_name):
        raise Conflict(
            "Cannot delete organization that has one or more Intersections only associated with this organization"
        )

    if check_orphan_users(org_name):
        raise Conflict(
            "Cannot delete organization that has one or more users only associated with this organization"
        )

    # Delete user-to-organization relationships
    user_org_remove_query = (
        "DELETE FROM public.user_organization WHERE "
        "organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
    )
    pgquery.write_db(user_org_remove_query, params={"org_name": org_name})

    # Delete rsu-to-organization relationships
    rsu_org_remove_query = (
        "DELETE FROM public.rsu_organization WHERE "
        "organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
    )
    pgquery.write_db(rsu_org_remove_query, params={"org_name": org_name})

    # Delete intersection-to-organization relationships
    intersection_org_remove_query = (
        "DELETE FROM public.intersection_organization WHERE "
        "organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
    )
    pgquery.write_db(intersection_org_remove_query, params={"org_name": org_name})

    # Delete organization data
    org_remove_query = "DELETE FROM public.organizations WHERE name = :org_name"
    pgquery.write_db(org_remove_query, params={"org_name": org_name})

    return {"message": "Organization successfully deleted"}


def check_orphan_rsus(org):
    rsu_query = (
        "SELECT to_jsonb(row) "
        "FROM (SELECT rsu_id, count(organization_id) count FROM rsu_organization WHERE rsu_id IN (SELECT rsu_id FROM rsu_organization WHERE organization_id = "
        "(SELECT organization_id FROM organizations WHERE name = :org_name)) GROUP BY rsu_id) as row"
    )
    params = {"org_name": org}
    rsu_count = pgquery.query_db(rsu_query, params=params)
    for row in rsu_count:
        rsu = dict(row[0])
        if rsu["count"] == 1:
            return True
    return False


def check_orphan_intersections(org):
    intersection_query = (
        "SELECT to_jsonb(row) "
        "FROM (SELECT intersection_id, count(organization_id) count FROM intersection_organization WHERE intersection_id IN (SELECT intersection_id FROM intersection_organization WHERE organization_id = "
        "(SELECT organization_id FROM organizations WHERE name = :org_name)) GROUP BY intersection_id) as row"
    )
    params = {"org_name": org}
    intersection_count = pgquery.query_db(intersection_query, params=params)
    for row in intersection_count:
        intersection = dict(row[0])
        if intersection["count"] == 1:
            return True
    return False


def check_orphan_users(org):
    user_query = (
        "SELECT to_jsonb(row) "
        "FROM (SELECT user_id, count(organization_id) count FROM user_organization WHERE user_id IN (SELECT user_id FROM user_organization WHERE organization_id = "
        "(SELECT organization_id FROM organizations WHERE name = :org_name)) GROUP BY user_id) as row"
    )
    params = {"org_name": org}
    user_count = pgquery.query_db(user_query, params=params)
    for row in user_count:
        user = dict(row[0])
        if user["count"] == 1:
            return True
    return False


# REST endpoint resource class
class AdminOrgGetDeleteSchema(Schema):
    org_name = fields.Str(required=True)


class UserRoleSchema(Schema):
    email = fields.Str(required=True)
    role = fields.Str(required=True)


class AdminOrgPatchSchema(Schema):
    orig_name = fields.Str(required=True)
    name = fields.Str(required=True)
    email = fields.Str(required=True, allow_none=True)
    users_to_add = fields.List(fields.Nested(UserRoleSchema), required=True)
    users_to_modify = fields.List(fields.Nested(UserRoleSchema), required=True)
    users_to_remove = fields.List(fields.Nested(UserRoleSchema), required=True)
    rsus_to_add = fields.List(fields.IPv4(), required=True)
    rsus_to_remove = fields.List(fields.IPv4(), required=True)
    intersections_to_add = fields.List(fields.Integer, required=True)
    intersections_to_remove = fields.List(fields.Integer, required=True)


class AdminOrg(Resource):
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

    @require_permission(required_role=ORG_ROLE_LITERAL.USER)
    def get(self):
        logging.debug("AdminOrg GET requested")
        schema = AdminOrgGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        org_name = urllib.request.unquote(request.args["org_name"])

        return (get_modify_org_data_authorized(org_name), 200, self.headers)

    @require_permission(required_role=ORG_ROLE_LITERAL.ADMIN)
    def patch(self):
        logging.debug("AdminOrg PATCH requested")

        # Check for main body values
        schema = AdminOrgPatchSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))
        return (
            modify_org_authorized(request.json["orig_name"], request.json),
            200,
            self.headers,
        )

    @require_permission(required_role=ORG_ROLE_LITERAL.ADMIN)
    def delete(self):
        logging.debug("AdminOrg DELETE requested")

        schema = AdminOrgGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        org_name = urllib.request.unquote(request.args["org_name"])
        return (delete_org_authorized(org_name), 200, self.headers)
