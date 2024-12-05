from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import logging
import common.pgquery as pgquery
import sqlalchemy
import admin_new_intersection
import os

from common.auth_tools import (
    ORG_ROLE_LITERAL,
    RESOURCE_TYPE,
    EnvironWithOrg,
    PermissionResult,
    require_permission,
)
from common.errors import ServerErrorException, UnauthorizedException


def get_intersection_data(
    intersection_id: str, user: EnvironWithOrg, qualified_orgs: list[str]
):
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
        "LEFT JOIN public.rsus AS rsu ON rsu.rsu_id = ri.rsu_id"
    )

    where_clauses = []
    if not user.user_info.super_user:
        where_clauses.append(f"org.name IN ({', '.join(qualified_orgs)})")
    if intersection_id != "all":
        where_clauses.append(f"intersection_number = '{intersection_id}'")
    if where_clauses:
        query += " WHERE " + " AND ".join(where_clauses)
    query += ") as row"

    data = pgquery.query_db(query)

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
        if row["rsu_ip"] not in orgs and row["rsu_ip"] is not None:
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


@require_permission(
    required_role=ORG_ROLE_LITERAL.USER,
)
def get_modify_intersection_data_authorized(
    intersection_id: str, permission_result: PermissionResult
):
    modify_intersection_obj = {}
    modify_intersection_obj["intersection_data"] = get_intersection_data(
        intersection_id, permission_result.user, permission_result.qualified_orgs
    )
    if intersection_id != "all":
        modify_intersection_obj["allowed_selections"] = (
            admin_new_intersection.get_allowed_selections(permission_result.user)
        )
    return modify_intersection_obj


def enforce_modify_intersection_org_permissions(
    *,
    user: EnvironWithOrg,
    intersection_spec: dict,
):
    if not user.user_info.super_user:
        qualified_orgs = user.qualified_orgs
        unqualified_orgs = [
            org
            for org in intersection_spec.get("organizations_to_add", [])
            if org not in qualified_orgs
        ]
        if unqualified_orgs:
            raise UnauthorizedException(
                f"Unauthorized added organizations: {','.join(unqualified_orgs)}"
            )

        unqualified_orgs = [
            org
            for org in intersection_spec.get("organizations_to_remove", [])
            if org not in qualified_orgs
        ]
        if unqualified_orgs:
            raise UnauthorizedException(
                f"Unauthorized removed organizations: {','.join(unqualified_orgs)}"
            )


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    resource_type=RESOURCE_TYPE.INTERSECTION,
    additional_check=enforce_modify_intersection_org_permissions,
)
def modify_intersection_authorized(intersection_id: str, intersection_spec: dict):
    # Check for special characters for potential SQL injection
    if not admin_new_intersection.check_safe_input(intersection_spec):
        raise ServerErrorException(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    try:
        # Modify the existing Intersection data
        query = (
            "UPDATE public.intersections SET "
            f"intersection_number='{intersection_id}', "
            f"ref_pt=ST_GeomFromText('POINT({str(intersection_spec['ref_pt']['longitude'])} {str(intersection_spec['ref_pt']['latitude'])})')"
        )
        if "bbox" in intersection_spec:
            query += f", bbox=ST_MakeEnvelope({str(intersection_spec['bbox']['longitude1'])},{str(intersection_spec['bbox']['latitude1'])},{str(intersection_spec['bbox']['longitude2'])},{str(intersection_spec['bbox']['latitude2'])})"
        if "intersection_name" in intersection_spec:
            query += f", intersection_name='{intersection_spec['intersection_name']}'"
        if "origin_ip" in intersection_spec:
            query += f", origin_ip='{intersection_spec['origin_ip']}'"
        query += (
            f" WHERE intersection_number='{intersection_spec["orig_intersection_id"]}'"
        )
        pgquery.write_db(query)

        # Add the intersection-to-organization relationships for the organizations to add
        if len(intersection_spec["organizations_to_add"]) > 0:
            org_add_query = "INSERT INTO public.intersection_organization(intersection_id, organization_id) VALUES"
            for organization in intersection_spec["organizations_to_add"]:
                org_add_query += (
                    " ("
                    f"(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}'), "
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
                    "),"
                )
            org_add_query = org_add_query[:-1]
            pgquery.write_db(org_add_query)

        # Remove the intersection-to-organization relationships for the organizations to remove
        for organization in intersection_spec["organizations_to_remove"]:
            org_remove_query = (
                "DELETE FROM public.intersection_organization WHERE "
                f"intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}') "
                f"AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
            )
            pgquery.write_db(org_remove_query)

        # Add the rsu-to-intersection relationships for the rsus to add
        if len(intersection_spec["rsus_to_add"]) > 0:
            rsu_add_query = (
                "INSERT INTO public.rsu_intersection(rsu_id, intersection_id) VALUES"
            )
            for ip in intersection_spec["rsus_to_add"]:
                rsu_add_query += (
                    " ("
                    f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{ip}'), "
                    f"(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}')"
                    "),"
                )
            rsu_add_query = rsu_add_query[:-1]
            pgquery.write_db(rsu_add_query)

        # Remove the rsu-to-intersection relationships for the rsus to remove
        for ip in intersection_spec["rsus_to_remove"]:
            rsu_remove_query = (
                "DELETE FROM public.rsu_intersection WHERE "
                f"intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}') "
                f"AND rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{ip}')"
            )
            pgquery.write_db(rsu_remove_query)
    except sqlalchemy.exc.IntegrityError as e:
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        logging.error(f"Exception encountered: {failed_value}")
        raise ServerErrorException(failed_value) from e
    except ServerErrorException:
        # Re-raise ServerErrorException without catching it
        raise
    except Exception as e:
        logging.error(f"Exception encountered: {e}")
        raise ServerErrorException("Encountered unknown issue") from e

    return {"message": "Intersection successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    resource_type=RESOURCE_TYPE.INTERSECTION,
)
def delete_intersection_authorized(intersection_id: str):

    # Delete Intersection to Organization relationships
    org_remove_query = (
        "DELETE FROM public.intersection_organization WHERE "
        f"intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}')"
    )
    pgquery.write_db(org_remove_query)

    rsu_intersection_remove_query = (
        "DELETE FROM public.rsu_intersection WHERE "
        f"intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_id}')"
    )
    pgquery.write_db(rsu_intersection_remove_query)

    # Delete Intersection data
    intersection_remove_query = (
        "DELETE FROM public.intersections WHERE "
        f"intersection_number = '{intersection_id}'"
    )
    pgquery.write_db(intersection_remove_query)

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

    def get(self):
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
            get_modify_intersection_data_authorized(request.args["intersection_id"]),
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
                request.json.get("intersection_id"), request.json
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
