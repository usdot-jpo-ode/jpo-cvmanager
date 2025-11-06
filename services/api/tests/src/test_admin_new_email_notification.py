from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_email_notification as admin_new_notification
import api.tests.data.admin_new_notification_data as admin_new_notification_data
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from werkzeug.exceptions import HTTPException
from werkzeug.exceptions import BadRequest, InternalServerError


###################################### Testing Requests ##########################################
def test_request_options():
    info = admin_new_notification.AdminNewNotification()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_email_notification.add_notification_authorized")
@patch(
    "api.src.admin_new_email_notification.request",
    MagicMock(
        json=admin_new_notification_data.request_json_good,
    ),
)
def test_entry_post(mock_add_notification):
    mock_add_notification.return_value = {}
    status = admin_new_notification.AdminNewNotification()
    (body, code, headers) = status.post()

    mock_add_notification.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_new_email_notification.request",
    MagicMock(
        json=admin_new_notification_data.request_json_bad,
    ),
)
def test_entry_post_schema():
    status = admin_new_notification.AdminNewNotification()
    with pytest.raises(HTTPException):
        status.post()


###################################### Testing Functions ##########################################
def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_notification.check_safe_input(
        admin_new_notification_data.good_input
    )
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_notification.check_safe_input(
        admin_new_notification_data.bad_input
    )
    assert actual_result == expected_result


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "New email notification successfully added"}
    actual_msg = admin_new_notification.add_notification_authorized(
        "test@gmail.com", admin_new_notification_data.request_json_good
    )

    calls = [call(admin_new_notification_data.notification_insert_query)]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_safety_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False

    with pytest.raises(BadRequest) as exc_info:
        admin_new_notification.add_notification_authorized(
            "test@gmail.com", admin_new_notification_data.request_json_good
        )

    assert (
        str(exc_info.value)
        == "400 Bad Request: No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = SQLAlchemyError("Test")

    with pytest.raises(InternalServerError) as exc_info:
        admin_new_notification.add_notification_authorized(
            "test@gmail.com", admin_new_notification_data.request_json_good
        )

    assert (
        str(exc_info.value)
        == "500 Internal Server Error: Encountered unknown issue executing query"
    )


@patch("api.src.admin_new_email_notification.check_safe_input")
@patch("api.src.admin_new_email_notification.pgquery.write_db")
def test_add_notification_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = IntegrityError("", {}, orig)

    with pytest.raises(InternalServerError) as exc_info:
        admin_new_notification.add_notification_authorized(
            "test@gmail.com", admin_new_notification_data.request_json_good
        )

    assert str(exc_info.value) == "500 Internal Server Error: SQL issue encountered"
