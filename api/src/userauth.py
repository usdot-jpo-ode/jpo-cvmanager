import json

# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource


class UserAuth(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }

    headers = {"Access-Control-Allow-Origin": "*", "Content-Type": "application/json"}

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def get(self):
        # Check for user info and return data
        data = request.environ["user_info"]
        if data:
            return (json.dumps(data), 200, self.headers)
        return ("Unauthorized user", 401)
