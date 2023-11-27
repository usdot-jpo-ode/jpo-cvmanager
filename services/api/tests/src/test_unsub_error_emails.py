from unittest.mock import patch, MagicMock, call
import pytest
import src.unsub_error_emails as unsub_error_emails
import tests.data.unsub_error_emails_data as unsub_error_emails_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################


def test_request_options():
    info = unsub_error_emails.UnsubErrorEmails()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'


@patch('src.unsub_error_emails.unsubscribe_user')
def test_unsubscribe_user_success(mock_unsubscribe_user):
    email = "test@gmail.com"
    mock_unsubscribe_user.return_value = 200
    status = unsub_error_emails.UnsubErrorEmails()
    (body, code, headers) = status.get(email)

    mock_unsubscribe_user.assert_called_once()
    mock_unsubscribe_user.assert_called_with(email)
    assert code == 200
    assert headers['Access-Control-Allow-Origin'] == "test.com"
    assert body == f"User {email} was successfully unsubscribed!"


@patch('src.unsub_error_emails.unsubscribe_user')
def test_unsubscribe_user_400(mock_unsubscribe_user):
    email = None
    mock_unsubscribe_user.return_value = 200
    status = unsub_error_emails.UnsubErrorEmails()
    (body, code, headers) = status.get(email)

    mock_unsubscribe_user.assert_not_called()
    assert code == 400
    assert headers['Access-Control-Allow-Origin'] == "test.com"
    assert body == f"No unsubscribe email was specified"


@patch('src.unsub_error_emails.unsubscribe_user')
def test_unsubscribe_user_404(mock_unsubscribe_user):
    email = "test@gmail.com"
    mock_unsubscribe_user.return_value = 400
    status = unsub_error_emails.UnsubErrorEmails()
    (body, code, headers) = status.get(email)

    mock_unsubscribe_user.assert_called_once()
    mock_unsubscribe_user.assert_called_with(email)
    assert code == 400
    assert headers['Access-Control-Allow-Origin'] == "test.com"
    assert body == f"User with email {email} does not exist"


###################################### Testing Functions ##########################################
@patch('src.unsub_error_emails.smtp_error_handler.unsubscribe_user')
def test_unsubscribe_user(mock_unsubscribe_user):
    mock_unsubscribe_user.return_value = "unsub_user_return"
    expected_result = "unsub_user_return"
    actual_result = unsub_error_emails.unsubscribe_user("test@gmail.com")

    mock_unsubscribe_user.assert_called_with("test@gmail.com")
    assert actual_result == expected_result
