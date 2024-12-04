import common.pgquery as pgquery
import json
import logging
import os
import requests

from common.errors import ServerErrorException


def check_for_upgrade(rsu_ip):
    available_upgrade = {
        "upgrade_available": False,
        "upgrade_id": -1,
        "upgrade_name": "",
        "upgrade_version": "",
    }

    logging.info(f"Querying for an available firmware upgrade for {rsu_ip}")
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT fur.to_id AS eligible_upgrade_id, fi2.name AS eligible_upgrade_name, fi2.version AS eligible_upgrade_version "
        "FROM public.rsus AS rd "
        "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version "
        "JOIN public.firmware_images fi2 ON fi2.firmware_id = fur.to_id "
        f"WHERE rd.ipv4_address = '{rsu_ip}'"
        ") as row"
    )
    data = pgquery.query_db(query)

    if len(data) > 0:
        # Grab the first result, it should be the only result if the 'firmware_upgrade_rules' table is populated properly
        row = dict(data[0][0])
        available_upgrade["upgrade_available"] = True
        available_upgrade["upgrade_id"] = row["eligible_upgrade_id"]
        available_upgrade["upgrade_name"] = row["eligible_upgrade_name"]
        available_upgrade["upgrade_version"] = row["eligible_upgrade_version"]
    else:
        logging.warning(
            f"There is no firmware available for {rsu_ip} to upgrade to. Most likely due to the RSU already being up to date."
        )

    return available_upgrade


def mark_rsu_for_upgrade(rsu_ip):
    if os.getenv("FIRMWARE_MANAGER_ENDPOINT") is None:
        raise ServerErrorException(
            "The firmware manager is not supported for this CV Manager deployment"
        )

    # Verify requested target RSU is eligible for upgrade and determine next upgrade
    upgrade_info = check_for_upgrade(rsu_ip)

    if upgrade_info["upgrade_available"] is False:
        raise ServerErrorException(
            f"Requested RSU '{rsu_ip}' is already up to date with the latest firmware"
        )

    # Modify PostgreSQL RSU row to new target firmware ID
    query = f"UPDATE public.rsus SET target_firmware_version = {upgrade_info['upgrade_id']} WHERE ipv4_address = '{rsu_ip}'"
    pgquery.write_db(query)

    logging.info(f"Initiating firmware upgrade with the firmware manager for {rsu_ip}")
    # Environment variable FIRMWARE_MANAGER_ENDPOINT must contain "http://" and port
    firmware_manager_endpoint = os.getenv("FIRMWARE_MANAGER_ENDPOINT")
    post_body = {"rsu_ip": rsu_ip}
    response = requests.post(
        f"{firmware_manager_endpoint}/init_firmware_upgrade", json=post_body
    )

    # Forward response to the requester
    logging.info(response.text)
    json_msg = json.loads(response.text)
    return json_msg, response.status_code
