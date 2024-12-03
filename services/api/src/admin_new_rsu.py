from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate
import logging
import common.pgquery as pgquery
import sqlalchemy
import os

from common.auth_tools import (
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    PermissionResult,
    get_qualified_org_list,
    require_permission,
)
from api.src.errors import ServerErrorException, UnauthorizedException


def get_allowed_selections(permission_result: PermissionResult):
    allowed = {}

    primary_routes_query = (
        "SELECT DISTINCT primary_route FROM public.rsus ORDER BY primary_route ASC"
    )
    rsu_models_query = (
        "SELECT manufacturers.name as manufacturer, rsu_models.name as model "
        "FROM public.rsu_models "
        "JOIN public.manufacturers ON rsu_models.manufacturer = manufacturers.manufacturer_id "
        "ORDER BY manufacturer, model ASC"
    )
    ssh_credential_nicknames_query = (
        "SELECT nickname FROM public.rsu_credentials ORDER BY nickname ASC"
    )
    snmp_credential_nicknames_query = (
        "SELECT nickname FROM public.snmp_credentials ORDER BY nickname ASC"
    )
    snmp_version_nicknames_query = (
        "SELECT nickname FROM public.snmp_protocols ORDER BY nickname ASC"
    )

    allowed["primary_routes"] = pgquery.query_and_return_list(primary_routes_query)
    allowed["rsu_models"] = pgquery.query_and_return_list(rsu_models_query)
    allowed["ssh_credential_groups"] = pgquery.query_and_return_list(
        ssh_credential_nicknames_query
    )
    allowed["snmp_credential_groups"] = pgquery.query_and_return_list(
        snmp_credential_nicknames_query
    )
    allowed["snmp_version_groups"] = pgquery.query_and_return_list(
        snmp_version_nicknames_query
    )

    allowed["organizations"] = get_qualified_org_list(
        permission_result.user, ORG_ROLE_LITERAL.OPERATOR, include_super_user=True
    )

    return allowed


def check_safe_input(rsu_spec):
    special_characters = "!\"#$%&'()*+,./:;<=>?@[\\]^`{|}~"
    # Check all string based fields for special characters
    if (
        any(c in special_characters for c in rsu_spec["primary_route"])
        or "--" in rsu_spec["primary_route"]
    ):
        return False
    if (
        any(c in special_characters for c in rsu_spec["model"])
        or "--" in rsu_spec["model"]
    ):
        return False
    if (
        any(c in special_characters for c in rsu_spec["serial_number"])
        or "--" in rsu_spec["serial_number"]
    ):
        return False
    if (
        any(c in special_characters for c in rsu_spec["scms_id"])
        or "--" in rsu_spec["scms_id"]
    ):
        return False
    if (
        any(c in special_characters for c in rsu_spec["ssh_credential_group"])
        or "--" in rsu_spec["ssh_credential_group"]
    ):
        return False
    if (
        any(c in special_characters for c in rsu_spec["snmp_credential_group"])
        or "--" in rsu_spec["snmp_credential_group"]
    ):
        return False
    return True


def enforce_add_rsu_org_permissions(
    *,
    user: EnvironWithOrg,
    rsu_spec: dict,
):
    if not user.user_info.super_user:
        qualified_orgs = user.qualified_orgs
        unqualified_orgs = [
            org
            for org in rsu_spec.get("organizations", [])
            if org not in qualified_orgs
        ]
        if unqualified_orgs:
            raise UnauthorizedException(
                f"Unauthorized added organizations: {','.join(unqualified_orgs)}"
            )


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    additional_check=enforce_add_rsu_org_permissions,
)
def add_rsu_authorized(rsu_spec):
    # Check for special characters for potential SQL injection
    if not check_safe_input(rsu_spec):
        raise ServerErrorException(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    # Parse model out of the "Manufacturer Model" string
    space_index = rsu_spec["model"].find(" ")
    manufacturer = rsu_spec["model"][:space_index]
    model = rsu_spec["model"][(space_index + 1) :]

    # If RSU is a Commsignia or Kapsch, use the serial number for the SCMS ID
    scms_id = rsu_spec["scms_id"]
    if manufacturer == "Commsignia" or manufacturer == "Kapsch":
        scms_id = rsu_spec["serial_number"]
    else:
        if scms_id == "":
            raise ServerErrorException("SCMS ID must be specified")

    try:
        query = (
            "INSERT INTO public.rsus(geography, milepost, ipv4_address, serial_number, primary_route, model, credential_id, snmp_credential_id, snmp_protocol_id, iss_scms_id) "
            "VALUES ("
            f"ST_GeomFromText('POINT({str(rsu_spec['geo_position']['longitude'])} {str(rsu_spec['geo_position']['latitude'])})'), "
            f"{str(rsu_spec['milepost'])}, "
            f"'{rsu_spec['ip']}', "
            f"'{rsu_spec['serial_number']}', "
            f"'{rsu_spec['primary_route']}', "
            f"(SELECT rsu_model_id FROM public.rsu_models WHERE name = '{model}'), "
            f"(SELECT credential_id FROM public.rsu_credentials WHERE nickname = '{rsu_spec['ssh_credential_group']}'), "
            f"(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = '{rsu_spec['snmp_credential_group']}'), "
            f"(SELECT snmp_protocol_id FROM public.snmp_protocols WHERE nickname = '{rsu_spec['snmp_version_group']}'), "
            f"'{scms_id}'"
            ")"
        )
        pgquery.write_db(query)

        org_query = (
            "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES"
        )
        for organization in rsu_spec["organizations"]:
            org_query += (
                " ("
                f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_spec['ip']}'), "
                f"(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
                "),"
            )
        org_query = org_query[:-1]
        pgquery.write_db(org_query)
    except sqlalchemy.exc.IntegrityError as e:
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        logging.error(f"Exception encountered: {failed_value}")
        raise ServerErrorException(failed_value)
    except Exception as e:
        logging.error(f"Exception encountered: {e}")
        raise ServerErrorException("Encountered unknown issue")

    return {"message": "New RSU successfully added"}


# REST endpoint resource class
class GeoPositionSchema(Schema):
    latitude = fields.Decimal(required=True)
    longitude = fields.Decimal(required=True)


class AdminNewRsuSchema(Schema):
    ip = fields.IPv4(required=True)
    geo_position = fields.Nested(GeoPositionSchema, required=True)
    milepost = fields.Decimal(required=True)
    primary_route = fields.Str(required=True)
    serial_number = fields.Str(required=True)
    model = fields.Str(required=True)
    scms_id = fields.Str(required=True)
    ssh_credential_group = fields.Str(required=True)
    snmp_credential_group = fields.Str(required=True)
    snmp_version_group = fields.Str(required=True)
    organizations = fields.List(
        fields.String(), required=True, validate=validate.Length(min=1)
    )


class AdminNewRsu(Resource):
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
        logging.debug("AdminNewRsu GET requested")
        return (
            get_allowed_selections(),
            200,
            self.headers,
        )

    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
    )
    def post(self):
        logging.debug("AdminNewRsu POST requested")
        # Check for main body values
        schema = AdminNewRsuSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        return (add_rsu_authorized(request.json), 200, self.headers)
