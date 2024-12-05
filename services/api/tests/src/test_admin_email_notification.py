from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_email_notification as admin_notification
import api.tests.data.admin_notification_data as admin_notification_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

from common.auth_tools import ENVIRON_USER_KEY
from common.errors import ServerErrorException, UnauthorizedException

###################################### Testing Requests ##########################################


# OPTIONS endpoint test
def test_request_options():
    info = admin_notification.AdminNotification()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests
@patch("api.src.admin_email_notification.get_modify_notification_data_authorized")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args=admin_notification_data.request_args_good,
    ),
)
def test_entry_get(mock_get_modify_notification_data):
    mock_get_modify_notification_data.return_value = {}
    status = admin_notification.AdminNotification()
    (body, code, headers) = status.get()

    mock_get_modify_notification_data.assert_called_once_with(
        admin_notification_data.request_args_good["user_email"]
    )
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


# Test schema for string value
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args=admin_notification_data.request_args_bad,
    ),
)
def test_entry_get_schema_str():
    status = admin_notification.AdminNotification()
    with pytest.raises(HTTPException):
        status.get()


# PATCH endpoint tests
@patch("api.src.admin_email_notification.modify_notification_authorized")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        json=admin_notification_data.request_json_good,
    ),
)
def test_entry_patch(mock_modify_notification):
    mock_modify_notification.return_value = {}
    status = admin_notification.AdminNotification()
    (body, code, headers) = status.patch()

    mock_modify_notification.assert_called_once_with(
        "test@gmail.com", admin_notification_data.request_json_good
    )
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args={},
    ),
)
def test_entry_patch_schema():
    status = admin_notification.AdminNotification()
    with pytest.raises(HTTPException):
        status.patch()


# DELETE endpoint tests
@patch("api.src.admin_email_notification.delete_notification_authorized")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args=admin_notification_data.request_args_delete_good,
    ),
)
def test_entry_delete_user(mock_delete_notification):
    mock_delete_notification.return_value = {}
    status = admin_notification.AdminNotification()
    (body, code, headers) = status.delete()

    mock_delete_notification.assert_called_once_with(
        admin_notification_data.request_args_delete_good["email"],
        admin_notification_data.request_args_delete_good["email_type"],
    )
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args=admin_notification_data.request_args_bad,
    ),
)
def test_entry_delete_schema():
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
    actual_result = admin_notification.get_notification_data("test@gmail.com")

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_result


# get_modify_notification_data
@patch("api.src.admin_email_notification.get_notification_data")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args={},
    ),
)
def test_get_modify_notification_data_all(mock_get_notification_data):
    mock_get_notification_data.return_value = [
        {
            "email": "test@gmail.com",
            "first_name": "first",
            "last_name": "last",
            "email_type": "test type",
        }
    ]
    expected_notification_data = {
        "notification_data": [
            {
                "email": "test@gmail.com",
                "first_name": "first",
                "last_name": "last",
                "email_type": "test type",
            }
        ]
    }
    actual_result = admin_notification.get_modify_notification_data_authorized(
        "test@gmail.com"
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
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args={},
    ),
)
def test_modify_notification_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "Email notification successfully modified"}
    actual_msg = admin_notification.modify_notification_authorized(
        "test@gmail.com",
        admin_notification_data.request_json_good,
    )

    calls = [call(admin_notification_data.modify_notification_sql)]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args={},
    ),
)
def test_modify_notification_check_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False
    with pytest.raises(ServerErrorException) as exc_info:
        admin_notification.modify_notification_authorized(
            "test@gmail.com", admin_notification_data.request_json_good
        )

    assert (
        str(exc_info.value)
        == "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args={},
    ),
)
def test_modify_notification_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")
    with pytest.raises(ServerErrorException) as exc_info:
        admin_notification.modify_notification_authorized(
            "test@gmail.com", admin_notification_data.request_json_good
        )

    assert str(exc_info.value) == "Encountered unknown issue"


@patch("api.src.admin_email_notification.check_safe_input")
@patch("api.src.admin_email_notification.pgquery.write_db")
@patch(
    "api.src.admin_email_notification.request",
    MagicMock(
        args={},
    ),
)
def test_modify_notification_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)

    with pytest.raises(ServerErrorException) as exc_info:
        admin_notification.modify_notification_authorized(
            "test@gmail.com", admin_notification_data.request_json_good
        )

    assert str(exc_info.value) == "SQL issue encountered"


# delete_notification
@patch("api.src.admin_email_notification.pgquery.write_db")
def test_delete_notification(mock_write_db):
    expected_result = {"message": "Email notification successfully deleted"}
    actual_result = admin_notification.delete_notification_authorized(
        "test@gmail.com", "test type"
    )
    mock_write_db.assert_called_with(admin_notification_data.delete_notification_call)
    assert actual_result == expected_result


##################################### Authentication Tests ##########################################
@patch("api.src.admin_email_notification.get_notification_data")
@patch("common.auth_tools.request")
def test_get_modify_notification_data_authorized_self(
    mock_request,
    mock_get_notification_data,
):
    user = auth_data.get_request_environ()
    user.user_info.super_user = False
    mock_request.environ = {ENVIRON_USER_KEY: user}
    expected = "get_notification_data_result"
    mock_get_notification_data.return_value = expected
    actual = admin_notification.get_modify_notification_data_authorized(
        "test@gmail.com"
    )
    assert actual == {"notification_data": expected}
    mock_get_notification_data.assert_called_once_with("test@gmail.com")


@patch("api.src.admin_email_notification.get_notification_data")
@patch("common.auth_tools.request")
def test_get_modify_notification_data_authorized_super_user(
    mock_request,
    mock_get_notification_data,
):
    user = auth_data.get_request_environ()
    user.user_info.super_user = True
    mock_request.environ = {ENVIRON_USER_KEY: user}
    expected = "get_notification_data_result"
    mock_get_notification_data.return_value = expected
    actual = admin_notification.get_modify_notification_data_authorized(
        "mismatch@gmail.com"
    )
    assert actual == {"notification_data": expected}
    mock_get_notification_data.assert_called_once_with("mismatch@gmail.com")


@patch("api.src.admin_email_notification.get_notification_data")
@patch("common.auth_tools.request")
def test_get_modify_notification_data_authorized_invalid_access(
    mock_request,
    mock_get_notification_data,
):
    user = auth_data.get_request_environ()
    user.user_info.super_user = False
    user.user_info.organizations = {}
    mock_request.environ = {ENVIRON_USER_KEY: user}
    with pytest.raises(UnauthorizedException):
        admin_notification.get_modify_notification_data_authorized("mismatch@gmail.com")

    mock_get_notification_data.assert_not_called()
