from typing import Any
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import admin_new_rsu
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
    params: dict[str, Any] = {}
    if not user.user_info.super_user:
        org_names_placeholder, _ = generate_sql_placeholders_for_list(
            qualified_orgs, params_to_update=params
        )
        where_clauses.append(f"org.name IN ({org_names_placeholder})")
    if rsu_ip != "all":
        where_clauses.append("ipv4_address = :rsu_ip")
        params["rsu_ip"] = rsu_ip
    if where_clauses:
        query += " WHERE " + " AND ".join(where_clauses)
    query += ") as row"

    data = pgquery.query_db(query, params=params)

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
    modify_rsu_obj["rsu_data"] = get_rsu_data(
        rsu_ip, permission_result.user, permission_result.qualified_orgs
    )
    if rsu_ip != "all":
        modify_rsu_obj["allowed_selections"] = admin_new_rsu.get_allowed_selections(
            permission_result.user
        )
    return modify_rsu_obj


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.RSU
)
def modify_rsu_authorized(
    permission_result: PermissionResult, orig_ip: str, rsu_spec: dict
):
    enforce_organization_restrictions(
        user=permission_result.user,
        qualified_orgs=permission_result.qualified_orgs,
        spec=rsu_spec,
        keys_to_check=["organizations_to_add", "organizations_to_remove"],
    )

    # Check for special characters for potential SQL injection
    if not admin_new_rsu.check_safe_input(rsu_spec):
        raise BadRequest(
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
            "geography=ST_GeomFromText('POINT(' || :geo_position_longitude || ' ' || :geo_position_latitude || ')'), "
            "milepost=:milepost, "
            "ipv4_address=:rsu_ip, "
            "serial_number=:serial_number, "
            "primary_route=:primary_route, "
            "model=(SELECT rsu_model_id FROM public.rsu_models WHERE name = :model), "
            "credential_id=(SELECT credential_id FROM public.rsu_credentials WHERE nickname = :ssh_credential_group), "
            "snmp_credential_id=(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = :snmp_credential_group), "
            "snmp_protocol_id=(SELECT snmp_protocol_id FROM public.snmp_protocols WHERE nickname = :snmp_version_group), "
            "iss_scms_id=:scms_id "
            "WHERE ipv4_address=:orig_ip"
        )
        params = {
            "rsu_ip": rsu_ip,
            "geo_position_longitude": rsu_spec["geo_position"]["longitude"],
            "geo_position_latitude": rsu_spec["geo_position"]["latitude"],
            "milepost": rsu_spec["milepost"],
            "serial_number": rsu_spec["serial_number"],
            "primary_route": rsu_spec["primary_route"],
            "model": model,
            "ssh_credential_group": rsu_spec["ssh_credential_group"],
            "snmp_credential_group": rsu_spec["snmp_credential_group"],
            "snmp_version_group": rsu_spec["snmp_version_group"],
            "scms_id": rsu_spec["scms_id"],
            "orig_ip": orig_ip,
        }
        pgquery.write_db(query, params=params)

        # Add the rsu-to-organization relationships for the organizations to add
        if len(rsu_spec["organizations_to_add"]) > 0:
            query_rows = []
            params = {"rsu_ip": rsu_ip}
            for index, organization in enumerate(rsu_spec["organizations_to_add"]):
                org_placeholder = f"org_name_{index}"
                query_rows.append(
                    "("
                    "(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :rsu_ip), "
                    f"(SELECT organization_id FROM public.organizations WHERE name = :{org_placeholder})"
                    ")"
                )
                params[org_placeholder] = organization

            org_add_query = (
                "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES "
                + ", ".join(query_rows)
            )
            pgquery.write_db(org_add_query, params=params)

        # Remove the rsu-to-organization relationships for the organizations to remove
        if len(rsu_spec["organizations_to_remove"]) > 0:
            params = {"rsu_ip": rsu_ip}
            # Generate placeholders for each organization name
            org_placeholders = []
            for idx, org in enumerate(rsu_spec["organizations_to_remove"]):
                key = f"org_name_{idx}"
                org_placeholders.append(f":{key}")
                params[key] = org

            org_remove_query = (
                "DELETE FROM public.rsu_organization WHERE "
                "rsu_id = (SELECT rsu_id FROM public.rsus WHERE ipv4_address = :rsu_ip) "
                f"AND organization_id IN (SELECT organization_id FROM public.organizations WHERE name IN ({', '.join(org_placeholders)}))"
            )
            pgquery.write_db(org_remove_query, params=params)
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

    return {"message": "RSU successfully modified"}


@require_permission(
    required_role=ORG_ROLE_LITERAL.OPERATOR,
    resource_type=RESOURCE_TYPE.RSU,
)
def delete_rsu_authorized(rsu_ip: str):
    # Delete RSU to Organization relationships
    org_remove_query = (
        "DELETE FROM public.rsu_organization WHERE "
        "rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :rsu_ip)"
    )
    pgquery.write_db(org_remove_query, params={"rsu_ip": rsu_ip})

    # Delete recorded RSU ping data
    ping_remove_query = (
        "DELETE FROM public.ping WHERE "
        "rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :rsu_ip)"
    )
    pgquery.write_db(ping_remove_query, params={"rsu_ip": rsu_ip})

    # Delete recorded RSU SCMS health data
    scms_remove_query = (
        "DELETE FROM public.scms_health WHERE "
        "rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :rsu_ip)"
    )
    pgquery.write_db(scms_remove_query, params={"rsu_ip": rsu_ip})

    # Delete snmp message forward config data
    msg_config_remove_query = (
        "DELETE FROM public.snmp_msgfwd_config WHERE "
        "rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = :rsu_ip)"
    )
    pgquery.write_db(msg_config_remove_query, params={"rsu_ip": rsu_ip})

    # Delete RSU data
    rsu_remove_query = "DELETE FROM public.rsus WHERE ipv4_address = :rsu_ip"
    pgquery.write_db(rsu_remove_query, params={"rsu_ip": rsu_ip})

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

    @require_permission(required_role=ORG_ROLE_LITERAL.USER)
    def get(self):
        logging.debug("AdminRsu GET requested")

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

    @require_permission(required_role=ORG_ROLE_LITERAL.OPERATOR)
    def patch(self):
        logging.debug("AdminRsu PATCH requested")

        # Check for main body values
        schema = AdminRsuPatchSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        return (
            modify_rsu_authorized(
                orig_ip=request.json["orig_ip"], rsu_spec=request.json
            ),
            200,
            self.headers,
        )

    @require_permission(required_role=ORG_ROLE_LITERAL.OPERATOR)
    def delete(self):
        logging.debug("AdminRsu DELETE requested")
        schema = AdminRsuGetDeleteSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        return (delete_rsu_authorized(request.args["rsu_ip"]), 200, self.headers)
