from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_email_notification as admin_new_notification
import api.tests.data.admin_new_notification_data as admin_new_notification_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################


def test_request_options():
    info = admin_new_notification.AdminNewNotification()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_email_notification.add_notification")
def test_entry_post(mock_add_notification):
    req = MagicMock()
    req.environ = admin_new_notification_data.request_params_good
    req.json = admin_new_notification_data.request_json_good
    mock_add_notification.return_value = {}, 200
    with patch("api.src.admin_new_email_notification.request", req):
        status = admin_new_notification.AdminNewNotification()
        (body, code, headers) = status.post()

        mock_add_notification.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_post_schema():
    req = MagicMock()
    req.environ = admin_new_notification_data.request_params_good
    req.json = admin_new_notification_data.request_json_bad
    with patch("api.src.admin_new_email_notification.request", req):
        status = admin_new_notification.AdminNewNotification()
        with pytest.raises(HTTPException):
            status.post()


###################################### Testing Functions ##########################################


def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_notification.check_safe_input(admin_new_notification_data.good_input)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_notification.check_safe_input(admin_new_notification_data.bad_input)
    assert actual_result == expected_result


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {
        "message": "New email notification successfully added"
    }, 200
    actual_msg, actual_code = admin_new_notification.add_notification(
        admin_new_notification_data.request_json_good
    )

    calls = [call(admin_new_notification_data.notification_insert_query)]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_safety_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False
    expected_msg, expected_code = {
        "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    }, 500
    actual_msg, actual_code = admin_new_notification.add_notification(
        admin_new_notification_data.request_json_good
    )

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")
    expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
    actual_msg, actual_code = admin_new_notification.add_notification(
        admin_new_notification_data.request_json_good
    )

    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
    expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
    actual_msg, actual_code = admin_new_notification.add_notification(
        admin_new_notification_data.request_json_good
    )

    assert actual_msg == expected_msg
    assert actual_code == expected_code
