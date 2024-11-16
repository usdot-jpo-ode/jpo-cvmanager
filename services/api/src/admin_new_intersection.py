import logging
import common.pgquery as pgquery
import sqlalchemy
import os

from services.api.src.auth_tools import (
    ENVIRON_USER_KEY,
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    check_intersection_with_org,
    check_role_above,
    get_qualified_org_list,
    get_rsu_dict_for_org,
)


def query_and_return_list(query):
    data = pgquery.query_db(query)
    return_list = []
    for row in data:
        return_list.append(" ".join(row))
    return return_list


def get_allowed_selections(user: EnvironWithOrg):
    allowed = {}

    allowed["organizations"] = get_qualified_org_list(user, ORG_ROLE_LITERAL.OPERATOR)

    allowed["rsus"] = get_rsu_dict_for_org(allowed["organizations"]).keys()

    return allowed


def check_safe_input(intersection_spec):
    special_characters = "!\"#$%&'()*+,./:;<=>?@[\\]^`{|}~"
    unchecked_fields = [
        "origin_ip",
        "rsus",
        "rsus_to_add",
        "rsus_to_remove",
        "latitude",
        "longitude",
        "latitude1",
        "longitude1",
        "latitude2",
        "longitude2",
    ]
    for k, value in intersection_spec.items():
        if isinstance(value, dict):
            if not check_safe_input(value):
                return False
        elif isinstance(value, list):
            if not all(check_safe_input({k: v}) for v in value):
                return False
        else:
            if (k in unchecked_fields) or (value is None):
                continue
            if any(c in special_characters for c in str(value)) or "--" in str(value):
                return False
    return True


def add_intersection(intersection_spec, user: EnvironWithOrg):
    # Check for special characters for potential SQL injection
    if not check_safe_input(intersection_spec):
        return {
            "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        }, 500

    if not user.user_info.super_user:
        qualified_orgs = get_qualified_org_list(user, ORG_ROLE_LITERAL.OPERATOR)
        unqualified_orgs = [
            org
            for org in intersection_spec["organizations"]
            if org not in qualified_orgs
        ]
        if unqualified_orgs:
            return {
                "message": f"Unauthorized organizations: {','.join(unqualified_orgs)}"
            }, 403

        qualified_rsus = get_rsu_dict_for_org(qualified_orgs).keys()
        unqualified_rsus = [
            rsu for rsu in intersection_spec["rsus"] if rsu not in qualified_rsus
        ]
        if unqualified_rsus:
            return {"message": f"Unauthorized rsus: {','.join(unqualified_rsus)}"}, 403

    try:
        query = "INSERT INTO public.intersections(intersection_number, ref_pt"

        # Add optional fields if they are present
        if "bbox" in intersection_spec:
            query += ", bbox"
        if "intersection_name" in intersection_spec:
            query += ", intersection_name"
        if "origin_ip" in intersection_spec:
            query += ", origin_ip"

        # Close the column list and start the VALUES clause
        query += ") VALUES ("

        # Add the mandatory fields
        query += (
            f"'{intersection_spec['intersection_id']}', "
            f"ST_GeomFromText('POINT({str(intersection_spec['ref_pt']['longitude'])} {str(intersection_spec['ref_pt']['latitude'])})')"
        )

        # Add optional values if they are present
        if "bbox" in intersection_spec:
            query += (
                f", ST_MakeEnvelope({str(intersection_spec['bbox']['longitude1'])},"
                f"{str(intersection_spec['bbox']['latitude1'])},"
                f"{str(intersection_spec['bbox']['longitude2'])},"
                f"{str(intersection_spec['bbox']['latitude2'])})"
            )
        if "intersection_name" in intersection_spec:
            query += f", '{intersection_spec['intersection_name']}'"
        if "origin_ip" in intersection_spec:
            query += f", '{intersection_spec['origin_ip']}'"

        # Close the VALUES clause
        query += ")"
        pgquery.write_db(query)

        org_query = "INSERT INTO public.intersection_organization(intersection_id, organization_id) VALUES"
        for organization in intersection_spec["organizations"]:
            org_query += (
                " ("
                f"(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_spec['intersection_id']}'), "
                f"(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
                "),"
            )
        org_query = org_query[:-1]
        pgquery.write_db(org_query)
        if intersection_spec["rsus"]:
            rsu_intersection_query = (
                "INSERT INTO public.rsu_intersection(rsu_id, intersection_id) VALUES"
            )
            for rsu_ip in intersection_spec["rsus"]:
                rsu_intersection_query += (
                    " ("
                    f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}'), "
                    f"(SELECT intersection_id FROM public.intersections WHERE intersection_number = '{intersection_spec['intersection_id']}')"
                    "),"
                )
            rsu_intersection_query = rsu_intersection_query[:-1]
            pgquery.write_db(rsu_intersection_query)
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

    return {"message": "New Intersection successfully added"}, 200


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate


class GeoPositionSchema(Schema):
    latitude = fields.Decimal(required=True)
    longitude = fields.Decimal(required=True)


class GeoPolygonSchema(Schema):
    latitude1 = fields.Decimal(required=True)
    longitude1 = fields.Decimal(required=True)
    latitude2 = fields.Decimal(required=True)
    longitude2 = fields.Decimal(required=True)


class AdminNewIntersectionSchema(Schema):
    intersection_id = fields.Integer(required=True)
    ref_pt = fields.Nested(GeoPositionSchema, required=True)
    organizations = fields.List(
        fields.String(), required=True, validate=validate.Length(min=1)
    )
    bbox = fields.Nested(GeoPolygonSchema, required=False)
    intersection_name = fields.String(required=False)
    origin_ip = fields.IPv4(required=False)
    rsus = fields.List(fields.IPv4(), required=True)


class AdminNewIntersection(Resource):
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
        logging.debug("AdminNewIntersection GET requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]

        return (get_allowed_selections(user), 200, self.headers)

    def post(self):
        logging.debug("AdminNewIntersection POST requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]
        if not user.user_info.super_user and not check_role_above(
            user.role, ORG_ROLE_LITERAL.OPERATOR
        ):
            return (
                {
                    "Message": "Unauthorized, requires at least super_user or organization operator role"
                },
                403,
                self.headers,
            )
        # Check for main body values
        schema = AdminNewIntersectionSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        data, code = add_intersection(request.json)
        return (data, code, self.headers)
