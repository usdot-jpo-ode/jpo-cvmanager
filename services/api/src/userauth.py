import json
import os

# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource

from common.auth_tools import ENVIRON_USER_KEY, EnvironWithoutOrg
from werkzeug.exceptions import Forbidden


class UserAuth(Resource):
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

    def get(self):
        # Check for user info and return data
        user: EnvironWithoutOrg = request.environ[ENVIRON_USER_KEY]

        if not user.user_info:
            raise Forbidden("Unauthorized user")
        return (json.dumps(user.user_info.to_dict()), 200, self.headers)
