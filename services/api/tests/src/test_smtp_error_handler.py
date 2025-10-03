from collections import namedtuple
from unittest.mock import patch, MagicMock, mock_open
from api.src.smtp_error_handler import SMTP_SSLHandler
import api.src.smtp_error_handler as smtp_error_handler
import api.tests.data.smtp_error_handler_data as smtp_error_handler_data
from unittest.mock import ANY


def test_get_environment_name_success():
    expected = "test"
    actual = smtp_error_handler.get_environment_name("test:1234")

    assert actual == expected


def test_get_environment_name_fail():
    expected = "True"
    actual = smtp_error_handler.get_environment_name(True)

    assert actual == str(expected)


###################################### Testing Functions ##########################################
@patch(
    "api.src.api_environment.CSM_EMAILS_TO_SEND_TO",
    ["test@gmail.com", "test2@gmail.com"],
)
def test_get_subscribed_users_success():
    expected = ["test@gmail.com", "test2@gmail.com"]
    actual = smtp_error_handler.get_subscribed_users()
    assert actual == expected


EMAIL_TO_SEND_FROM = "test@test.test"
EMAIL_APP_USERNAME = "test"
EMAIL_APP_PASSWORD = "test"
DEFAULT_TARGET_SMTP_SERVER_ADDRESS = "smtp.gmail.com"
DEFAULT_TARGET_SMTP_SERVER_PORT = 587
ENVIRONMENT_NAME = "ENVIRONMENT"
LOGS_LINK = "http://logs_link.com"


@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_EMAIL_TO_SEND_FROM", EMAIL_TO_SEND_FROM)
@patch("api.src.api_environment.CSM_EMAIL_APP_USERNAME", EMAIL_APP_USERNAME)
@patch("api.src.api_environment.CSM_EMAIL_APP_PASSWORD", EMAIL_APP_PASSWORD)
def test_configure_error_emails():
    app = MagicMock()
    app.logger = MagicMock()
    app.logger.addHandler = MagicMock()
    smtp_error_handler.configure_error_emails(app)
    app.logger.addHandler.assert_called_once()


@patch("api.src.api_environment.LOGS_LINK", LOGS_LINK)
@patch("api.src.api_environment.ENVIRONMENT_NAME", ENVIRONMENT_NAME)
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch("api.src.smtp_error_handler.smtplib")
@patch("api.src.smtp_error_handler.get_subscribed_users")
def test_send(mock_get_subscribed_users, mock_smtplib, mock_file):
    # prepare
    emailHandler = SMTP_SSLHandler(
        mailhost=[DEFAULT_TARGET_SMTP_SERVER_ADDRESS, DEFAULT_TARGET_SMTP_SERVER_PORT],
        fromaddr=EMAIL_TO_SEND_FROM,
        toaddrs=[],
        subject="Automated CV Manager API Error",
        credentials=[EMAIL_APP_USERNAME, EMAIL_APP_PASSWORD],
        secure=(),
    )

    smtp_obj = MagicMock()
    mock_smtplib.SMTP = MagicMock()
    mock_smtplib.SMTP.return_value = smtp_obj

    smtp_obj.starttls = MagicMock()
    smtp_obj.ehlo = MagicMock()
    smtp_obj.login = MagicMock()
    smtp_obj.sendmail = MagicMock()
    smtp_obj.quit = MagicMock()
    mock_get_subscribed_users.return_value = (
        smtp_error_handler_data.subscribed_user_emails
    )
    mock_file = MagicMock()
    mock_file.read = MagicMock()
    mock_file.read.return_value = smtp_error_handler_data.html_email_template

    Record = namedtuple("Record", ["asctime"])
    record = Record("2023-09-15 00:00:00,000000")
    emailHandler.format = lambda x: "%s".format(x)

    # execute
    emailHandler.emit(record)

    # assert
    mock_get_subscribed_users.assert_called_once()
    smtp_obj.starttls.assert_called_once()
    smtp_obj.ehlo.assert_called_once()
    smtp_obj.login.assert_called_once_with(EMAIL_APP_USERNAME, EMAIL_APP_PASSWORD)

    smtp_obj.sendmail.call_count == 2
    print(smtp_obj.sendmail.call_args_list)
    smtp_obj.sendmail.assert_any_call(EMAIL_TO_SEND_FROM, "test1@gmail.com", ANY)
    smtp_obj.sendmail.assert_any_call(EMAIL_TO_SEND_FROM, "test2@gmail.com", ANY)

    smtp_obj.quit.call_count == 2
