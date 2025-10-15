from flask_restful import Resource
import logging
import requests
import json
import os

from common.auth_tools import require_permission


def get_wzdx_data():
    # Execute the query and fetch all results
    return json.loads(
        requests.get(
            f'https://{os.getenv("WZDX_ENDPOINT")}/api/v1/wzdx?apiKey={os.getenv("WZDX_API_KEY")}'
        ).content.decode("utf-8")
    )


# REST endpoint resource class
class WzdxFeed(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
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

    @require_permission(required_role=None)
    def get(self):
        logging.debug("WzdxFeed GET requested")
        return (get_wzdx_data(), 200, self.headers)
