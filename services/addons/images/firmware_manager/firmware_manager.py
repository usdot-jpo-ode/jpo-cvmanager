from apscheduler.schedulers.background import BackgroundScheduler
from flask import Flask, jsonify, request
from waitress import serve
import logging
import os

app = Flask(__name__)

log_level = os.environ.get("LOGGING_LEVEL", "INFO")
logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

# Process tracker for active firmware upgrades
active_upgrades = {}

# Manual REST endpoint to start firmware upgrades for targeted RSUs
@app.route("/init_firmware_upgrade", methods=["POST"])
def init_firmware_upgrade():
  data = request.get_json()

  if "rsu_ip" not in data:
    return jsonify({"error": "Missing 'rsu_ip' parameter"}), 400

  logging.info(f"Initializing firmware upgrade for '{data['rsu_ip']}'")
  return jsonify({"message": f"Firmware upgrade started successfully for '{data['rsu_ip']}'"}), 201

# Scheduled PostgreSQL checker for RSUs with a different target firmware than their current 
def check_for_upgrades():
  logging.info("Checking PostgreSQL DB for RSUs with new target firmware")

if __name__ == "__main__":
  logging.info("Initiating Firmware Manager background checker...")
  # Run scheduler for async RSU firmware upgrade checks
  scheduler = BackgroundScheduler({"apscheduler.timezone": "UTC"})
  scheduler.add_job(check_for_upgrades, "cron", minute="0")
  scheduler.start()

  # Run Flask app for manually initiated firmware upgrades
  logging.info("Initiating Firmware Manager REST API...")
  serve(app, host="0.0.0.0", port=5000)