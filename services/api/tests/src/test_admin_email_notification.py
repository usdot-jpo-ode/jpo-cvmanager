from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_email_notification as admin_notification
import api.tests.data.admin_notification_data as admin_notification_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################

# OPTIONS endpoint test


def test_request_options():
    info = admin_notification.AdminNotification()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests


@patch("api.src.admin_email_notification.get_modify_notification_data")
def test_entry_get(mock_get_modify_notification_data):
    req = MagicMock()
    req.environ = admin_notification_data.request_environ
    req.args = admin_notification_data.request_args_good
    mock_get_modify_notification_data.return_value = {}
    with patch("api.src.admin_email_notification.request", req):
        status = admin_notification.AdminNotification()
        (body, code, headers) = status.get()

        mock_get_modify_notification_data.assert_called_once_with(
            admin_notification_data.request_args_good["user_email"]
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


# Test schema for string value
def test_entry_get_schema_str():
    req = MagicMock()
    req.environ = admin_notification_data.request_environ
    req.args = admin_notification_data.request_args_bad
    with patch("api.src.admin_email_notification.request", req):
        status = admin_notification.AdminNotification()
        with pytest.raises(HTTPException):
            status.get()


# PATCH endpoint tests


@patch("api.src.admin_email_notification.modify_notification")
def test_entry_patch(mock_modify_notification):
    req = MagicMock()
    req.environ = admin_notification_data.request_environ
    req.json = admin_notification_data.request_json_good
    mock_modify_notification.return_value = {}, 200
    with patch("api.src.admin_email_notification.request", req):
        status = admin_notification.AdminNotification()
        (body, code, headers) = status.patch()

        mock_modify_notification.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_patch_schema():
    req = MagicMock()
    req.environ = admin_notification_data.request_environ
    req.json = admin_notification_data.request_json_bad
    with patch("api.src.admin_email_notification.request", req):
        status = admin_notification.AdminNotification()
        with pytest.raises(HTTPException):
            status.patch()


# DELETE endpoint tests


@patch("api.src.admin_email_notification.delete_notification")
def test_entry_delete_user(mock_delete_notification):
    req = MagicMock()
    req.environ = admin_notification_data.request_environ
    req.args = admin_notification_data.request_args_delete_good
    mock_delete_notification.return_value = {}
    with patch("api.src.admin_email_notification.request", req):
        status = admin_notification.AdminNotification()
        (body, code, headers) = status.delete()

        mock_delete_notification.assert_called_once_with(
            admin_notification_data.request_args_delete_good["email"],
            admin_notification_data.request_args_delete_good["email_type"],
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_delete_schema():
    req = MagicMock()
    req.environ = admin_notification_data.request_environ
    req.args = admin_notification_data.request_args_bad
    with patch("api.src.admin_email_notification.request", req):
        status = admin_notification.AdminNotification()
        with pytest.raises(HTTPException):
            status.delete()


###################################### Testing Functions ##########################################

# get_notification_data


@patch("api.src.admin_email_notification.pgquery.query_db")
def test_get_all_notifications(mock_query_db):
    mock_query_db.return_value = (
        admin_notification_data.get_notification_data_pgdb_return
    )
    expected_result = admin_notification_data.get_notification_data_result
    expected_query = admin_notification_data.get_notification_data_sql
    actual_result = admin_notification.get_notification_data("email@email.com")

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_result


# get_modify_notification_data


@patch("api.src.admin_email_notification.get_notification_data")
def test_get_modify_notification_data_all(mock_get_notification_data):
    mock_get_notification_data.return_value = [
        {
            "email": "email@email.com",
            "first_name": "first",
            "last_name": "last",
            "email_type": "test type",
        }
    ]
    expected_notification_data = {
        "notification_data": [
            {
                "email": "email@email.com",
                "first_name": "first",
                "last_name": "last",
                "email_type": "test type",
            }
        ]
    }
    actual_result = admin_notification.get_modify_notification_data_authorized(
        "email@email.com"
    )

    assert actual_result == expected_notification_data


# check_safe_input


def test_check_safe_input():
    expected_result = True
    actual_result = admin_notification.check_safe_input(
        admin_notification_data.request_json_good
    )
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_notification.check_safe_input(
        admin_notification_data.request_json_unsafe_input
    )
    assert actual_result == expected_result


# modify_notification


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
def test_modify_notification_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {
        "message": "Email notification successfully modified"
    }, 200
    actual_msg, actual_code = admin_notification.modify_notification_authorized(
        admin_notification_data.request_json_good
    )

    calls = [call(admin_notification_data.modify_notification_sql)]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
def test_modify_notification_check_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False
    expected_msg, expected_code = {
        "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    }, 500
    actual_msg, actual_code = admin_notification.modify_notification_authorized(
        admin_notification_data.request_json_good
    )

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
def test_modify_notification_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")
    expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
    actual_msg, actual_code = admin_notification.modify_notification_authorized(
        admin_notification_data.request_json_good
    )

    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
def test_modify_notification_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
    expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
    actual_msg, actual_code = admin_notification.modify_notification_authorized(
        admin_notification_data.request_json_good
    )

    assert actual_msg == expected_msg
    assert actual_code == expected_code


# delete_notification


@patch("api.src.admin_email_notification.pgquery.write_db")
def test_delete_notification(mock_write_db):
    expected_result = {"message": "Email notification successfully deleted"}
    actual_result = admin_notification.delete_notification_authorized(
        "email@email.com", "test type"
    )
    mock_write_db.assert_called_with(admin_notification_data.delete_notification_call)
    assert actual_result == expected_result
