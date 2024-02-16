from marshmallow import Schema, fields
import common.pgquery as pgquery
import logging
import common.rsufwdsnmpwalk as rsufwdsnmpwalk
import common.rsufwdsnmpset as rsufwdsnmpset
import rsu_upgrade
import ssh_commands
import os

# Dict of functions
command_data = {
    "rsufwdsnmpwalk": {
        "function": rsufwdsnmpwalk.get,
        "roles": ["user", "operator", "admin"],
        "ssh_required": False,
        "snmp_required": True,
    },
    "rsufwdsnmpset": {
        "function": rsufwdsnmpset.post,
        "roles": ["operator", "admin"],
        "ssh_required": True,
        "snmp_required": True,
    },
    "rsufwdsnmpset-del": {
        "function": rsufwdsnmpset.delete,
        "roles": ["operator", "admin"],
        "ssh_required": False,
        "snmp_required": True,
    },
    "reboot": {
        "function": ssh_commands.reboot,
        "roles": ["operator", "admin"],
        "ssh_required": True,
        "snmp_required": False,
    },
    "snmpfilter": {
        "function": ssh_commands.snmpfilter,
        "roles": ["operator", "admin"],
        "ssh_required": True,
        "snmp_required": False,
    },
    "upgrade-check": {
        "function": rsu_upgrade.check_for_upgrade,
        "roles": ["operator", "admin"],
        "ssh_required": False,
        "snmp_required": False,
    },
    "upgrade-rsu": {
        "function": rsu_upgrade.mark_rsu_for_upgrade,
        "roles": ["operator", "admin"],
        "ssh_required": False,
        "snmp_required": False,
    },
}


# Performs the RSU command based on the requested command
def execute_command(command, rsu_ip, args, rsu_info):
    logging.info(f"Executing command {command} on RSU {rsu_ip}")
    request_data = {
        "command": command,
        "manufacturer": rsu_info["manufacturer"],
        "rsu_ip": rsu_ip,
        "args": args,
    }

    if command_data[command]["ssh_required"]:
        request_data["creds"] = {
            "username": rsu_info["ssh_username"],
            "password": rsu_info["ssh_password"],
        }

    if command_data[command]["snmp_required"]:
        request_data["snmp_creds"] = {
            "username": rsu_info["snmp_username"],
            "password": rsu_info["snmp_password"],
            "encrypt_pw": rsu_info["snmp_encrypt_pw"],
        }
        request_data["snmp_version"] = rsu_info["snmp_version"]

    logging.debug(f"Request data: {str(request_data)}")
    return command_data[command]["function"](request_data)


# Queries for RSU manufacturer, SSH credentials, and SNMP credentials using a provided RSU IP address
def fetch_rsu_info(rsu_ip, organization):
    logging.info(f"Fetching RSU info for RSU {rsu_ip}")
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT man.name AS manufacturer_name, rcred.username AS ssh_username, rcred.password AS ssh_password, snmp.username AS snmp_username, snmp.password AS snmp_password, snmp.encrypt_password as snmp_encrypt_pw, sver.version_code AS snmp_version "
        "FROM public.rsus AS rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rd.model "
        "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer "
        "LEFT JOIN public.rsu_credentials AS rcred ON rcred.credential_id = rd.credential_id "
        "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id "
        "LEFT JOIN public.snmp_versions AS sver ON sver.snmp_version_id = rd.snmp_version_id "
        f"WHERE ron_v.name = '{organization}' AND rd.ipv4_address = '{rsu_ip}'"
        ") as row"
    )

    data = pgquery.query_db(query)
    logging.info("Parsing results...")
    if len(data) > 0:
        # Grab the first result, it should be the only result
        row = dict(data[0][0])
        rsu_info = {
            "manufacturer": row["manufacturer_name"],
            "ssh_username": row["ssh_username"],
            "ssh_password": row["ssh_password"],
            "snmp_username": row["snmp_username"],
            "snmp_password": row["snmp_password"],
            "snmp_encrypt_pw": row["snmp_encrypt_pw"],
            "snmp_version": row["snmp_version"],
        }
        return rsu_info

    logging.warning(f"RSU info cannot be found for {rsu_ip}")
    return None


# Returns the appropriate snmp_walk index given add/del command
def fetch_index(command, rsu_ip, rsu_info, message_type=None, target_ip=None):
    index = 0
    data, code = execute_command("rsufwdsnmpwalk", rsu_ip, {}, rsu_info)
    if code == 200:
        walkResult = {}
        if rsu_info["snmp_version"] == "1218":
            if message_type.upper() == "BSM" or message_type.upper() == "SSM":
                walkResult = data["RsuFwdSnmpwalk"]["rsuReceivedMsgTable"]
            else:
                walkResult = data["RsuFwdSnmpwalk"]["rsuXmitMsgFwdingTable"]
        elif rsu_info["snmp_version"] == "41":
            walkResult = data["RsuFwdSnmpwalk"]
        else:
            # SNMP version not supported
            logging.error("Requested SNMP standard version is not supported")
            return -1

        # finds the next available index
        if command == "add":
            for entry in walkResult:
                if int(entry) > index:
                    index = int(entry)
            index += 1

        # grabs the highest index matching the message type and target ip
        if command == "del" and message_type != None and target_ip != None:
            for entry in walkResult:
                if (
                    int(entry) > index
                    and walkResult[entry]["Message Type"].upper()
                    == message_type.upper()
                    and walkResult[entry]["IP"] == target_ip
                ):
                    index = int(entry)
    return index


def execute_rsufwdsnmpset(command, organization, rsu_list, args):
    return_dict = {}
    if command == "rsufwdsnmpset-del":
        dest_ip = args["dest_ip"]
        del args["dest_ip"]

    for rsu in rsu_list:
        rsu_info = fetch_rsu_info(rsu, organization)
        if rsu_info is None:
            return_dict[rsu] = {
                "code": 400,
                "data": f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu}",
            }
        else:
            # Fetch the proper index based on the provided arguments
            if command == "rsufwdsnmpset-del":
                index = fetch_index("del", rsu, rsu_info, args["msg_type"], dest_ip)
            else:
                index = fetch_index("add", rsu, rsu_info, args["msg_type"])

            if index != -1:
                args["rsu_index"] = index
                data, code = execute_command(command, rsu, args, rsu_info)
                return_dict[rsu] = {"code": code, "data": data}
            else:
                return_dict[rsu] = {
                    "code": 400,
                    "data": f"Invalid index for RSU: {rsu}",
                }
    return return_dict


def execute_upgradersu(organization, rsu_list):
    return_dict = {}
    for rsu in rsu_list:
        if fetch_rsu_info(rsu, organization) is None:
            return_dict[rsu] = {
                "code": 400,
                "data": f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu}",
            }
        else:
            json_msg, status_code = command_data["upgrade-rsu"]["function"](rsu)
            return_dict[rsu] = {"code": status_code, "data": json_msg}
    return return_dict


# Main driver function
def perform_command(command, organization, role, rsu_list, args):
    # Check if command is a known command
    if command not in command_data:
        return f"Command unknown: {command}", 400

    # Check if the user is authorized to run the command
    if role not in command_data[command]["roles"]:
        return f"Unauthorized role to run {command}", 401

    # Handle functions supporting multiple RSUs
    if command == "rsufwdsnmpset" or command == "rsufwdsnmpset-del":
        return execute_rsufwdsnmpset(command, organization, rsu_list, args), 200

    if command == "upgrade-rsu":
        return execute_upgradersu(organization, rsu_list), 200

    # Handle remaining functions with only one RSU
    rsu_ip = rsu_list[0]
    rsu_info = fetch_rsu_info(rsu_ip, organization)
    if rsu_info is None:
        return (
            f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu_ip}",
            500,
        )

    if command == "upgrade-check":
        return command_data[command]["function"](rsu_ip), 200

    return execute_command(command, rsu_ip, args, rsu_info)


# REST endpoint resource class and schema
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields


class RsuCommandRequestSchema(Schema):
    command = fields.Str(required=True)
    rsu_ip = fields.List(fields.IPv4(required=True))
    args = fields.Dict(required=True)


class RsuCommandRequest(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
        "Access-Control-Allow-Methods": "GET,POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {"Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"]}

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def get(self):
        logging.debug("RsuCommandRequest GET requested")
        return self.universal()

    def post(self):
        logging.debug("RsuCommandRequest POST requested")
        return self.universal()

    def universal(self):
        schema = RsuCommandRequestSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        data, code = perform_command(
            request.json["command"],
            request.environ["organization"],
            request.environ["role"],
            request.json["rsu_ip"],
            request.json["args"],
        )
        return (data, code, self.headers)
