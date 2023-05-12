import datetime
import logging
import os
from flask import abort, request
from flask_restful import Resource
from marshmallow import Schema
from marshmallow import fields

import smtplib, ssl

EMAIL_TO_SEND_FROM = os.environ.get('EMAIL_TO_SEND_FROM')
EMAIL_APP_PASSWORD = os.environ.get('EMAIL_APP_PASSWORD')
EMAIL_TO_SEND_TO = os.environ.get('EMAIL_TO_SEND_TO')

if EMAIL_TO_SEND_FROM is None:
    print("Environment variable EMAIL_TO_SEND_FROM is not set")
    exit(1)

if EMAIL_APP_PASSWORD is None:
    print("Environment variable EMAIL_APP_PASSWORD is not set")
    exit(1)

if EMAIL_TO_SEND_TO is None:
    print("Environment variable EMAIL_TO_SEND_TO is not set")
    exit(1)

class SendEmailSchema(Schema):
    email = fields.Str(required=True)
    subject = fields.Str(required=True)
    message = fields.Str(required=True)

class SendEmailResource(Resource):
    options_headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type,Authorization',
        'Access-Control-Allow-Methods': 'POST',
        'Access-Control-Max-Age': '3600'
    }

    headers = {
        'Access-Control-Allow-Origin': '*',
        'Content-Type': 'application/json'
    }

    def options(self):
        # CORS support
        return ('', 204, self.options_headers)
    
    def post(self):
        logging.debug("SendEmail POST requested")
        # Check for main body values
        if not request.json:
            logging.error("No JSON body provided")
            abort(400)
            
        self.validate_input(request.json)

        try:
            replyEmail = request.json['email']
            subject = request.json['subject']
            message = request.json['message']
            
            emailSender = EmailSender()
            emailSender.send(EMAIL_TO_SEND_FROM, EMAIL_TO_SEND_TO, subject, message, replyEmail)
        except Exception as e:
            logging.error(f"Exception encountered: {e}")
            abort(500)
        return ('', 204, self.headers)

    def validate_input(self, input):
        try:
            schema = SendEmailSchema()
            schema.load(input)
        except Exception as e:
            logging.error(f"Exception encountered: {e}")
            abort(400)

class EmailSender():
    def __init__(self):
        self.smtp_server = "smtp.gmail.com"
        self.port = 587
        self.context = ssl.create_default_context()
        self.server = smtplib.SMTP(self.smtp_server, self.port)
    
    def send(self, sender, recipient, subject, message, replyEmail):
        try:
            self.server.ehlo() # say hello to server
            self.server.starttls(context=self.context) # start TLS encryption
            self.server.ehlo() # say hello again
            self.server.login(sender, EMAIL_APP_PASSWORD)

            emailHeaders = "From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n" % (sender, recipient, subject)
            toSend = emailHeaders + message + "\r\n\r\nReply-To: " + replyEmail

            # send email
            self.server.sendmail(sender, recipient, toSend)
            logging.debug(f"Email sent to {recipient}")
        except Exception as e:
            print(e)
        finally:
            self.server.quit()
    
if __name__ == '__main__':
    print("Instantiating EmailSender and sending email to " + EMAIL_TO_SEND_TO + "...")

    subject = "Test Email sent with `send_email.py`"

    message = """\
    Hello!

    This is a test email sent with `send_email.py`. If you are seeing this, it worked!

    Time: """ + str(datetime.datetime.now())

    replyEmail = "test@test.com"

    emailSender = EmailSender()
    emailSender.send(EMAIL_TO_SEND_FROM, EMAIL_TO_SEND_TO, subject, message, replyEmail)
    print("Email sent!")