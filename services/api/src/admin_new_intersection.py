from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import os
from werkzeug.exceptions import InternalServerError, BadRequest, Forbidden
from common.auth_tools import (
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    get_qualified_org_list,
    get_rsu_dict_for_org,
    require_permission,
    PermissionResult,
)


def get_allowed_selections(user: EnvironWithOrg):
    """
    Retrieve the list of allowed organizations and RSUs for the given user.

    This function determines the organizations and RSUs (Roadside Units) that the user
    is authorized to access. If the user is a superuser, all organizations and RSUs are
    returned. Otherwise, only the organizations and RSUs associated with the user's
    permissions are included.

    Args:
        user (EnvironWithOrg): The user object containing organizational context and permissions.

    Returns:
        dict: A dictionary containing the allowed organizations and RSUs for the user.
              Example:
              {
                  "organizations": ["Org A", "Org B"],
                  "rsus": ["192.168.1.1", "192.168.1.2"]
              }
    """
    allowed = {}

    if user.user_info.super_user:
        organizations_query = "SELECT name FROM public.organizations ORDER BY name ASC"
        allowed["organizations"] = pgquery.query_and_return_list(organizations_query)

        rsus_query = "SELECT CAST(ipv4_address AS TEXT) FROM public.rsus ORDER BY ipv4_address ASC"
        allowed["rsus"] = pgquery.query_and_return_list(rsus_query)
    else:
        allowed["organizations"] = get_qualified_org_list(
            user, ORG_ROLE_LITERAL.OPERATOR, include_super_user=False
        )
        allowed["rsus"] = get_rsu_dict_for_org(allowed["organizations"]).keys()

    return allowed


def check_safe_input(intersection_spec):
    """
    Validate the intersection specification for unsafe characters.

    This function checks the intersection specification for special characters or sequences
    that could indicate potential SQL injection or other unsafe input. If any unsafe input
    is detected, the function returns False.

    Args:
        intersection_spec (dict): A dictionary containing the intersection specification.

    Returns:
        bool: True if the input is safe, False otherwise.
    """
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


def add_intersection(intersection_spec: dict):
    """
    Add a new intersection to the database.

    This function inserts a new intersection into the database, including its associated
    organizations and RSUs (Roadside Units). It validates the input for unsafe characters
    and handles database integrity and SQL errors.

    Args:
        intersection_spec (dict): A dictionary containing the intersection specification.
                                  Example:
                                  {
                                      "intersection_id": "123",
                                      "ref_pt": {"latitude": 40.123, "longitude": -105.456},
                                      "bbox": {
                                          "latitude1": 40.111,
                                          "longitude1": -105.444,
                                          "latitude2": 40.133,
                                          "longitude2": -105.466
                                      },
                                      "intersection_name": "Main St Intersection",
                                      "origin_ip": "192.168.1.1",
                                      "organizations": ["Org A", "Org B"],
                                      "rsus": ["192.168.1.2", "192.168.1.3"]
                                  }

    Returns:
        dict: A success message indicating the intersection was added.
              Example: {"message": "New Intersection successfully added"}

    Raises:
        BadRequest: If the input contains unsafe characters.
        InternalServerError: If a database integrity or SQL error occurs.
    """
    # Check for special characters for potential SQL injection
    if not check_safe_input(intersection_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

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

    return {"message": "New Intersection successfully added"}


# REST endpoint resource class
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


def enforce_add_intersection_org_permissions(
    user: EnvironWithOrg,
    qualified_orgs: list[str],
    intersection_spec: dict,
):
    """
    Enforce permissions for adding organizations to an intersection.

    This function ensures that a user has the necessary permissions to add organizations
    to an intersection. If the user attempts to add organizations they are not authorized
    to modify, a Forbidden exception is raised.

    Args:
        user (EnvironWithOrg): The user object containing organizational context and permissions.
        qualified_orgs (list[str]): A list of organizations the user is authorized to access.
        intersection_spec (dict): A dictionary containing the intersection specification,
                                  including the organizations to be added.

    Raises:
        Forbidden: If the user attempts to add organizations they are not authorized to modify.

    Example:
        >>> user = EnvironWithOrg(user_info={"super_user": False})
        >>> qualified_orgs = ["Org A", "Org B"]
        >>> intersection_spec = {"organizations": ["Org A", "Org C"]}
        >>> enforce_add_intersection_org_permissions(user, qualified_orgs, intersection_spec)
        Forbidden: Unauthorized added organizations: Org C
    """
    if not user.user_info.super_user:
        # Collect list of organizations the user doesn't have enough permissions to modify
        unqualified_orgs = [
            org
            for org in intersection_spec.get("organizations", [])
            if org not in qualified_orgs
        ]
        # If the user tries to add organizations they are not authorized for, raise Forbidden
        if unqualified_orgs:
            raise Forbidden(
                f"Unauthorized added organizations: {','.join(unqualified_orgs)}"
            )


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

    @require_permission(
        required_role=ORG_ROLE_LITERAL.USER,
    )
    def get(self, permission_result: PermissionResult):
        logging.debug("AdminNewIntersection GET requested")

        return (get_allowed_selections(permission_result.user), 200, self.headers)

    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
    )
    def post(self, permission_result: PermissionResult):
        logging.debug("AdminNewIntersection POST requested")
        # Check for main body values
        schema = AdminNewIntersectionSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))
        enforce_add_intersection_org_permissions(
            user=permission_result.user,
            qualified_orgs=permission_result.qualified_orgs,
            intersection_spec=request.json,
        )

        return (add_intersection(request.json), 200, self.headers)
