from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import logging
import common.pgquery as pgquery
import sqlalchemy
import admin_new_rsu
import os

from common.errors import ServerErrorException, UnauthorizedException
from common.auth_tools import (
    ENVIRON_USER_KEY,
    ORG_ROLE_LITERAL,
    RESOURCE_TYPE,
    EnvironWithOrg,
    PermissionResult,
    check_role_above,
    require_permission,
)


def get_rsu_data(rsu_ip: str, user: EnvironWithOrg, qualified_orgs: list[str]):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT ipv4_address, ST_X(geography::geometry) AS longitude, ST_Y(geography::geometry) AS latitude, "
        "milepost, primary_route, serial_number, iss_scms_id, concat(man.name, ' ',rm.name) AS model, "
        "rsu_cred.nickname AS ssh_credential, snmp_cred.nickname AS snmp_credential, snmp_ver.nickname AS snmp_version, org.name AS org_name "
        "FROM public.rsus "
        "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rsus.model "
        "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer "
        "JOIN public.rsu_credentials AS rsu_cred ON rsu_cred.credential_id = rsus.credential_id "
        "JOIN public.snmp_credentials AS snmp_cred ON snmp_cred.snmp_credential_id = rsus.snmp_credential_id "
        "JOIN public.snmp_protocols AS snmp_ver ON snmp_ver.snmp_protocol_id = rsus.snmp_protocol_id "
        "JOIN public.rsu_organization AS ro ON ro.rsu_id = rsus.rsu_id  "
        "JOIN public.organizations AS org ON org.organization_id = ro.organization_id"
    )

    where_clauses = []
    if not user.user_info.super_user:
        where_clauses.append(
            f"org.name = ANY (ARRAY[{', '.join(f"'{org}'" for org in qualified_orgs)}])"
        )
    if rsu_ip != "all":
        where_clauses.append(f"ipv4_address = '{rsu_ip}'")
    if where_clauses:
        query += " WHERE " + " AND ".join(where_clauses)
    query += ") as row"

    data = pgquery.query_db(query)

    rsu_dict = {}
    for row in data:
        row = dict(row[0])
        if str(row["ipv4_address"]) not in rsu_dict:
            rsu_dict[str(row["ipv4_address"])] = {
                "ip": str(row["ipv4_address"]),
                "geo_position": {
                    "latitude": row["latitude"],
                    "longitude": row["longitude"],
                },
                "milepost": row["milepost"],
                "primary_route": row["primary_route"],
                "serial_number": row["serial_number"],
                "scms_id": row["iss_scms_id"],
                "model": row["model"],
                "ssh_credential_group": row["ssh_credential"],
                "snmp_credential_group": row["snmp_credential"],
                "snmp_version_group": row["snmp_version"],
                "organizations": [],
            }
        rsu_dict[str(row["ipv4_address"])]["organizations"].append(row["org_name"])

    rsu_list = list(rsu_dict.values())
    # If list is empty and a single RSU was requested, return empty object
    if len(rsu_list) == 0 and rsu_ip != "all":
        return {}
    # If list is not empty and a single RSU was requested, return the first index of the list
    elif len(rsu_list) == 1 and rsu_ip != "all":
        return rsu_list[0]
    else:
        return rsu_list


@require_permission(
    required_role=ORG_ROLE_LITERAL.USER,
    resource_type=RESOURCE_TYPE.RSU,
)
def get_modify_rsu_data_authorized(rsu_ip: str, permission_result: PermissionResult):
    modify_rsu_obj = {}
    modify_rsu_obj["rsu_data"] = get_rsu_data(rsu_ip, permission_result)
    if rsu_ip != "all":
        modify_rsu_obj["allowed_selections"] = admin_new_rsu.get_allowed_selections(
            permission_result
        )
    return modify_rsu_obj


def enforce_modify_rsu_org_permissions(
    *,
    user: EnvironWithOrg,
    rsu_spec: dict,
):
    if not user.user_info.super_user:
        qualified_orgs = user.qualified_orgs
        unqualified_orgs = [
            org for org in rsu_spec["organizations_to_add"] if org not in qualified_orgs
        ]
        if unqualified_orgs:
            raise UnauthorizedException(
                f"Unauthorized added organizations: {','.join(unqualified_orgs)}"
            )

        unqualified_orgs = [
            org
            for org in rsu_spec["organizations_to_remove"]
            if org not in qualified_orgs
        ]
        if unqualified_orgs:
            raise UnauthorizedException(
                f"Unauthorized removed organizations: {','.join(unqualified_orgs)}"
            )


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    resource_type=RESOURCE_TYPE.RSU,
    additional_check=enforce_modify_rsu_org_permissions,
)
def modify_rsu_authorized(orig_ip: str, rsu_spec: dict):
    # Check for special characters for potential SQL injection
    if not admin_new_rsu.check_safe_input(rsu_spec):
        raise ServerErrorException(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    # Parse model out of the "Manufacturer Model" string
    space_index = rsu_spec["model"].find(" ")
    model = rsu_spec["model"][(space_index + 1) :]
    rsu_ip = rsu_spec["ip"]

    try:
        # Modify the existing RSU data
        query = (
            "UPDATE public.rsus SET "
            f"geography=ST_GeomFromText('POINT({str(rsu_spec['geo_position']['longitude'])} {str(rsu_spec['geo_position']['latitude'])})'), "
            f"milepost={str(rsu_spec['milepost'])}, "
            f"ipv4_address='{rsu_ip}', "
            f"serial_number='{rsu_spec['serial_number']}', "
            f"primary_route='{rsu_spec['primary_route']}', "
            f"model=(SELECT rsu_model_id FROM public.rsu_models WHERE name = '{model}'), "
            f"credential_id=(SELECT credential_id FROM public.rsu_credentials WHERE nickname = '{rsu_spec['ssh_credential_group']}'), "
            f"snmp_credential_id=(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = '{rsu_spec['snmp_credential_group']}'), "
            f"snmp_protocol_id=(SELECT snmp_protocol_id FROM public.snmp_protocols WHERE nickname = '{rsu_spec['snmp_version_group']}'), "
            f"iss_scms_id='{rsu_spec['scms_id']}' "
            f"WHERE ipv4_address='{orig_ip}'"
        )
        pgquery.write_db(query)

        # Add the rsu-to-organization relationships for the organizations to add
        if len(rsu_spec["organizations_to_add"]) > 0:
            org_add_query = (
                "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES"
            )
            for organization in rsu_spec["organizations_to_add"]:
                org_add_query += (
                    " ("
                    f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}'), "
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
                    "),"
                )
            org_add_query = org_add_query[:-1]
            pgquery.write_db(org_add_query)

        # Remove the rsu-to-organization relationships for the organizations to remove
        for organization in rsu_spec["organizations_to_remove"]:
            org_remove_query = (
                "DELETE FROM public.rsu_organization WHERE "
                f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}') "
                f"AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
            )
            pgquery.write_db(org_remove_query)
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

    return {"message": "RSU successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    resource_type=RESOURCE_TYPE.RSU,
)
def delete_rsu_authorized(rsu_ip: str):
    # Delete RSU to Organization relationships
    org_remove_query = (
        "DELETE FROM public.rsu_organization WHERE "
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
    )
    pgquery.write_db(org_remove_query)

    # Delete recorded RSU ping data
    ping_remove_query = (
        "DELETE FROM public.ping WHERE "
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
    )
    pgquery.write_db(ping_remove_query)

    # Delete recorded RSU SCMS health data
    scms_remove_query = (
        "DELETE FROM public.scms_health WHERE "
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
    )
    pgquery.write_db(scms_remove_query)

    # Delete snmp message forward config data
    msg_config_remove_query = (
        "DELETE FROM public.snmp_msgfwd_config WHERE "
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
    )
    pgquery.write_db(msg_config_remove_query)

    # Delete RSU data
    rsu_remove_query = "DELETE FROM public.rsus WHERE " f"ipv4_address = '{rsu_ip}'"
    pgquery.write_db(rsu_remove_query)

    return {"message": "RSU successfully deleted"}


# REST endpoint resource class
class AdminRsuGetAllSchema(Schema):
    rsu_ip = fields.Str(required=True)


class AdminRsuGetDeleteSchema(Schema):
    rsu_ip = fields.IPv4(required=True)


class GeoPositionSchema(Schema):
    latitude = fields.Decimal(required=True)
    longitude = fields.Decimal(required=True)


class AdminRsuPatchSchema(Schema):
    orig_ip = fields.IPv4(required=True)
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
    organizations_to_add = fields.List(fields.String(), required=True)
    organizations_to_remove = fields.List(fields.String(), required=True)


class AdminRsu(Resource):
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
        logging.debug("AdminRsu GET requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]

        schema = AdminRsuGetAllSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        # If rsu_ip is "all", allow without checking for an IPv4 address
        if request.args["rsu_ip"] != "all":
            schema = AdminRsuGetDeleteSchema()
            errors = schema.validate(request.args)
            if errors:
                logging.error(errors)
                abort(400, errors)

        return (
            get_modify_rsu_data_authorized(request.args["rsu_ip"]),
            200,
            self.headers,
        )

    def patch(self):
        logging.debug("AdminRsu PATCH requested")
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
        schema = AdminRsuPatchSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        return (
            modify_rsu_authorized(request.json["orig_ip"], request.json),
            200,
            self.headers,
        )

    def delete(self):
        logging.debug("AdminRsu DELETE requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]
        schema = AdminRsuGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

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

        return (delete_rsu_authorized(request.args["rsu_ip"], user), 200, self.headers)
