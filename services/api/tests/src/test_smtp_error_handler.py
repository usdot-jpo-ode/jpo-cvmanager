import sys
from unittest.mock import patch, MagicMock
from api.src.smtp_error_handler import ErrorEmailHandler
import api.src.smtp_error_handler as smtp_error_handler


LOGS_LINK = "http://logs_link.com"
IAPI_ENDPOINT = "http://test.test"
KC_SA_CLIENT_ID = "sa_cvmanager_python_api"
KC_SA_CLIENT_SECRET = "sa_cvmanager_python_api_secret"


def test_configure_error_emails():
    app = MagicMock()
    app.logger = MagicMock()
    app.logger.addHandler = MagicMock()
    smtp_error_handler.configure_error_emails(app)
    app.logger.addHandler.assert_called_once()


@patch("api_environment.LOGS_LINK", LOGS_LINK)
@patch("api_environment.IAPI_ENDPOINT", IAPI_ENDPOINT)
@patch("api_environment.KC_SA_CLIENT_ID", KC_SA_CLIENT_ID)
@patch("api_environment.KC_SA_CLIENT_SECRET", KC_SA_CLIENT_SECRET)
def test_emit_with_asctime():
    # Test emit when record has asctime
    email_handler = ErrorEmailHandler()
    email_handler.email_api = MagicMock()

    record = MagicMock()
    record.asctime = "2023-09-15 00:00:00,000"
    record.exc_text = "Test stack trace"
    record.exc_info = None
    record.getMessage.return_value = "Test error message"

    email_handler.emit(record)

    email_handler.email_api.send_api_error_email.assert_called_once_with(
        error_message="Test error message",
        stack_trace="Test stack trace",
        timestamp="2023-09-15 00:00:00,000",
        logs_link=LOGS_LINK,
    )


@patch("api_environment.LOGS_LINK", LOGS_LINK)
@patch("api_environment.IAPI_ENDPOINT", IAPI_ENDPOINT)
@patch("api_environment.KC_SA_CLIENT_ID", KC_SA_CLIENT_ID)
@patch("api_environment.KC_SA_CLIENT_SECRET", KC_SA_CLIENT_SECRET)
def test_emit_without_asctime():
    # Test emit when record doesn't have asctime
    email_handler = ErrorEmailHandler()
    email_handler.email_api = MagicMock()

    # Create a record without asctime
    record = MagicMock()
    record.getMessage = MagicMock(return_value="Test error message")
    record.exc_text = "Test stack trace"
    record.exc_info = None
    # Remove asctime attribute
    delattr(record, "asctime")

    with patch("datetime.datetime") as mock_datetime:
        mock_datetime.now.return_value.strftime.return_value = (
            "2023-09-15 00:00:00,123456"
        )

        email_handler.emit(record)

        # Verify asctime was set
        assert hasattr(record, "asctime")
        assert record.asctime == "2023-09-15 00:00:00,123"

        email_handler.email_api.send_api_error_email.assert_called_once_with(
            error_message="Test error message",
            stack_trace="Test stack trace",
            timestamp="2023-09-15 00:00:00,123",
            logs_link=LOGS_LINK,
        )


@patch("api_environment.LOGS_LINK", LOGS_LINK)
@patch("api_environment.IAPI_ENDPOINT", IAPI_ENDPOINT)
@patch("api_environment.KC_SA_CLIENT_ID", KC_SA_CLIENT_ID)
@patch("api_environment.KC_SA_CLIENT_SECRET", KC_SA_CLIENT_SECRET)
def test_emit_without_stack_trace():
    # Test emit when record doesn't have exc_text
    email_handler = ErrorEmailHandler()
    email_handler.email_api = MagicMock()

    record = MagicMock()
    record.asctime = "2023-09-15 00:00:00,000"
    record.exc_text = None
    record.exc_info = None
    record.getMessage.return_value = "Test error message"

    email_handler.emit(record)

    email_handler.email_api.send_api_error_email.assert_called_once_with(
        error_message="Test error message",
        stack_trace="No stack trace available",
        timestamp="2023-09-15 00:00:00,000",
        logs_link=LOGS_LINK,
    )


@patch("api_environment.LOGS_LINK", LOGS_LINK)
@patch("api_environment.IAPI_ENDPOINT", IAPI_ENDPOINT)
@patch("api_environment.KC_SA_CLIENT_ID", KC_SA_CLIENT_ID)
@patch("api_environment.KC_SA_CLIENT_SECRET", KC_SA_CLIENT_SECRET)
def test_emit_with_newlines():
    # Test that newlines are converted to <br> tags
    email_handler = ErrorEmailHandler()
    email_handler.email_api = MagicMock()

    record = MagicMock()
    record.asctime = "2023-09-15 00:00:00,000"
    record.exc_text = "Line 1\nLine 2\nLine 3"
    record.exc_info = None
    record.getMessage.return_value = "Error line 1\nError line 2"

    email_handler.emit(record)

    email_handler.email_api.send_api_error_email.assert_called_once_with(
        error_message="Error line 1\nError line 2",
        stack_trace="Line 1\nLine 2\nLine 3",
        timestamp="2023-09-15 00:00:00,000",
        logs_link=LOGS_LINK,
    )


@patch("api_environment.LOGS_LINK", LOGS_LINK)
@patch("api_environment.IAPI_ENDPOINT", IAPI_ENDPOINT)
@patch("api_environment.KC_SA_CLIENT_ID", KC_SA_CLIENT_ID)
@patch("api_environment.KC_SA_CLIENT_SECRET", KC_SA_CLIENT_SECRET)
def test_emit_handles_exception():
    # Test that exceptions in emit are handled
    email_handler = ErrorEmailHandler()
    email_handler.email_api = MagicMock()
    email_handler.email_api.send_api_error_email.side_effect = Exception("API Error")
    email_handler.handleError = MagicMock()

    record = MagicMock()
    record.asctime = "2023-09-15 00:00:00,000"
    record.exc_text = "Test stack trace"
    record.exc_info = None
    record.getMessage.return_value = "Test error message"

    email_handler.emit(record)

    # Verify handleError was called when exception occurred
    email_handler.handleError.assert_called_once_with(record)


@patch("api_environment.LOGS_LINK", LOGS_LINK)
@patch("api_environment.IAPI_ENDPOINT", IAPI_ENDPOINT)
@patch("api_environment.KC_SA_CLIENT_ID", KC_SA_CLIENT_ID)
@patch("api_environment.KC_SA_CLIENT_SECRET", KC_SA_CLIENT_SECRET)
def test_emit_with_real_exception():
    """Test emit with a real exception and traceback (exc_info)"""
    email_handler = ErrorEmailHandler()
    email_handler.email_api = MagicMock()

    # Create a real exception with traceback
    try:
        raise ValueError("Test exception")
    except ValueError:
        exc_info = sys.exc_info()

    record = MagicMock()
    record.asctime = "2023-09-15 00:00:00,000"
    record.exc_info = exc_info
    record.exc_text = None
    record.getMessage.return_value = "ValueError occurred"

    email_handler.emit(record)

    # Verify the email was sent
    assert email_handler.email_api.send_api_error_email.called
    call_args = email_handler.email_api.send_api_error_email.call_args[1]

    # Check that stack trace contains expected elements
    assert "ValueError: Test exception" in call_args["stack_trace"]
    assert "raise ValueError" in call_args["stack_trace"]
    assert "\n" in call_args["stack_trace"]  # Newlines preserved
    assert call_args["error_message"] == "ValueError occurred"
    assert call_args["timestamp"] == "2023-09-15 00:00:00,000"
