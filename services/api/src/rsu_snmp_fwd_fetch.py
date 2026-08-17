from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import common.snmp.rsu_message_forward_helpers as rsu_message_forward_helpers
import api_environment
import logging
from rsu_commands import fetch_rsu_info
from common.snmp.update_pg.update_rsu_message_forward import UpdatePostgresRsuMessageForward

from common.auth_tools import (
    ORG_ROLE_LITERAL,
    PermissionResult,
    require_permission,
)

from werkzeug.exceptions import InternalServerError, BadRequest, NotFound


# REST endpoint resource class and schema
class RsuSnmpFwdFetchSchema(Schema):
    rsu_ip = fields.IPv4(required=True)


class RsuSnmpFwdFetch(Resource):
    """
    Handles fetching SNMP message forwarding configurations for Roadside Units (RSUs).
    This resource provides endpoints to retrieve SNMP configurations for a specified RSU,
    ensuring proper permissions and schema validation, and interacting with the necessary
    helper services for RSU data retrieval.
    """
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    @require_permission(required_role=ORG_ROLE_LITERAL.USER)
    def get(self, permission_result: PermissionResult):
        """
        Handles the GET request for fetching SNMP configurations of a specified RSU (Roadside Unit)
        based on the RSU IP and the organization of the requesting user. This method validates the input
        parameters, fetches RSU information, and utilizes SNMP configuration methods to retrieve and
        format the required information.

        :param permission_result: An instance of `PermissionResult` containing user-related
            permissions data, used to determine the organization context for the operation.
        :return: A tuple consisting of the formatted SNMP configuration data, the HTTP status code,
            and response headers.
        """
        logging.debug("RsuSnmpFwdFetch GET requested")
        # Schema check for arguments
        schema = RsuSnmpFwdFetchSchema()
        errors = schema.validate(request.args)
        if errors:
            abort(400, str(errors))

        # Get arguments from request
        rsu_ip = request.args.get("rsu_ip")
        organization = permission_result.user.organization

        # Fetch RSU info
        rsu_info = fetch_rsu_info(rsu_ip, organization)
        if not rsu_info:
            raise NotFound(f"RSU IP {rsu_ip} not found in organization {organization}")

        # Call get_snmp_configs
        updater = UpdatePostgresRsuMessageForward()
        # get_snmp_configs expects a list of RSU dicts with specific keys.
        # fetch_rsu_info must provide at least: rsu_id, snmp_username, snmp_password, snmp_version.
        # UpdatePostgresRsuMessageForward.get_snmp_configs uses: ipv4_address, snmp_username, snmp_password, snmp_encrypt_pw, snmp_version, rsu_id.
        required_keys = ["rsu_id", "snmp_username", "snmp_password", "snmp_version"] # "snmp_encrypt_pw" is expected but appears to be optional
        missing_keys = [key for key in required_keys if not rsu_info.get(key)]
        if missing_keys:
            logging.error(
                "RSU info for IP %s is missing required fields for SNMP config fetch: %s",
                rsu_ip,
                ", ".join(missing_keys),
            )
            raise InternalServerError(f"RSU info missing required fields: {missing_keys}")
        rsu_info_copy = rsu_info.copy()
        rsu_info_copy["ipv4_address"] = rsu_ip
        
        try:
            configs = updater.get_snmp_configs([rsu_info_copy])
            rsu_configs = configs.get(rsu_info_copy["rsu_id"])

            if rsu_configs == "Unable to retrieve latest SNMP config":
                raise InternalServerError("Unable to retrieve latest SNMP config from RSU")
            if rsu_configs == "Unsupported SNMP version":
                raise BadRequest("Unsupported SNMP version for direct fetch")

            return (
                rsu_message_forward_helpers.format_snmp_msgfwd_configs(
                    rsu_configs, rsu_ip=rsu_ip
                ),
                200,
                self.headers,
            )
        except Exception as e:
            logging.exception(f"Error fetching SNMP configs: {e}")
            raise InternalServerError("Error fetching SNMP configs") from e
