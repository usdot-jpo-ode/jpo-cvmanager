import logging
import os
from flask import abort, request
from flask_restful import Resource
from marshmallow import Schema
from marshmallow import fields

from common.emailSender import EmailSender
from common.auth_tools import (
    ENVIRON_USER_KEY,
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    check_role_above,
)


class RSUErrorSummarySchema(Schema):
    emails = fields.Str(required=True)
    subject = fields.Str(required=True)
    message = fields.Str(required=True)


class RSUErrorSummaryResource(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST,OPTIONS",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST,OPTIONS",
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def post(self):
        logging.debug("RSUErrorSummary POST requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]

        # Check for main body values
        if not request.json:
            logging.error("No JSON body provided")
            abort(400)

        self.validate_input(request.json)

        if not user.user_info.super_user and not check_role_above(
            user.role, ORG_ROLE_LITERAL.OPERATOR
        ):
            return (
                {
                    "Message": "Unauthorized, requires at least super_user or organization operator role"
                },
                403,
                self.headers,
            )

        try:
            email_addresses = request.json["emails"].split(",")
            subject = request.json["subject"]
            message = request.json["message"]

            for email_address in email_addresses:
                logging.info(f"Sending email to {email_address}")
                emailSender = EmailSender(
                    os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"],
                    587,
                )
                emailSender.send(
                    sender=os.environ["CSM_EMAIL_TO_SEND_FROM"],
                    recipient=email_address,
                    subject=subject,
                    message=message,
                    replyEmail="",
                    username=os.environ["CSM_EMAIL_APP_USERNAME"],
                    password=os.environ["CSM_EMAIL_APP_PASSWORD"],
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
