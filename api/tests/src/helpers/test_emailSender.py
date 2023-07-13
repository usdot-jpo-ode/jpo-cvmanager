from unittest.mock import MagicMock
from api.src.helpers.emailSender import EmailSender

import tests.data.contact_support_data as contact_support_data

DEFAULT_TARGET_SMTP_SERVER_ADDRESS = "smtp.gmail.com"
DEFAULT_TARGET_SMTP_SERVER_PORT = 587

def test_send():
    # prepare
    emailSender = EmailSender(DEFAULT_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_TARGET_SMTP_SERVER_PORT)
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(contact_support_data.EMAIL_TO_SEND_FROM, contact_support_data.EMAILS_TO_SEND_TO, contact_support_data.EMAIL_SUBJECT, contact_support_data.EMAIL_MESSAGE, contact_support_data.EMAIL_REPLY_EMAIL, contact_support_data.EMAIL_APP_PASSWORD)

    # assert
    emailSender.server.starttls.assert_called_once()
    assert emailSender.server.ehlo.call_count == 2
    emailSender.server.login.assert_called_once()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()