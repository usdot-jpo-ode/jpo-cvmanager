from unittest.mock import MagicMock
from api.src.emailSender import EmailSender


CSM_EMAIL_TO_SEND_FROM = "test@test.test"
CSM_EMAILS_TO_SEND_TO = ["test@test.test, test2@test.test"]
EMAIL_SUBJECT = "Test Subject"
EMAIL_MESSAGE = "Test Message"
EMAIL_REPLY_EMAIL = "test@test.test"
CSM_EMAIL_APP_USERNAME = "test"
CSM_EMAIL_APP_PASSWORD = "test"
DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS = "smtp.gmail.com"
DEFAULT_CSM_TARGET_SMTP_SERVER_PORT = 587

def test_send():
    # prepare
    emailSender = EmailSender(DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(CSM_EMAIL_TO_SEND_FROM, CSM_EMAILS_TO_SEND_TO, EMAIL_SUBJECT, EMAIL_MESSAGE, EMAIL_REPLY_EMAIL, CSM_EMAIL_APP_USERNAME, CSM_EMAIL_APP_PASSWORD)

    # assert
    emailSender.server.starttls.assert_called_once()
    assert emailSender.server.ehlo.call_count == 2
    emailSender.server.login.assert_called_once()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()