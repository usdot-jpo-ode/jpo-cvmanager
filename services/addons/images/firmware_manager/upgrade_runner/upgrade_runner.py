from flask import Flask, jsonify, request, abort
from subprocess import Popen, DEVNULL
from waitress import serve
from marshmallow import Schema, fields
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


def start_upgrade_task(rsu_upgrade_data):
    try:
        Popen(
            [
                "python3",
                f'/home/{manufacturer_upgrade_scripts[rsu_upgrade_data["manufacturer"]]}',
                f"'{json.dumps(rsu_upgrade_data)}'",
            ],
            stdout=DEVNULL,
        )

        return (
            jsonify(
                {
                    "message": f"Firmware upgrade started successfully for '{rsu_upgrade_data['ipv4_address']}'"
                }
            ),
            201,
        )
    except Exception as err:
        # If this case occurs, only log it since there may not be a listener.
        # Since the upgrade_queue and upgrade_queue_info will no longer have the RSU present,
        # the hourly check_for_upgrades() will pick up the firmware upgrade again to retry the upgrade.
        logging.error(
            f"Encountered error of type {type(err)} while starting automatic upgrade process for {rsu_upgrade_data['ipv4_address']}: {err}"
        )

        return (
            jsonify(
                {
                    "message": f"Firmware upgrade failed to start for '{rsu_upgrade_data['ipv4_address']}'"
                }
            ),
            500,
        )


class RunFirmwareUpgradeSchema(Schema):
    ipv4_address = fields.IPv4(required=True)
    manufacturer = fields.Str(required=True)
    model = fields.Str(required=True)
    ssh_username = fields.Str(required=True)
    ssh_password = fields.Str(required=True)
    target_firmware_id = fields.Int(required=True)
    target_firmware_version = fields.Str(required=True)
    install_package = fields.Str(required=True)


# REST endpoint to manually start firmware upgrades for a single targeted RSU
# Required request body values:
# - ipv4_address
# - manufacturer
# - model
# - ssh_username
# - ssh_password
# - target_firmware_id
# - target_firmware_version
# - install_package
@app.route("/run_firmware_upgrade", methods=["POST"])
def run_firmware_upgrade():
    # Verify HTTP body JSON object
    request_args = request.get_json()
    schema = RunFirmwareUpgradeSchema()
    errors = schema.validate(request_args)
    if errors:
        logging.error(str(errors))
        abort(400, str(errors))

    # Start the RSU upgrade task
    return start_upgrade_task(request_args)


def serve_rest_api():
    # Run Flask app
    logging.info("Initiating the Firmware Manager Upgrade Runner REST API...")
    serve(app, host="0.0.0.0", port=8080)


if __name__ == "__main__":
    serve_rest_api()
