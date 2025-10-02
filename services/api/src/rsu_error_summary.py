import logging
from api.src import environment
from flask import abort, request
from flask_restful import Resource
from marshmallow import Schema
from marshmallow import fields

from common.emailSender import EmailSender

class RSUErrorSummarySchema(Schema):
    emails = fields.Str(required=True)
    subject = fields.Str(required=True)
    message = fields.Str(required=True)

class RSUErrorSummaryResource(Resource):
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

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def post(self):
        logging.debug("RSUErrorSummary POST requested")
        # Check for main body values
        if not request.json:
            logging.error("No JSON body provided")
            abort(400)

        self.validate_input(request.json)

        try:
            email_addresses = request.json["emails"].split(",")
            subject = request.json["subject"]
            message = request.json["message"]

            for email_address in email_addresses:
                logging.info(f"Sending email to {email_address}")
                emailSender = EmailSender(
                    environment.CSM_TARGET_SMTP_SERVER_ADDRESS,
                    environment.CSM_TARGET_SMTP_SERVER_PORT,
                )
                emailSender.send(
                    sender=environment.CSM_EMAIL_TO_SEND_FROM,
                    recipient=email_address,
                    subject=subject,
                    message=message,
                    replyEmail="",
                    username=environment.CSM_EMAIL_APP_USERNAME,
                    password=environment.CSM_EMAIL_APP_PASSWORD,
                    pretty=True,
                )
        except Exception as e:
            logging.error(f"Exception encountered: {e}")
            abort(500)
        return ("", 200, self.headers)

    def validate_input(self, input):
        try:
            schema = RSUErrorSummarySchema()
            schema.load(input)
        except Exception as e:
            logging.error(f"Exception encountered: {e}")
            abort(400)
