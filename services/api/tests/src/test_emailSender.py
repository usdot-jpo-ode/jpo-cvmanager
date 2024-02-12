from unittest.mock import MagicMock
from api.src.emailSender import EmailSender


EMAIL_TO_SEND_FROM = "test@test.test"
EMAILS_TO_SEND_TO = ["test@test.test, test2@test.test"]
EMAIL_SUBJECT = "Test Subject"
EMAIL_MESSAGE = "Test Message"
EMAIL_REPLY_EMAIL = "test@test.test"
EMAIL_APP_USERNAME = "test"
EMAIL_APP_PASSWORD = "test"
DEFAULT_TARGET_SMTP_SERVER_ADDRESS = "smtp.gmail.com"
DEFAULT_TARGET_SMTP_SERVER_PORT = 587


def test_send_with_tls_and_auth():
    # prepare
    emailSender = EmailSender(
        DEFAULT_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_TARGET_SMTP_SERVER_PORT
    )
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(
        EMAIL_TO_SEND_FROM,
        EMAILS_TO_SEND_TO,
        EMAIL_SUBJECT,
        EMAIL_MESSAGE,
        EMAIL_REPLY_EMAIL,
        "true", # tlsEnabled
        "true", # authEnabled
        EMAIL_APP_USERNAME,
        EMAIL_APP_PASSWORD,
    )

    # assert
    emailSender.server.starttls.assert_called_once()
    assert emailSender.server.ehlo.call_count == 2
    emailSender.server.login.assert_called_once()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()


def test_send_with_tls_and_no_auth():
    # prepare
    emailSender = EmailSender(
        DEFAULT_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_TARGET_SMTP_SERVER_PORT
    )
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(
        EMAIL_TO_SEND_FROM,
        EMAILS_TO_SEND_TO,
        EMAIL_SUBJECT,
        EMAIL_MESSAGE,
        EMAIL_REPLY_EMAIL,
        "true", # tlsEnabled
        "false", # authEnabled
        EMAIL_APP_USERNAME,
        EMAIL_APP_PASSWORD,
    )

    # assert
    emailSender.server.starttls.assert_called_once()
    assert emailSender.server.ehlo.call_count == 2
    emailSender.server.login.assert_not_called()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()


def test_send_with_no_tls_and_auth():
    # prepare
    emailSender = EmailSender(
        DEFAULT_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_TARGET_SMTP_SERVER_PORT
    )
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(
        EMAIL_TO_SEND_FROM,
        EMAILS_TO_SEND_TO,
        EMAIL_SUBJECT,
        EMAIL_MESSAGE,
        EMAIL_REPLY_EMAIL,
        "false", # tlsEnabled
        "true", # authEnabled
        EMAIL_APP_USERNAME,
        EMAIL_APP_PASSWORD,
    )

    # assert
    emailSender.server.starttls.assert_not_called()
    emailSender.server.ehlo.assert_called_once()
    emailSender.server.login.assert_called_once()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()


def test_send_with_no_tls_and_no_auth():
    # prepare
    emailSender = EmailSender(
        DEFAULT_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_TARGET_SMTP_SERVER_PORT
    )
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(
        EMAIL_TO_SEND_FROM,
        EMAILS_TO_SEND_TO,
        EMAIL_SUBJECT,
        EMAIL_MESSAGE,
        EMAIL_REPLY_EMAIL,
        "false", # tlsEnabled
        "false", # authEnabled
        EMAIL_APP_USERNAME,
        EMAIL_APP_PASSWORD,
    )

    # assert
    emailSender.server.starttls.assert_not_called()
    emailSender.server.ehlo.assert_called_once()
    emailSender.server.login.assert_not_called()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()