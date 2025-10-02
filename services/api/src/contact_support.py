import logging
from api.src import environment

from flask import abort, request
from flask_restful import Resource
from marshmallow import Schema
from marshmallow import fields

from common.emailSender import EmailSender
from common.email_util import get_email_list


class ContactSupportSchema(Schema):
    email = fields.Str(required=True)
    subject = fields.Str(required=True)
    message = fields.Str(required=True)


class ContactSupportResource(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST,OPTIONS",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST,OPTIONS",
        "Content-Type": "application/json",
    }

    def __init__(self):
        self.CSM_EMAIL_TO_SEND_FROM = environment.CSM_EMAIL_TO_SEND_FROM
        self.CSM_TARGET_SMTP_SERVER_ADDRESS = environment.CSM_TARGET_SMTP_SERVER_ADDRESS
        self.CSM_TARGET_SMTP_SERVER_PORT = environment.CSM_TARGET_SMTP_SERVER_PORT
        self.CSM_TLS_ENABLED = environment.CSM_TLS_ENABLED
        self.CSM_AUTH_ENABLED = environment.CSM_AUTH_ENABLED
        self.CSM_EMAIL_APP_USERNAME = environment.CSM_EMAIL_APP_USERNAME
        self.CSM_EMAIL_APP_PASSWORD = environment.CSM_EMAIL_APP_PASSWORD

        if not self.CSM_EMAIL_TO_SEND_FROM:
            logging.error("CSM_EMAIL_TO_SEND_FROM environment variable not set")
            abort(500)
        if not self.CSM_TARGET_SMTP_SERVER_ADDRESS:
            logging.error("CSM_TARGET_SMTP_SERVER_ADDRESS environment variable not set")
            abort(500)
        if not self.CSM_TARGET_SMTP_SERVER_PORT:
            logging.error("CSM_TARGET_SMTP_SERVER_PORT environment variable not set")
            abort(500)

        if self.CSM_AUTH_ENABLED:
            if not self.CSM_EMAIL_APP_USERNAME:
                logging.error("CSM_EMAIL_APP_USERNAME environment variable not set")
                abort(500)
            if not self.CSM_EMAIL_APP_PASSWORD:
                logging.error("CSM_EMAIL_APP_PASSWORD environment variable not set")
                abort(500)

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def post(self):
        logging.debug("ContactSupport POST requested")
        # Check for main body values
        if not request.json:
            logging.error("No JSON body provided")
            abort(400)

        self.validate_input(request.json)

        try:
            replyEmail = request.json["email"]
            subject = request.json["subject"]
            message = request.json["message"]

            email_addresses = get_email_list("Support Requests")
            for email_address in email_addresses:
                emailSender = EmailSender(
                    self.CSM_TARGET_SMTP_SERVER_ADDRESS,
                    self.CSM_TARGET_SMTP_SERVER_PORT,
                )
                emailSender.send(
                    self.CSM_EMAIL_TO_SEND_FROM,
                    email_address,
                    subject,
                    message,
                    replyEmail,
                    self.CSM_EMAIL_APP_USERNAME,
                    self.CSM_EMAIL_APP_PASSWORD,
                    False,
                    self.CSM_TLS_ENABLED,
                    self.CSM_AUTH_ENABLED,
                )
        except Exception as e:
            logging.error(f"Exception encountered: {e}")
            abort(500)
        return ("", 200, self.headers)

    def validate_input(self, input):
        try:
            schema = ContactSupportSchema()
            schema.load(input)
        except Exception as e:
            logging.error(f"Exception encountered: {e}")
            abort(400)
