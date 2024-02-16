import os
import logging
import common.pgquery as pgquery
import common.rsufwdsnmpwalk as rsufwdsnmpwalk
from datetime import datetime


def insert_config_list(snmp_config_list):
    query = (
        "INSERT INTO public.snmp_msgfwd_config("
        "rsu_id, msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active) "
        "VALUES"
    )

    for snmp_config in snmp_config_list:
        query += (
            f" ({snmp_config['rsu_id']}, {snmp_config['msgfwd_type']}, {snmp_config['snmp_index']}, "
            f"'{snmp_config['message_type']}', '{snmp_config['dest_ipv4']}', {snmp_config['dest_port']}, "
            f"'{snmp_config['start_datetime']}', '{snmp_config['end_datetime']}', '{snmp_config['active']}'),"
        )

    pgquery.write_db(query[:-1])


def delete_config_list(snmp_config_list):
    for snmp_config in snmp_config_list:
        query = (
            "DELETE FROM public.snmp_msgfwd_config "
            f"WHERE rsu_id={snmp_config['rsu_id']} AND msgfwd_type={snmp_config['msgfwd_type']} AND snmp_index={snmp_config['snmp_index']}"
        )

        pgquery.write_db(query)


def get_msgfwd_types():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT snmp_msgfwd_type_id, name "
        "FROM public.snmp_msgfwd_type"
        ") as row"
    )

    # Query PostgreSQL for the list of SNMP message forwarding types
    data = pgquery.query_db(query)

    msgfwd_types = {}
    for row in data:
        row = dict(row[0])
        msgfwd_types[row["name"]] = row["snmp_msgfwd_type_id"]

    return msgfwd_types


def get_config_list():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rsu_id, smt.name msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active "
        "FROM public.snmp_msgfwd_config smc "
        "JOIN public.snmp_msgfwd_type smt ON smc.msgfwd_type = smt.snmp_msgfwd_type_id"
        ") as row"
    )

    # Query PostgreSQL for the list of SNMP message forwarding configurations tracked in PostgreSQL
    data = pgquery.query_db(query)

    config_list = []
    for row in data:
        row = dict(row[0])
        row["start_datetime"] = datetime.strptime(
            row["start_datetime"], "%Y-%m-%dT%H:%M:%S"
        ).strftime("%Y-%m-%d %H:%M")
        row["end_datetime"] = datetime.strptime(
            row["end_datetime"], "%Y-%m-%dT%H:%M:%S"
        ).strftime("%Y-%m-%d %H:%M")
        config_list.append(row)

    return config_list


def update_postgresql(rsu_snmp_configs_obj):
    # Pull all recorded message forwarding configurations from PostgreSQL
    recorded_config_list = get_config_list()
    msgfwd_types = get_msgfwd_types()

    # Perform a diff on the active and recorded configurations
    # Altered configurations will be removed and then added for simplicity
    configs_to_remove = []
    configs_to_add = []

    # Determine configurations to be deleted
    for recorded_config in recorded_config_list:
        if recorded_config["rsu_id"] not in rsu_snmp_configs_obj:
            logging.warn(f"Unknown RSU with id of: {recorded_config['rsu_id']}")
            # Swap msgfwd_type string with PostgreSQL id
            recorded_config["msgfwd_type"] = msgfwd_types[
                recorded_config["msgfwd_type"]
            ]
            configs_to_remove.append(recorded_config)
            continue

        # Maintain configuration data on offline RSUs
        if (
            rsu_snmp_configs_obj[recorded_config["rsu_id"]]
            == "Unable to retrieve latest SNMP config"
        ):
            continue

        if recorded_config not in rsu_snmp_configs_obj[recorded_config["rsu_id"]]:
            logging.debug(f"Configuration is no longer active: {recorded_config}")
            # Swap msgfwd_type string with PostgreSQL id
            recorded_config["msgfwd_type"] = msgfwd_types[
                recorded_config["msgfwd_type"]
            ]
            configs_to_remove.append(recorded_config)

    # Determine configurations to be added
    for snmp_configs in rsu_snmp_configs_obj.values():
        if snmp_configs == "Unable to retrieve latest SNMP config":
            continue

        for snmp_config in snmp_configs:
            if snmp_config not in recorded_config_list:
                logging.debug(f"Configuration is new: {snmp_config}")
                # Swap msgfwd_type string with PostgreSQL id
                snmp_config["msgfwd_type"] = msgfwd_types[snmp_config["msgfwd_type"]]
                configs_to_add.append(snmp_config)

    # Make deletions
    if len(configs_to_remove) > 0:
        delete_config_list(configs_to_remove)

    # Make additions
    if len(configs_to_add) > 0:
        insert_config_list(configs_to_add)


def get_snmp_configs(rsu_list):
    config_obj = {}

    for rsu in rsu_list:
        request = {
            "rsu_ip": rsu["ipv4_address"],
            "snmp_version": rsu["snmp_version"],
            "snmp_creds": {
                "username": rsu["snmp_username"],
                "password": rsu["snmp_password"],
            },
        }
        response, code = rsufwdsnmpwalk.get(request)

        if code != 200:
            config_obj[rsu["rsu_id"]] = "Unable to retrieve latest SNMP config"
            continue

        config_list = []
        if rsu["snmp_version"] == "41":
            # Handle the rsuDsrcFwd configurations
            for key, value in response["RsuFwdSnmpwalk"].items():
                config = {
                    "rsu_id": rsu["rsu_id"],
                    "msgfwd_type": "rsuDsrcFwd",
                    "snmp_index": int(key),
                    "message_type": value["Message Type"],
                    "dest_ipv4": value["IP"],
                    "dest_port": value["Port"],
                    "start_datetime": value["Start DateTime"],
                    "end_datetime": value["End DateTime"],
                    "active": "1" if value["Config Active"] == "Enabled" else "0",
                }
                config_list.append(config)
        elif rsu["snmp_version"] == "1218":
            # Handle the rsuReceivedMsgTable configurations
            for key, value in response["RsuFwdSnmpwalk"]["rsuReceivedMsgTable"].items():
                config = {
                    "rsu_id": rsu["rsu_id"],
                    "msgfwd_type": "rsuReceivedMsg",
                    "snmp_index": int(key),
                    "message_type": value["Message Type"],
                    "dest_ipv4": value["IP"],
                    "dest_port": value["Port"],
                    "start_datetime": value["Start DateTime"],
                    "end_datetime": value["End DateTime"],
                    "active": "1" if value["Config Active"] == "Enabled" else "0",
                }
                config_list.append(config)

            # Handle the rsuXmitMsgFwdingTable configurations
            for key, value in response["RsuFwdSnmpwalk"][
                "rsuXmitMsgFwdingTable"
            ].items():
                config = {
                    "rsu_id": rsu["rsu_id"],
                    "msgfwd_type": "rsuXmitMsgFwding",
                    "snmp_index": int(key),
                    "message_type": value["Message Type"],
                    "dest_ipv4": value["IP"],
                    "dest_port": value["Port"],
                    "start_datetime": value["Start DateTime"],
                    "end_datetime": value["End DateTime"],
                    "active": "1" if value["Config Active"] == "Enabled" else "0",
                }
                config_list.append(config)

        config_obj[rsu["rsu_id"]] = config_list

    return config_obj


def get_rsu_list():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rd.rsu_id, rd.ipv4_address, snmp.username AS snmp_username, snmp.password AS snmp_password, sver.version_code AS snmp_version "
        "FROM public.rsus AS rd "
        "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id "
        "LEFT JOIN public.snmp_versions AS sver ON sver.snmp_version_id = rd.snmp_version_id"
        ") as row"
    )

    # Query PostgreSQL for the list of RSU IPs with SNMP credentials and version
    data = pgquery.query_db(query)

    rsu_list = []
    for row in data:
        rsu_list.append(dict(row[0]))

    return rsu_list


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    log_level = "INFO" if log_level == "" else log_level
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = os.environ.get("RSU_SNMP_FETCH", "False").lower() == "true"
    if not run_service:
        logging.info("The rsu-snmp-fetch service is disabled and will not run")
        exit()

    rsu_list = get_rsu_list()
    configs = get_snmp_configs(rsu_list)
    update_postgresql(configs)
