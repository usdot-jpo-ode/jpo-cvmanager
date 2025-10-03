from flask_restful import Resource
import logging
import requests
import json
import api_environment


def get_wzdx_data():
    # Execute the query and fetch all results
    return json.loads(
        requests.get(
            f"https://{api_environment.WZDX_ENDPOINT}/api/v1/wzdx?apiKey={api_environment.WZDX_API_KEY}"
        ).content.decode("utf-8")
    )


# REST endpoint resource class
class WzdxFeed(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def get(self):
        logging.debug("WzdxFeed GET requested")
        return (get_wzdx_data(), 200, self.headers)
