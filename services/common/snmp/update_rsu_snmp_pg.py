import logging
import common.pgquery as pgquery
import common.snmp.ntcip1218.rsu_message_forward as ntcip1218_rsumf
import common.snmp.rsu41.rsu_message_forward as rsu41_rsumf
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


def get_config_list(rsu_obj={}):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rsu_id, smt.name msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active "
        "FROM public.snmp_msgfwd_config smc "
        "JOIN public.snmp_msgfwd_type smt ON smc.msgfwd_type = smt.snmp_msgfwd_type_id"
    )

    # If an rsu_obj was provided, only return the RSU information for the subset
    if len(rsu_obj) > 0:
        query += " WHERE "
        for rsu_id in rsu_obj:
            query += f"rsu_id = {rsu_id} OR "
        # Trim off the last " OR " which is 4 characters long
        query = query[:-4]

    query += ") as row"

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


def update_postgresql(rsu_snmp_configs_obj, subset=False):
    # Pull all recorded message forwarding configurations from PostgreSQL
    # If the rsu_snmp_configs_obj is only a subset of all of the RSUs in PostgreSQL, only get the relevant configs
    if subset:
        recorded_config_list = get_config_list(rsu_snmp_configs_obj)
    else:
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


def get_snmp_msgfwd_configs(rsu_list):
    config_obj = {}

    for rsu in rsu_list:
        snmp_creds = {
            "username": rsu["snmp_username"],
            "password": rsu["snmp_password"],
        }

        if rsu["snmp_version"] == "41":
            response, code = rsu41_rsumf.get(rsu["ipv4_address"], snmp_creds)
        elif rsu["snmp_version"] == "1218":
            response, code = ntcip1218_rsumf.get(rsu["ipv4_address"], snmp_creds)
        else:
            config_obj[rsu["rsu_id"]] = "Unsupported SNMP version"
            continue

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
                logging.info(config)
                config_list.append(config)

        config_obj[rsu["rsu_id"]] = config_list

    return config_obj
