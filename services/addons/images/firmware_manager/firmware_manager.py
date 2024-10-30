from apscheduler.schedulers.background import BackgroundScheduler
from common import pgquery
from collections import deque
from flask import Flask, jsonify, request
from subprocess import Popen, DEVNULL
from threading import Lock
from waitress import serve
import json
import logging
import os

app = Flask(__name__)

log_level = os.environ.get("LOGGING_LEVEL", "INFO")
logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

manufacturer_upgrade_scripts = {
    "Commsignia": "commsignia_upgrader.py",
    "Yunex": "yunex_upgrader.py",
}


# Tracker for active firmware upgrades
# Key: IPv4 string of target device
# Value: Dictionary with the following key-values:
# - process
# - manufacturer
# - model
# - ssh_username
# - ssh_password
# - target_firmware_id
# - target_firmware_version
# - install_package
active_upgrades = {}
upgrade_queue = deque([])
upgrade_queue_info = {}
active_upgrades_lock = Lock()


# Changed from a constant to a function to help with unit testing
def get_upgrade_limit() -> int:
    try:
        upgrade_limit = int(os.environ.get("ACTIVE_UPGRADE_LIMIT", "1"))
        return upgrade_limit
    except ValueError:
        raise ValueError(
            "The environment variable 'ACTIVE_UPGRADE_LIMIT' must be an integer."
        )


# Function to query the CV Manager PostgreSQL database for RSUs that have:
# - A different target version than their current version
# - A target firmware that complies with an existing upgrade rule relative to the RSU's current version
# - An optional RSU IP can be specified for only returning results for a single RSU
def get_rsu_upgrade_data(rsu_ip="all"):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT ipv4_address, man.name AS manufacturer, rm.name AS model, rc.username AS ssh_username, rc.password AS ssh_password, "
        "fi.firmware_id AS target_firmware_id, fi.version AS target_firmware_version, fi.install_package AS install_package "
        "FROM public.rsus rd "
        "JOIN public.rsu_models rm ON rm.rsu_model_id = rd.model "
        "JOIN public.manufacturers man ON man.manufacturer_id = rm.manufacturer "
        "JOIN public.rsu_credentials rc ON rc.credential_id = rd.credential_id "
        "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version "
        "JOIN public.firmware_images fi ON fi.firmware_id = rd.target_firmware_version "
        "WHERE firmware_version != target_firmware_version AND target_firmware_version = fur.to_id"
    )
    if rsu_ip != "all":
        query += f" AND ipv4_address = '{rsu_ip}'"
    query += ") as row"

    data = pgquery.query_db(query)

    return_list = []
    for row in data:
        return_list.append(dict(row[0]))
    return return_list


def start_tasks_from_queue():
    # Start the next process in the queue if there are less than ACTIVE_UPGRADE_LIMIT number of active upgrades occurring
    while len(active_upgrades) < get_upgrade_limit() and len(upgrade_queue) > 0:
        rsu_to_upgrade = upgrade_queue.popleft()
        try:
            rsu_upgrade_info = upgrade_queue_info[rsu_to_upgrade]
            del upgrade_queue_info[rsu_to_upgrade]
            p = Popen(
                [
                    "python3",
                    f'/home/{manufacturer_upgrade_scripts[rsu_upgrade_info["manufacturer"]]}',
                    f"'{json.dumps(rsu_upgrade_info)}'",
                ],
                stdout=DEVNULL,
            )
            rsu_upgrade_info["process"] = p
            # Remove redundant ipv4_address from rsu since it is the key for active_upgrades
            del rsu_upgrade_info["ipv4_address"]
            active_upgrades[rsu_to_upgrade] = rsu_upgrade_info
        except Exception as err:
            # If this case occurs, only log it since there may not be a listener.
            # Since the upgrade_queue and upgrade_queue_info will no longer have the RSU present,
            # the hourly check_for_upgrades() will pick up the firmware upgrade again to retry the upgrade.
            logging.error(
                f"Encountered error of type {type(err)} while starting automatic upgrade process for {rsu_to_upgrade}: {err}"
            )


# REST endpoint to manually start firmware upgrades for targeted RSUs.
# Required request body values:
# - rsu_ip: Target device IP
@app.route("/init_firmware_upgrade", methods=["POST"])
def init_firmware_upgrade():
    request_args = request.get_json()
    if "rsu_ip" not in request_args:
        return jsonify({"error": "Missing 'rsu_ip' parameter"}), 400

    # Acquire lock and check if an upgrade is already occurring for the device
    logging.info(
        f"Checking if existing upgrade is running or queued for '{request_args['rsu_ip']}'"
    )
    with active_upgrades_lock:
        if (
            request_args["rsu_ip"] in active_upgrades
            or request_args["rsu_ip"] in upgrade_queue
        ):
            return (
                jsonify(
                    {
                        "error": f"Firmware upgrade failed to start for '{request_args['rsu_ip']}': an upgrade is already underway or queued for the target device"
                    }
                ),
                500,
            )

        # Check if latest ping was unsuccessful
        if not was_latest_ping_successful_for_rsu(request_args["rsu_ip"]):
            return (
                jsonify(
                    {
                        "error": f"Firmware upgrade failed to start for '{request_args['rsu_ip']}': device is unreachable"
                    }
                ),
                500,
            )

        # Pull RSU data from the PostgreSQL database
        logging.info(f"Querying RSU data for '{request_args['rsu_ip']}'")
        rsu_to_upgrade = get_rsu_upgrade_data(request_args["rsu_ip"])
        if len(rsu_to_upgrade) == 0:
            return (
                jsonify(
                    {
                        "error": f"Firmware upgrade failed to start for '{request_args['rsu_ip']}': the target firmware is already installed or is an invalid upgrade from the current firmware"
                    }
                ),
                500,
            )
        rsu_to_upgrade = rsu_to_upgrade[0]

        # Add the RSU to the upgrade queue and record the necessary upgrade information
        logging.info(
            f"Adding '{request_args['rsu_ip']}' to the firmware manager upgrade queue"
        )
        upgrade_queue.extend([request_args["rsu_ip"]])
        upgrade_queue_info[request_args["rsu_ip"]] = rsu_to_upgrade

        # Start any processes that can be started
        start_tasks_from_queue()
    return (
        jsonify(
            {
                "message": f"Firmware upgrade started successfully for '{request_args['rsu_ip']}'"
            }
        ),
        201,
    )


# REST endpoint to mark a firmware upgrade as complete and remove it from the active upgrades.
# Will modify the RSU's firmware_version according to the "status" argument in the PostgreSQL database.
# Required request body key-values:
# - rsu_ip: Target device IPv4 address
# - status: "success" or "fail" depending upon result of the firmware upgrade
@app.route("/firmware_upgrade_completed", methods=["POST"])
def firmware_upgrade_completed():
    request_args = request.get_json()
    with active_upgrades_lock:
        if "rsu_ip" not in request_args:
            return jsonify({"error": "Missing 'rsu_ip' parameter"}), 400
        elif request_args["rsu_ip"] not in active_upgrades:
            return (
                jsonify(
                    {
                        "error": "Specified device is not actively being upgraded or was already completed"
                    }
                ),
                400,
            )

        if "status" not in request_args:
            return jsonify({"error": "Missing 'status' parameter"}), 400
        elif request_args["status"] != "success" and request_args["status"] != "fail":
            return (
                jsonify(
                    {
                        "error": "Wrong value for 'status' parameter - must be either 'success' or 'fail'"
                    }
                ),
                400,
            )

        # Update RSU firmware_version in PostgreSQL if the upgrade was successful
        if request_args["status"] == "success":
            reset_consecutive_failure_count_for_rsu(request_args["rsu_ip"])
            try:
                upgrade_info = active_upgrades[request_args["rsu_ip"]]
                query = f"UPDATE public.rsus SET firmware_version={upgrade_info['target_firmware_id']} WHERE ipv4_address='{request_args['rsu_ip']}'"
                pgquery.write_db(query)
            except Exception as err:
                logging.error(
                    f"Encountered error of type {type(err)} while querying the PostgreSQL database: {err}"
                )
                return (
                    jsonify(
                        {
                            "error": "Unexpected error occurred while querying the PostgreSQL database - firmware upgrade not marked as complete"
                        }
                    ),
                    500,
                )
        else:
            increment_consecutive_failure_count_for_rsu(request_args["rsu_ip"])
            if is_rsu_at_max_retries_limit(request_args["rsu_ip"]):
                logging.error(
                    f"RSU {request_args['rsu_ip']} has reached the maximum number of upgrade retries. Setting target_firmware_version to firmware_version and resetting consecutive failures count."
                )

                # set target_firmware_version to firmware_version value
                query = f"UPDATE public.rsus SET target_firmware_version=firmware_version WHERE ipv4_address='{request_args['rsu_ip']}'"
                pgquery.write_db(query)

                log_max_retries_reached_incident_for_rsu_to_postgres(
                    request_args["rsu_ip"],
                    active_upgrades[request_args["rsu_ip"]]["target_firmware_version"],
                )

                reset_consecutive_failure_count_for_rsu(request_args["rsu_ip"])

        # Remove firmware upgrade from active upgrades
        logging.info(
            f"Marking firmware upgrade as complete for '{request_args['rsu_ip']}'"
        )
        del active_upgrades[request_args["rsu_ip"]]

        # Start any processes that can be started
        start_tasks_from_queue()

    return jsonify({"message": "Firmware upgrade successfully marked as complete"}), 204


# REST endpoint to retrieve a list of all active firmware upgrades.
@app.route("/list_active_upgrades", methods=["GET"])
def list_active_upgrades():
    # Remove all sensitive data from the response
    sanitized_active_upgrades = {}
    with active_upgrades_lock:
        for key, value in active_upgrades.items():
            sanitized_active_upgrades[key] = {
                "manufacturer": value["manufacturer"],
                "model": value["model"],
                "target_firmware_id": value["target_firmware_id"],
                "target_firmware_version": value["target_firmware_version"],
                "install_package": value["install_package"],
            }
        return (
            jsonify(
                {
                    "active_upgrades": sanitized_active_upgrades,
                    "upgrade_queue": list(upgrade_queue),
                }
            ),
            200,
        )


# Scheduled firmware upgrade checker
def check_for_upgrades():
    logging.info("Checking PostgreSQL DB for RSUs with new target firmware")
    # Get all RSUs that need to be upgraded from the PostgreSQL database
    rsus_to_upgrade = get_rsu_upgrade_data()

    with active_upgrades_lock:
        # Start upgrade scripts for any results
        for rsu in rsus_to_upgrade:
            # Check if an upgrade is already occurring for the device
            if (
                rsu["ipv4_address"] in active_upgrades
                or rsu["ipv4_address"] in upgrade_queue
            ):
                continue

            # Check if latest ping was unsuccessful
            if not was_latest_ping_successful_for_rsu(rsu["ipv4_address"]):
                logging.info(
                    f"Skipping firmware upgrade for '{rsu['ipv4_address']}': device is unreachable"
                )
                continue

            # Add the RSU to the upgrade queue and record the necessary upgrade information
            logging.info(
                f"Adding '{rsu["ipv4_address"]}' to the firmware manager upgrade queue"
            )
            upgrade_queue.extend([rsu["ipv4_address"]])
            upgrade_queue_info[rsu["ipv4_address"]] = rsu
            logging.info(
                f"Firmware upgrade successfully started for '{rsu["ipv4_address"]}'"
            )

        # Start any processes that can be started
        start_tasks_from_queue()


def was_latest_ping_successful_for_rsu(rsu_ip):
    query = f"select result from ping where rsu_id=(select rsu_id from rsus where ipv4_address='{rsu_ip}') order by timestamp desc limit 1"
    query_result = pgquery.query_db(query)
    if len(query_result) == 0 or len(query_result[0]) == 0:
        # no ping results have been recorded for this RSU
        return False
    latest_ping_successful = query_result[0][0]
    logging.info(f"Latest ping result for '{rsu_ip}': {latest_ping_successful}")
    return latest_ping_successful


def increment_consecutive_failure_count_for_rsu(rsu_ip):
    upsert_query = f"insert into consecutive_firmware_upgrade_failures (rsu_id, consecutive_failures) values ((select rsu_id from rsus where ipv4_address='{rsu_ip}'), 1) on conflict (rsu_id) do update set consecutive_failures=consecutive_firmware_upgrade_failures.consecutive_failures+1"
    pgquery.write_db(upsert_query)


def reset_consecutive_failure_count_for_rsu(rsu_ip):
    upsert_query = f"insert into consecutive_firmware_upgrade_failures (rsu_id, consecutive_failures) values ((select rsu_id from rsus where ipv4_address='{rsu_ip}'), 0) on conflict (rsu_id) do update set consecutive_failures=0"
    pgquery.write_db(upsert_query)


def is_rsu_at_max_retries_limit(rsu_ip):
    max_retries = int(os.environ.get("FW_UPGRADE_MAX_RETRY_LIMIT", "3"))
    query_result = pgquery.query_db(
        f"select consecutive_failures from consecutive_firmware_upgrade_failures where rsu_id=(select rsu_id from rsus where ipv4_address='{rsu_ip}')"
    )
    if len(query_result) == 0 or len(query_result[0]) == 0:
        # no failures have been recorded for this RSU, so it cannot be at the limit
        return False
    consecutive_failures = query_result[0][0]
    return consecutive_failures >= max_retries


def log_max_retries_reached_incident_for_rsu_to_postgres(
    rsu_ip,
    target_firmware_version: int,
):
    pgquery.write_db(
        f"insert into max_retry_limit_reached_instances (rsu_id, reached_at, target_firmware_version) values ((select rsu_id from rsus where ipv4_address='{rsu_ip}'), now(), (select firmware_id from firmware_images where name='{target_firmware_version}'))"
    )


def serve_rest_api():
    # Run Flask app for manually initiated firmware upgrades
    logging.info("Initiating Firmware Manager REST API...")
    serve(app, host="0.0.0.0", port=8080)


def init_background_task():
    logging.info("Initiating Firmware Manager background checker...")
    # Run scheduler for async RSU firmware upgrade checks
    scheduler = BackgroundScheduler({"apscheduler.timezone": "UTC"})
    scheduler.add_job(check_for_upgrades, "cron", minute="0")
    scheduler.start()


if __name__ == "__main__":
    init_background_task()
    serve_rest_api()
