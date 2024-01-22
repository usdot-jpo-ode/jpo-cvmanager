from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import logging
import logging.handlers

from logging.handlers import SMTPHandler
import smtplib
import datetime
import ssl
import os

import common.pgquery as pgquery


def get_subscribed_users():
    query = "SELECT email FROM public.users WHERE receive_error_emails = '1'"

    data = pgquery.query_db(query)

    return [point["email"] for point in data]


def unsubscribe_user(email: str):
    pre_check = f"SELECT receive_error_emails FROM public.users WHERE email = '{email}'"
    pre_check_results = pgquery.query_db(pre_check)
    if not pre_check_results:
        return 400

    query = f"UPDATE public.users SET receive_error_emails='0' WHERE email = '{email}'"

    pgquery.write_db(query)

    return 200


def configure_error_emails(app):
    mail_handler = SMTP_SSLHandler(
        mailhost=[
            os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"],
            int(os.environ["CSM_TARGET_SMTP_SERVER_PORT"]),
        ],
        fromaddr=os.environ["CSM_EMAIL_TO_SEND_FROM"],
        toaddrs=[],
        subject="Automated CV Manager API Error",
        credentials=[
            os.environ["CSM_EMAIL_APP_USERNAME"],
            os.environ["CSM_EMAIL_APP_PASSWORD"],
        ],
        secure=(),
    )
    mail_handler.setLevel(logging.ERROR)
    # this seems weird, but it's the only way I can figure out how to include the stack trace info. This command appends the stack trace to the end of the self.format(record) call.
    mail_handler.setFormatter(logging.Formatter(""))
    app.logger.addHandler(mail_handler)


def get_environment_name(instance_connection_name):
    try:
        return instance_connection_name.split(":")[0]
    except:
        return str(instance_connection_name)


class SMTP_SSLHandler(SMTPHandler):
    def __init__(
        self, mailhost, fromaddr, toaddrs, subject, credentials=None, secure=None
    ):
        super(SMTP_SSLHandler, self).__init__(
            mailhost, fromaddr, toaddrs, subject, credentials, secure
        )

    def emit(self, record):
        try:
            subscribed_users = get_subscribed_users()

            if not hasattr(record, "asctime"):
                # For some reason, asctime is not always available. So we update it to the current time in the same format (2023-08-23 15:39:29,115)
                record.asctime = datetime.datetime.now().strftime(
                    "%Y-%m-%d %H:%M:%S,%f"
                )[:-3]

            body_content = open("./error_email/error_email_template.html").read()

            EMAIL_KEYS = {
                "ENVIRONMENT": os.environ["ENVIRONMENT_NAME"],
                "ERROR_MESSAGE": self.format(record).replace("\n", "<br>"),
                "ERROR_TIME": str(record.asctime),
                "LOGS_LINK": os.environ["LOGS_LINK"],
            }

            context = ssl._create_unverified_context()
            smtp = smtplib.SMTP(host=self.mailhost, port=self.mailport)
            smtp.starttls(context=context)
            smtp.ehlo()
            smtp.login(self.username, self.password)

            for email in subscribed_users:
                message = MIMEMultipart()
                message["Subject"] = self.subject
                message["From"] = self.fromaddr
                message["To"] = email

                EMAIL_KEYS["UNSUBSCRIBE_LINK"] = os.environ[
                    "ERROR_EMAIL_UNSUBSCRIBE_LINK"
                ].format(email=email)

                for key, value in EMAIL_KEYS.items():
                    body_content = body_content.replace(f"##_{key}_##", value)
                message.attach(MIMEText(body_content, "html"))
                smtp.sendmail(self.fromaddr, email, message.as_string())
            smtp.quit()

            logging.debug(f"Successfully sent error email to {subscribed_users}")
        except Exception as e:
            logging.exception(e)
