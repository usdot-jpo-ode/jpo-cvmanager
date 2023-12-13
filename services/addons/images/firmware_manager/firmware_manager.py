from apscheduler.schedulers.background import BackgroundScheduler
from flask import Flask, jsonify, request
from waitress import serve
from common import pgquery
import logging
import os

app = Flask(__name__)

log_level = os.environ.get("LOGGING_LEVEL", "INFO")
logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

# Tracker for active firmware upgrades
# Key: IPv4 string of target device
# Value: Dictionary with the following values:
# - manufacturer
# - model
# - target_firmware_id
# - target_firmware_name
# - install_package
active_upgrades = {}


# Function to query the CV Manager PostgreSQL database for RSUs that have: 
# - A different target version than their current version
# - A target firmware that complies with an existing upgrade rule relative to the RSU's current version
# - An optional RSU IP can be specified for only returning results for a single RSU
def get_rsu_upgrade_data(rsu_ip = "all"):
  query = "SELECT to_jsonb(row) " \
    "FROM (" \
      "SELECT ipv4_address, man.name AS manufacturer, rm.name AS model, rc.username AS ssh_username, rc.password AS ssh_password, " \
        "fi.firmware_id AS target_firmware_id, fi.name AS target_firmware_name, fi.install_package AS install_package " \
      "FROM public.rsus rd " \
      "JOIN public.rsu_models rm ON rm.rsu_model_id = rd.model " \
      "JOIN public.manufacturers man ON man.manufacturer_id = rm.manufacturer " \
      "JOIN public.rsu_credentials rc ON rc.credential_id = rd.credential_id " \
      "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version " \
      "JOIN public.firmware_images fi ON fi.firmware_id = rd.target_firmware_version " \
      "WHERE firmware_version != target_firmware_version AND target_firmware_version = fur.to_id"
  if rsu_ip != "all":
    query += f" AND ipv4_address = '{rsu_ip}'"
  query += ") as row"

  data = pgquery.query_db(query)

  return_list = []
  for row in data:
    return_list.append(dict(row[0]))
  return return_list


# REST endpoint to manually start firmware upgrades for targeted RSUs.
# Required request body values:
# - rsu_ip: Target device IP
@app.route("/init_firmware_upgrade", methods=["POST"])
def init_firmware_upgrade():
  request_args = request.get_json()
  if "rsu_ip" not in request_args:
    return jsonify({"error": "Missing 'rsu_ip' parameter"}), 400

  # Check if an upgrade is already occurring for the device
  if request_args['rsu_ip'] in active_upgrades:
    return jsonify({"error": f"Firmware upgrade failed to start for '{request_args['rsu_ip']}': an upgrade is already underway for the target device"}), 500

  # Pull RSU data from the PostgreSQL database
  rsu_to_upgrade = get_rsu_upgrade_data(request_args['rsu_ip'])
  if len(rsu_to_upgrade) == 0:
    return jsonify({"error": f"Firmware upgrade failed to start for '{request_args['rsu_ip']}': due to the target firmware already being installed or being an invalid upgrade from the current firmware"}), 500
  rsu_to_upgrade = rsu_to_upgrade[0]

  logging.info(f"Initializing firmware upgrade for '{request_args['rsu_ip']}'")
  # Start upgrade process
  active_upgrades[rsu_to_upgrade['ipv4_address']] = {
      "manufacturer": rsu_to_upgrade['manufacturer'],
      "model": rsu_to_upgrade['model'],
      "target_firmware_id": rsu_to_upgrade['target_firmware_id'],
      "target_firmware_name": rsu_to_upgrade['target_firmware_name'],
      "install_package": rsu_to_upgrade['install_package']
    }

  return jsonify({"message": f"Firmware upgrade started successfully for '{request_args['rsu_ip']}'"}), 201


# REST endpoint to mark a firmware upgrade as complete and remove it from the active upgrades.
# Will modify the RSU's firmware_version according to the "status" argument in the PostgreSQL database.
# Required request body values:
# - rsu_ip: Target device IP
# - status: "success" or "fail" depending upon result of the firmware upgrade
@app.route("/firmware_upgrade_completed", methods=["POST"])
def firmware_upgrade_completed():
  request_args = request.get_json()
  if "rsu_ip" not in request_args:
    return jsonify({"error": "Missing 'rsu_ip' parameter"}), 400
  if "status" not in request_args:
    return jsonify({"error": "Missing 'status' parameter"}), 400
  elif request_args['status'] != "success" and request_args['status'] != "fail":
    return jsonify({"error": "Wrong value for 'status' parameter - must be either 'success' or 'fail'"}), 400

  # Update RSU firmware_version in PostgreSQL if the upgrade was successful
  if request_args['status'] == "success":
    try:
      upgrade_info = active_upgrades[request_args['rsu_ip']]
      query = f"UPDATE public.rsus SET firmware_version={upgrade_info['target_firmware_id']} WHERE ipv4_address='{request_args['rsu_ip']}'"
      pgquery.write_db(query)
    except Exception as err:
      logging.error(f"Encountered error of type {type(err)} while querying the PostgreSQL database: {err}")
      return jsonify({"error": "Unexpected error occurred while querying the PostgreSQL database - firmware upgrade not marked as complete"}), 500

  # Remove firmware upgrade from active upgrades
  logging.info(f"Marking firmware upgrade as complete for '{request_args['rsu_ip']}'")
  del active_upgrades[request_args['rsu_ip']]

  return jsonify({"message": "Firmware upgrade successfully marked as complete"}), 204


# REST endpoint to retrieve a list of all active firmware upgrades.
@app.route("/list_active_upgrades", methods=["GET"])
def list_active_upgrades():
  return jsonify({"active_upgrades": active_upgrades}), 200


# Scheduled firmware upgrade checker
def check_for_upgrades():
  logging.info("Checking PostgreSQL DB for RSUs with new target firmware")
  # Get all RSUs that need to be upgraded from the PostgreSQL database
  rsus_to_upgrade = get_rsu_upgrade_data()

  # Start upgrade scripts for any results
  for rsu in rsus_to_upgrade:
    # Check if an upgrade is already occurring for the device
    if rsu['ipv4_address'] in active_upgrades:
      continue

    # Start upgrade script
    active_upgrades[rsu['ipv4_address']] = {
      "manufacturer": rsu['manufacturer'],
      "model": rsu['model'],
      "target_firmware_id": rsu['target_firmware_id'],
      "target_firmware_name": rsu['target_firmware_name'],
      "install_package": rsu['install_package']
    }


def serve_rest_api():
  # Run Flask app for manually initiated firmware upgrades
  logging.info("Initiating Firmware Manager REST API...")
  serve(app, host="0.0.0.0", port=5000)


def init_background_task():
  logging.info("Initiating Firmware Manager background checker...")
  # Run scheduler for async RSU firmware upgrade checks
  scheduler = BackgroundScheduler({"apscheduler.timezone": "UTC"})
  scheduler.add_job(check_for_upgrades, "cron", minute="0")
  scheduler.start()


if __name__ == "__main__":
  init_background_task()
  serve_rest_api()