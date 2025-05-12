from common.snmp.update_pg.update_rsu_message_forward import (
    UpdatePostgresRsuMessageForward,
)
import common.snmp.ntcip1218.rsu_message_forward as nticp1218_rsu_mf
import common.snmp.rsu41.rsu_message_forward as rsu41_rsu_mf
import logging
import rsu_commands


msg_type_map = {
    "bsm": {"port": "46800", "psid": "20", "tx": False, "raw": False},
    "spat": {"port": "44910", "psid": "8002", "tx": True, "raw": False},
    "map": {"port": "44920", "psid": "20", "tx": True, "raw": True},
    "ssm": {"port": "44900", "psid": "E0000015", "tx": True, "raw": True},
    "srm": {"port": "44930", "psid": "E0000016", "tx": False, "raw": True},
    "tim": {"port": "47900", "psid": "8003", "tx": True, "raw": True},
}


# Returns the appropriate snmp_walk index given add/del command
def fetch_index(command, rsu_ip, rsu_info, message_type, target_ip=None):
    snmp_creds = {
        "username": rsu_info["snmp_username"],
        "password": rsu_info["snmp_password"],
        "encrypt_pw": rsu_info["snmp_encrypt_pw"],
    }

    snmp_version = rsu_info["snmp_version"]
    if snmp_version not in ["1218", "41"]:
        logging.error("Requested SNMP standard version is not supported")
        return -1

    data, code = (
        nticp1218_rsu_mf.get(rsu_ip, snmp_creds)
        if snmp_version == "1218"
        else rsu41_rsu_mf.get(rsu_ip, snmp_creds)
    )

    if code != 200:
        return -1

    walk_result = (
        data["RsuFwdSnmpwalk"]["rsuReceivedMsgTable"]
        if snmp_version == "1218" and message_type.upper() in ["BSM", "SRM"]
        else (
            data["RsuFwdSnmpwalk"]["rsuXmitMsgFwdingTable"]
            if snmp_version == "1218"
            else data["RsuFwdSnmpwalk"]
        )
    )

    if command == "add":
        return max((int(entry) for entry in walk_result), default=0) + 1

    if command == "del" and message_type and target_ip:
        return max(
            (
                int(entry)
                for entry in walk_result
                if walk_result[entry]["Message Type"].upper() == message_type.upper()
                and walk_result[entry]["IP"] == target_ip
            ),
            default=0,
        )

    return 1


def perform_snmp_operation(command, rsu, rsu_info, snmp_creds, args, index):
    msg_type = args["msg_type"].lower()
    snmp_version = rsu_info["snmp_version"]

    if snmp_version == "1218":
        if command == "rsufwdsnmpset-del":
            return nticp1218_rsu_mf.delete(rsu, snmp_creds, msg_type, index)
        return nticp1218_rsu_mf.set(
            rsu_ip=rsu,
            snmp_creds=snmp_creds,
            dest_ip=args["dest_ip"],
            udp_port=msg_type_map[msg_type]["port"],
            rsu_index=index,
            psid=msg_type_map[msg_type]["psid"],
            security=args["security"],
            tx=msg_type_map[msg_type]["tx"],
        )
    elif snmp_version == "41":
        if command == "rsufwdsnmpset-del":
            return rsu41_rsu_mf.delete(rsu, snmp_creds, index)
        return rsu41_rsu_mf.set(
            rsu_ip=rsu,
            manufacturer=rsu_info["manufacturer"],
            snmp_creds=snmp_creds,
            dest_ip=args["dest_ip"],
            udp_port=msg_type_map[msg_type]["port"],
            rsu_index=index,
            psid=msg_type_map[msg_type]["psid"],
            raw=msg_type_map[msg_type]["raw"],
        )
    return None, 400


def execute_rsufwdsnmpset(command, organization, rsu_list, args):
    return_dict = {}
    dest_ip = args.pop("dest_ip", None) if command == "rsufwdsnmpset-del" else None

    rsu_info_list = []
    for rsu in rsu_list:
        rsu_info = rsu_commands.fetch_rsu_info(rsu, organization)
        if rsu_info is None:
            return_dict[rsu] = {
                "code": 400,
                "data": f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu}",
            }
            continue

        rsu_info_list.append(
            {
                "rsu_id": rsu_info["rsu_id"],
                "ipv4_address": rsu,
                "snmp_username": rsu_info["snmp_username"],
                "snmp_password": rsu_info["snmp_password"],
                "snmp_encrypt_pw": rsu_info["snmp_encrypt_pw"],
                "snmp_version": rsu_info["snmp_version"],
            }
        )

        # Get the index for the snmpset request
        index = fetch_index(
            "del" if command == "rsufwdsnmpset-del" else "add",
            rsu,
            rsu_info,
            args.get("msg_type"),
            dest_ip,
        )

        if index == -1:
            return_dict[rsu] = {"code": 400, "data": f"Invalid index for RSU: {rsu}"}
            continue

        snmp_creds = {
            "username": rsu_info["snmp_username"],
            "password": rsu_info["snmp_password"],
            "encrypt_pw": rsu_info["snmp_encrypt_pw"],
        }

        data, code = perform_snmp_operation(
            command, rsu, rsu_info, snmp_creds, args, index
        )
        return_dict[rsu] = {"code": code, "data": data}

    # Update PostgreSQL with the latest SNMP configs
    update_pg_rsu_mf = UpdatePostgresRsuMessageForward()
    configs = update_pg_rsu_mf.get_snmp_configs(rsu_info_list)
    update_pg_rsu_mf.update_postgresql(configs, subset=True)

    return return_dict
