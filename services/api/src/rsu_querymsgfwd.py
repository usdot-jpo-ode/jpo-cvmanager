import common.pgquery as pgquery
import common.snmpwalk_helpers as snmpwalk_helpers
import common.util as util
import os
import logging


def query_snmp_msgfwd(rsu_ip, organization):
    logging.info(f"Preparing to query for all RSU IPs for {organization}...")

    # Execute the query and fetch all results
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT smt.name msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active, security "
        "FROM public.snmp_msgfwd_config smc "
        "JOIN public.snmp_msgfwd_type smt ON smc.msgfwd_type = smt.snmp_msgfwd_type_id "
        "JOIN ("
        "SELECT rd.rsu_id, rd.ipv4_address "
        "FROM public.rsus rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{organization}'"
        ") rdo ON smc.rsu_id = rdo.rsu_id "
        f"WHERE rdo.ipv4_address = '{rsu_ip}' "
        "ORDER BY smt.name, snmp_index ASC"
        ") as row"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    msgfwd_configs_dict = {}
    for row in data:
        row = dict(row[0])

        config_row = {
            "Message Type": row["message_type"].upper(),
            "IP": row["dest_ipv4"],
            "Port": row["dest_port"],
            "Start DateTime": util.format_date_denver_iso(row["start_datetime"]),
            "End DateTime": util.format_date_denver_iso(row["end_datetime"]),
            "Config Active": snmpwalk_helpers.active(row["active"]),
            "Full WSMP": snmpwalk_helpers.active(row["security"]),
        }

        # Based on the value of msgfwd_type, store the configuration data to match the response object of rsufwdsnmpwalk
        if row["msgfwd_type"] == "rsuDsrcFwd":
            msgfwd_configs_dict[row["snmp_index"]] = config_row
        elif row["msgfwd_type"] == "rsuReceivedMsg":
            if "rsuReceivedMsgTable" not in msgfwd_configs_dict:
                msgfwd_configs_dict["rsuReceivedMsgTable"] = {}
            msgfwd_configs_dict["rsuReceivedMsgTable"][row["snmp_index"]] = config_row
        elif row["msgfwd_type"] == "rsuXmitMsgFwding":
            if "rsuXmitMsgFwdingTable" not in msgfwd_configs_dict:
                msgfwd_configs_dict["rsuXmitMsgFwdingTable"] = {}
            msgfwd_configs_dict["rsuXmitMsgFwdingTable"][row["snmp_index"]] = config_row
        else:
            # changed the double quotes around msgfwd_type to single quotes to allow for vscode debugging to work properly
            logging.warn(
                f"Encountered unknown message forwarding configuration type '{row['msgfwd_type']}' for RSU '{rsu_ip}'"
            )

    # Make sure both RX and TX objects are available if the RSU ends up having NTCIP 1218 configurations
    if (
        "rsuReceivedMsgTable" in msgfwd_configs_dict
        and "rsuXmitMsgFwdingTable" not in msgfwd_configs_dict
    ):
        msgfwd_configs_dict["rsuXmitMsgFwdingTable"] = {}
    elif (
        "rsuXmitMsgFwdingTable" in msgfwd_configs_dict
        and "rsuReceivedMsgTable" not in msgfwd_configs_dict
    ):
        msgfwd_configs_dict["rsuReceivedMsgTable"] = {}

    return {"RsuFwdSnmpwalk": msgfwd_configs_dict}, 200


# REST endpoint resource class and schema
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields


class RsuQueryMsgFwdSchema(Schema):
    rsu_ip = fields.IPv4(required=True)


class RsuQueryMsgFwd(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
        "Access-Control-Allow-Methods": "GET",
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
        logging.debug("RsuQueryMsgFwd GET requested")
        # Schema check for arguments
        schema = RsuQueryMsgFwdSchema()
        errors = schema.validate(request.args)
        if errors:
            abort(400, str(errors))
        # Get arguments from request and set defaults if not provided
        rsu_ip = request.args.get("rsu_ip")

        data, code = query_snmp_msgfwd(rsu_ip, request.environ["organization"])

        return (data, code, self.headers)
