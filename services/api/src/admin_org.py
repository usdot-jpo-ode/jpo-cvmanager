import logging
import common.pgquery as pgquery
import sqlalchemy
import admin_new_user
import os


def get_all_orgs():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT org.name, org.email, "
        "(SELECT COUNT(*) FROM public.user_organization uo WHERE uo.organization_id = org.organization_id) num_users, "
        "(SELECT COUNT(*) FROM public.rsu_organization ro WHERE ro.organization_id = org.organization_id) num_rsus, "
        "(SELECT COUNT(*) FROM public.intersection_organization io WHERE io.organization_id = org.organization_id) num_intersections "
        "FROM public.organizations org"
        ") as row"
    )
    data = pgquery.query_db(query)

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


def get_org_data(org_name):
    org_obj = {"org_users": [], "org_rsus": [], "org_intersections": []}

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
        f"WHERE org.name = '{org_name}'"
        ") as row"
    )
    data = pgquery.query_db(user_query)
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
        f"WHERE org.name = '{org_name}'"
        ") as row"
    )
    data = pgquery.query_db(rsu_query)
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
        f"WHERE org.name = '{org_name}'"
        ") as row"
    )
    data = pgquery.query_db(intersection_query)
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


def get_modify_org_data(org_name):
    modify_org_obj = {}
    # Get list of all organizations or details of a singular organization
    if org_name == "all":
        modify_org_obj["org_data"] = get_all_orgs()
    else:
        modify_org_obj["org_data"] = get_org_data(org_name)
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


def modify_org(org_spec):
    # Check for special characters for potential SQL injection
    if not check_safe_input(org_spec):
        return {
            "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        }, 500

    try:
        # Modify the existing organization data
        query = (
            "UPDATE public.organizations SET "
            f"name = '{org_spec['name']}', "
            f"email = '{org_spec['email']}' "
            f"WHERE name = '{org_spec['orig_name']}'"
        )
        pgquery.write_db(query)

        # Add the user-to-organization relationships
        if len(org_spec["users_to_add"]) > 0:
            user_add_query = "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES"
            for user in org_spec["users_to_add"]:
                user_add_query += (
                    " ("
                    f"(SELECT user_id FROM public.users WHERE email = '{user['email']}'), "
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}'), "
                    f"(SELECT role_id FROM public.roles WHERE name = '{user['role']}')"
                    "),"
                )
            user_add_query = user_add_query[:-1]
            pgquery.write_db(user_add_query)

        # Modify the user-to-organization relationships
        for user in org_spec["users_to_modify"]:
            user_modify_query = (
                "UPDATE public.user_organization "
                f"SET role_id = (SELECT role_id FROM public.roles WHERE name = '{user['role']}') "
                f"WHERE user_id = (SELECT user_id FROM public.users WHERE email = '{user['email']}') "
                f"AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}')"
            )
            pgquery.write_db(user_modify_query)

        # Remove the user-to-organization relationships
        for user in org_spec["users_to_remove"]:
            user_remove_query = (
                "DELETE FROM public.user_organization WHERE "
                f"user_id = (SELECT user_id FROM public.users WHERE email = '{user['email']}') "
                f"AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}')"
            )
            pgquery.write_db(user_remove_query)

        # Add the rsu-to-organization relationships
        if len(org_spec["rsus_to_add"]) > 0:
            rsu_add_query = (
                "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES"
            )
            for rsu in org_spec["rsus_to_add"]:
                rsu_add_query += (
                    " ("
                    f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu}'), "
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}')"
                    "),"
                )
            rsu_add_query = rsu_add_query[:-1]
            pgquery.write_db(rsu_add_query)

        # Remove the rsu-to-organization relationships
        for rsu in org_spec["rsus_to_remove"]:
            rsu_remove_query = (
                "DELETE FROM public.rsu_organization WHERE "
                f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu}') "
                f"AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}')"
            )
            pgquery.write_db(rsu_remove_query)

        # Add the intersection-to-organization relationships
        if len(org_spec["intersections_to_add"]) > 0:
            intersection_add_query = "INSERT INTO public.intersection_organization(intersection_id, organization_id) VALUES"
            for intersection_id in org_spec["intersections_to_add"]:
                intersection_add_query += (
                    " ("
                    f"(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}'), "
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}')"
                    "),"
                )
            intersection_add_query = intersection_add_query[:-1]
            pgquery.write_db(intersection_add_query)

        # Remove the intersection-to-organization relationships
        for intersection_id in org_spec["intersections_to_remove"]:
            intersection_remove_query = (
                "DELETE FROM public.intersection_organization WHERE "
                f"intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}') "
                f"AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = '{org_spec['name']}')"
            )
            pgquery.write_db(intersection_remove_query)
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

    return {"message": "Organization successfully modified"}, 200


def delete_org(org_name):

    if check_orphan_rsus(org_name):
        return {
            "message": "Cannot delete organization that has one or more RSUs only associated with this organization"
        }, 400

    if check_orphan_intersections(org_name):
        return {
            "message": "Cannot delete organization that has one or more Intersections only associated with this organization"
        }, 400

    if check_orphan_users(org_name):
        return {
            "message": "Cannot delete organization that has one or more users only associated with this organization"
        }, 400

    # Delete user-to-organization relationships
    user_org_remove_query = (
        "DELETE FROM public.user_organization WHERE "
        f"organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{org_name}')"
    )
    pgquery.write_db(user_org_remove_query)

    # Delete rsu-to-organization relationships
    rsu_org_remove_query = (
        "DELETE FROM public.rsu_organization WHERE "
        f"organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{org_name}')"
    )
    pgquery.write_db(rsu_org_remove_query)

    # Delete intersection-to-organization relationships
    intersection_org_remove_query = (
        "DELETE FROM public.intersection_organization WHERE "
        f"organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{org_name}')"
    )
    pgquery.write_db(intersection_org_remove_query)

    # Delete organization data
    org_remove_query = "DELETE FROM public.organizations WHERE " f"name = '{org_name}'"
    pgquery.write_db(org_remove_query)

    return {"message": "Organization successfully deleted"}, 200


def check_orphan_rsus(org):
    rsu_query = (
        "SELECT to_jsonb(row) "
        "FROM (SELECT rsu_id, count(organization_id) count FROM rsu_organization WHERE rsu_id IN (SELECT rsu_id FROM rsu_organization WHERE organization_id = "
        f"(SELECT organization_id FROM organizations WHERE name = '{org}')) GROUP BY rsu_id) as row"
    )
    rsu_count = pgquery.query_db(rsu_query)
    for row in rsu_count:
        rsu = dict(row[0])
        if rsu["count"] == 1:
            return True
    return False


def check_orphan_intersections(org):
    intersection_query = (
        "SELECT to_jsonb(row) "
        "FROM (SELECT intersection_id, count(organization_id) count FROM intersection_organization WHERE intersection_id IN (SELECT intersection_id FROM intersection_organization WHERE organization_id = "
        f"(SELECT organization_id FROM organizations WHERE name = '{org}')) GROUP BY intersection_id) as row"
    )
    intersection_count = pgquery.query_db(intersection_query)
    for row in intersection_count:
        intersection = dict(row[0])
        if intersection["count"] == 1:
            return True
    return False


def check_orphan_users(org):
    user_query = (
        "SELECT to_jsonb(row) "
        "FROM (SELECT user_id, count(organization_id) count FROM user_organization WHERE user_id IN (SELECT user_id FROM user_organization WHERE organization_id = "
        f"(SELECT organization_id FROM organizations WHERE name = '{org}')) GROUP BY user_id) as row"
    )
    user_count = pgquery.query_db(user_query)
    for row in user_count:
        user = dict(row[0])
        if user["count"] == 1:
            return True
    return False


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import urllib.request


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

    def get(self):
        logging.debug("AdminOrg GET requested")
        schema = AdminOrgGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        org_name = urllib.request.unquote(request.args["org_name"])
        return (get_modify_org_data(org_name), 200, self.headers)

    def patch(self):
        logging.debug("AdminOrg PATCH requested")
        # Check for main body values
        schema = AdminOrgPatchSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        data, code = modify_org(request.json)
        return (data, code, self.headers)

    def delete(self):
        logging.debug("AdminOrg DELETE requested")
        schema = AdminOrgGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        org_name = urllib.request.unquote(request.args["org_name"])
        message, code = delete_org(org_name)
        return (message, code, self.headers)
