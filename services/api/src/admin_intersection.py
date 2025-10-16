from typing import Any
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import admin_new_intersection
import os
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


def get_intersection_data(
    intersection_id: str, user: EnvironWithOrg, qualified_orgs: list[str]
):
    """
    Retrieve intersection data from the database for a given intersection ID or all intersections.

    Args:
        intersection_id (str): The ID of the intersection to retrieve data for.
                               Use "all" to retrieve data for all intersections.
        user (EnvironWithOrg): The user object containing organizational context and permissions.
        qualified_orgs (list[str]): A list of organizations the user is qualified to access.

    Returns:
        dict or list[dict]:
            - If a single intersection ID is provided and found, returns a dictionary containing the intersection data.
            - If "all" is provided, returns a list of dictionaries containing data for all intersections.
            - If no data is found for a single intersection ID, returns an empty dictionary.

    Raises:
        Exception: If there is an issue querying the database or processing the data.
    """
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT intersection_number, ST_X(ref_pt::geometry) AS ref_pt_longitude, ST_Y(ref_pt::geometry) AS ref_pt_latitude, "
        "ST_XMin(bbox::geometry) AS bbox_longitude_1, ST_YMin(bbox::geometry) AS bbox_latitude_1, "
        "ST_XMax(bbox::geometry) AS bbox_longitude_2, ST_YMax(bbox::geometry) AS bbox_latitude_2, "
        "intersection_name, origin_ip, "
        "org.name AS org_name, rsu.ipv4_address AS rsu_ip  "
        "FROM public.intersections "
        "JOIN public.intersection_organization AS ro ON ro.intersection_id = intersections.intersection_id  "
        "JOIN public.organizations AS org ON org.organization_id = ro.organization_id  "
        "LEFT JOIN public.rsu_intersection AS ri ON ri.intersection_id = intersections.intersection_id  "
        "LEFT JOIN public.rsus AS rsu ON rsu.rsu_id = ri.rsu_id "
    )

    where_clauses = []
    params: dict[str, Any] = {}
    if not user.user_info.super_user:
        org_names_placeholder, _ = generate_sql_placeholders_for_list(
            qualified_orgs, params_to_update=params
        )
        where_clauses.append(f"org.name IN ({org_names_placeholder})")

    if intersection_id != "all":
        where_clauses.append("intersection_number = :intersection_id")
        params["intersection_id"] = intersection_id
    if where_clauses:
        query += "WHERE " + " AND ".join(where_clauses)
    query += ") as row"

    data = pgquery.query_db(query, params=params)

    intersection_dict = {}
    for row in data:
        row = dict(row[0])
        if str(row["intersection_number"]) not in intersection_dict:
            intersection_dict[str(row["intersection_number"])] = {
                "intersection_id": str(row["intersection_number"]),
                "ref_pt": {
                    "latitude": row["ref_pt_latitude"],
                    "longitude": row["ref_pt_longitude"],
                },
                "bbox": {
                    "latitude1": row["bbox_latitude_1"],
                    "longitude1": row["bbox_longitude_1"],
                    "latitude2": row["bbox_latitude_2"],
                    "longitude2": row["bbox_longitude_2"],
                },
                "intersection_name": row["intersection_name"],
                "origin_ip": row["origin_ip"],
                "organizations": [],
                "rsus": [],
            }
        orgs = intersection_dict[str(row["intersection_number"])]["organizations"]
        rsus = intersection_dict[str(row["intersection_number"])]["rsus"]
        if row["org_name"] not in orgs:
            orgs.append(row["org_name"])
        if row["rsu_ip"] not in rsus and row["rsu_ip"] is not None:
            rsus.append(row["rsu_ip"])

    intersection_list = list(intersection_dict.values())
    # If list is empty and a single Intersection was requested, return empty object
    if len(intersection_list) == 0 and intersection_id != "all":
        return {}
    # If list is not empty and a single Intersection was requested, return the first index of the list
    elif len(intersection_list) == 1 and intersection_id != "all":
        return intersection_list[0]
    else:
        return intersection_list


def get_modify_intersection_data(
    intersection_id: str, user: EnvironWithOrg, qualified_orgs: list[str]
):
    """
    Retrieve and modify intersection summary data for a given intersection ID.

    Args:
        intersection_id (str): The ID of the intersection to retrieve data for.
                               Use "all" to retrieve data for all intersections.
        user (EnvironWithOrg): The user object containing organizational context.
        qualified_orgs (list[str]): A list of organizations the user is qualified to access.

    Returns:
        dict: A dictionary containing intersection data (and allowed selections
              if a specific intersection ID is provided).
    """
    modify_intersection_obj = {}
    modify_intersection_obj["intersection_data"] = get_intersection_data(
        intersection_id, user, qualified_orgs
    )
    if intersection_id != "all":
        modify_intersection_obj["allowed_selections"] = (
            admin_new_intersection.get_allowed_selections(user)
        )
    return modify_intersection_obj


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.INTERSECTION
)
def modify_intersection_authorized(
    permission_result: PermissionResult, intersection_id: str, intersection_spec: dict
):
    enforce_organization_restrictions(
        user=permission_result.user,
        qualified_orgs=permission_result.qualified_orgs,
        spec=intersection_spec,
        keys_to_check=["organizations_to_add", "organizations_to_remove"],
    )

    # Check for special characters for potential SQL injection
    if not admin_new_intersection.check_safe_input(intersection_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    try:
        # Modify the existing Intersection data
        query = (
            "UPDATE public.intersections SET "
            "intersection_number=:intersection_id, "
            "ref_pt=ST_GeomFromText('POINT(' || :ref_pt_longitude || ' ' || :ref_pt_latitude || ')')"
        )
        params = {
            "intersection_id": intersection_id,
            "ref_pt_longitude": intersection_spec["ref_pt"]["longitude"],
            "ref_pt_latitude": intersection_spec["ref_pt"]["latitude"],
        }
        if "bbox" in intersection_spec:
            query += ", bbox=ST_MakeEnvelope(:bbox_longitude1,:bbox_latitude1,:bbox_longitude2,:bbox_latitude2)"
            params["bbox_longitude1"] = intersection_spec["bbox"]["longitude1"]
            params["bbox_latitude1"] = intersection_spec["bbox"]["latitude1"]
            params["bbox_longitude2"] = intersection_spec["bbox"]["longitude2"]
            params["bbox_latitude2"] = intersection_spec["bbox"]["latitude2"]
        if "intersection_name" in intersection_spec:
            query += ", intersection_name=:intersection_name"
            params["intersection_name"] = intersection_spec.get("intersection_name", "")
        if "origin_ip" in intersection_spec:
            query += ", origin_ip=:origin_ip"
            params["origin_ip"] = intersection_spec.get("origin_ip", "")
        query += " WHERE intersection_number=:orig_intersection_id"
        params["orig_intersection_id"] = intersection_spec["orig_intersection_id"]
        pgquery.write_db(query, params=params)

        # Add the intersection-to-organization relationships for the organizations to add
        if len(intersection_spec["organizations_to_add"]) > 0:
            query_rows: list[tuple[str, dict]] = []
            for index, organization in enumerate(
                intersection_spec["organizations_to_add"]
            ):
                org_placeholder = f"org_name_{index}"
                query_rows.append(
                    (
                        "("
                        "(SELECT intersection_id FROM public.intersections WHERE intersection_number = :intersection_id), "
                        f"(SELECT organization_id FROM public.organizations WHERE name = :{org_placeholder})"
                        ")",
                        {org_placeholder: organization},
                    )
                )

            query_prefix = "INSERT INTO public.intersection_organization(intersection_id, organization_id) VALUES "
            pgquery.write_db_batched(
                query_prefix,
                query_rows,
                base_params={"intersection_id": intersection_id},
            )

        # Remove the intersection-to-organization relationships for the organizations to remove
        if len(intersection_spec["organizations_to_remove"]) > 0:
            params = {"intersection_id": intersection_id}
            # Generate placeholders for each organization name
            org_placeholders = []
            for idx, org in enumerate(intersection_spec["organizations_to_remove"]):
                key = f"org_name_{idx}"
                org_placeholders.append(f":{key}")
                params[key] = org

            org_remove_query = (
                "DELETE FROM public.intersection_organization WHERE "
                "intersection_id = (SELECT intersection_id FROM public.intersections WHERE intersection_number = :intersection_id) "
                f"AND organization_id IN (SELECT organization_id FROM public.organizations WHERE name IN ({', '.join(org_placeholders)}))"
            )
            pgquery.write_db(org_remove_query, params=params)

        # Add the rsu-to-intersection relationships for the rsus to add
        if len(intersection_spec["rsus_to_add"]) > 0:
            query_rows = []
            for index, rsu_ip in enumerate(intersection_spec["rsus_to_add"]):
                ip_placeholder = f"rsu_ip_{index}"
                query_rows.append(
                    (
                        "("
                        f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :{ip_placeholder}), "
                        "(SELECT intersection_id FROM public.intersections WHERE intersection_number = :intersection_id)"
                        ")",
                        {ip_placeholder: rsu_ip},
                    )
                )

            query_prefix = (
                "INSERT INTO public.rsu_intersection(rsu_id, intersection_id) VALUES "
            )
            pgquery.write_db_batched(
                query_prefix,
                query_rows,
                base_params={"intersection_id": intersection_id},
            )

        # Remove the rsu-to-intersection relationships for the rsus to remove
        if len(intersection_spec["rsus_to_remove"]) > 0:
            params = {"intersection_id": intersection_id}
            # Generate placeholders for each rsu IP
            ip_placeholders = []
            for idx, rsu_ip in enumerate(intersection_spec["rsus_to_remove"]):
                key = f"rsu_ip_{idx}"
                ip_placeholders.append(f":{key}")
                params[key] = rsu_ip

            rsu_remove_query = (
                "DELETE FROM public.rsu_intersection WHERE "
                "intersection_id = (SELECT intersection_id FROM public.intersections WHERE intersection_number = :intersection_id) "
                f"AND rsu_id IN (SELECT rsu_id FROM public.rsus WHERE ipv4_address IN ({', '.join(ip_placeholders)}))"
            )
            pgquery.write_db(rsu_remove_query, params=params)
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

    return {"message": "Intersection successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    resource_type=RESOURCE_TYPE.INTERSECTION,
)
def delete_intersection_authorized(intersection_id: str):

    # Delete Intersection to Organization relationships
    org_remove_query = (
        "DELETE FROM public.intersection_organization WHERE "
        "intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = :intersection_id)"
    )
    pgquery.write_db(org_remove_query, params={"intersection_id": intersection_id})

    rsu_intersection_remove_query = (
        "DELETE FROM public.rsu_intersection WHERE "
        "intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = :intersection_id)"
    )
    pgquery.write_db(
        rsu_intersection_remove_query, params={"intersection_id": intersection_id}
    )

    # Delete Intersection data
    intersection_remove_query = (
        "DELETE FROM public.intersections WHERE intersection_number = :intersection_id"
    )
    pgquery.write_db(
        intersection_remove_query, params={"intersection_id": intersection_id}
    )

    return {"message": "Intersection successfully deleted"}


# REST endpoint resource class
class AdminIntersectionGetAllSchema(Schema):
    intersection_id = fields.Str(required=True)


class AdminIntersectionGetDeleteSchema(Schema):
    intersection_id = fields.Str(required=True)


class GeoPositionSchema(Schema):
    latitude = fields.Decimal(required=True)
    longitude = fields.Decimal(required=True)


class GeoPolygonSchema(Schema):
    latitude1 = fields.Decimal(required=True)
    longitude1 = fields.Decimal(required=True)
    latitude2 = fields.Decimal(required=True)
    longitude2 = fields.Decimal(required=True)


class AdminIntersectionPatchSchema(Schema):
    orig_intersection_id = fields.Integer(required=True)
    intersection_id = fields.Integer(required=True)
    ref_pt = fields.Nested(GeoPositionSchema, required=True)
    bbox = fields.Nested(GeoPolygonSchema, required=False)
    intersection_name = fields.String(required=False)
    origin_ip = fields.IPv4(required=False)
    organizations_to_add = fields.List(fields.String(), required=True)
    organizations_to_remove = fields.List(fields.String(), required=True)
    rsus_to_add = fields.List(fields.String(), required=True)
    rsus_to_remove = fields.List(fields.String(), required=True)


class AdminIntersection(Resource):
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

    @require_permission(
        required_role=ORG_ROLE_LITERAL.USER,
    )
    def get(self, permission_result: PermissionResult):
        logging.debug("AdminIntersection GET requested")

        schema = AdminIntersectionGetAllSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        # If intersection_id is "all", allow without checking for an IPv4 address
        if request.args["intersection_id"] != "all":
            schema = AdminIntersectionGetDeleteSchema()
            errors = schema.validate(request.args)
            if errors:
                logging.error(errors)
                abort(400, errors)

        return (
            get_modify_intersection_data(
                request.args["intersection_id"],
                permission_result.user,
                permission_result.qualified_orgs,
            ),
            200,
            self.headers,
        )

    @require_permission(required_role=ORG_ROLE_LITERAL.OPERATOR)
    def patch(self):
        logging.debug("AdminIntersection PATCH requested")

        # Check for main body values
        schema = AdminIntersectionPatchSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        return (
            modify_intersection_authorized(
                intersection_id=request.json.get("intersection_id"),
                intersection_spec=request.json,
            ),
            200,
            self.headers,
        )

    @require_permission(required_role=ORG_ROLE_LITERAL.OPERATOR)
    def delete(self):
        logging.debug("AdminIntersection DELETE requested")

        schema = AdminIntersectionGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        return (
            delete_intersection_authorized(request.args["intersection_id"]),
            200,
            self.headers,
        )
