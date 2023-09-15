import logging

from flask_restful import Resource
import logging
import os
import smtp_error_handler


def unsubscribe_user(email: str):
    return smtp_error_handler.unsubscribe_user(email)


# REST endpoint resource class
class UnsubErrorEmails(Resource):
    options_headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type,Authorization',
        'Access-Control-Allow-Methods': 'GET',
        'Access-Control-Max-Age': '3600'
    }

    headers = {
        'Access-Control-Allow-Origin': '*',
        'Content-Type': 'application/json'
    }

    def options(self):
        # CORS support
        return ('', 204, self.options_headers)

    def get(self, email):
        logging.debug("UnsubErrorEmails GET requested")
        if email:
            resp_code = unsubscribe_user(email)

            if resp_code == 404:
                return (f"User with email {email} does not exist", 404, self.headers)
            else:
                return (f"User {email} was successfully unsubscribed!", 200, self.headers)
        return ("No unsubscribe email was specified", 400, self.headers)
