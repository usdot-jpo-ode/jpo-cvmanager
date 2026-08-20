import logging
from logging import Handler
import datetime
import traceback
import api_environment
from common.email_api import EmailApi
from common.keycloak_api import KeycloakServiceAccountApi


def configure_error_emails(app):
    mail_handler = ErrorEmailHandler()
    mail_handler.setLevel(logging.ERROR)
    app.logger.addHandler(mail_handler)


class ErrorEmailHandler(Handler):
    def __init__(self):
        super().__init__()  # initialize handler
        self.email_api = EmailApi(
            api_environment.IAPI_ENDPOINT,
            kc_api=KeycloakServiceAccountApi(
                api_environment.KEYCLOAK_ENDPOINT,
                api_environment.KEYCLOAK_REALM,
                api_environment.KC_SA_CLIENT_ID,
                api_environment.KC_SA_CLIENT_SECRET,
            ),
        )

    def emit(self, record):
        try:
            if not hasattr(record, "asctime"):
                # For some reason, asctime is not always available. So we update it to the current time in the same format (2023-08-23 15:39:29,115)
                record.asctime = datetime.datetime.now().strftime(
                    "%Y-%m-%d %H:%M:%S,%f"
                )[:-3]

            # Ensure stack_trace is always a string and preserve raw newlines.
            if record.exc_info:
                stack_trace = "".join(traceback.format_exception(*record.exc_info))
            elif record.exc_text:
                stack_trace = record.exc_text
            else:
                stack_trace = "No stack trace available"

            self.email_api.send_api_error_email(
                error_message=record.getMessage(),
                stack_trace=stack_trace,
                timestamp=record.asctime,
                logs_link=api_environment.LOGS_LINK,
            )

        except Exception:
            self.handleError(record)
