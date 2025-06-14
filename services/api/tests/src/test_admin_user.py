from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_user as admin_user
import api.tests.data.admin_user_data as admin_user_data
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from api.tests.data import auth_data
from werkzeug.exceptions import BadRequest, HTTPException, InternalServerError

user_valid = auth_data.get_request_environ()


# ##################################### Testing Requests ##########################################
# OPTIONS endpoint test
def test_request_options():
    info = admin_user.AdminUser()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests
@patch("api.src.admin_user.get_modify_user_data_authorized")
@patch(
    "api.src.admin_user.request",
    MagicMock(
        args=admin_user_data.request_args_good,
    ),
)
def test_entry_get(mock_get_modify_user_data):
    mock_get_modify_user_data.return_value = {}
    status = admin_user.AdminUser()
    (body, code, headers) = status.get()

    mock_get_modify_user_data.assert_called_once_with(
        admin_user_data.request_args_good["user_email"]
    )
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


# Test schema for string value
@patch(
    "api.src.admin_user.request",
    MagicMock(
        json=admin_user_data.request_args_bad,
    ),
)
def test_entry_get_schema_str():
    status = admin_user.AdminUser()
    with pytest.raises(HTTPException):
        status.get()


# PATCH endpoint tests
@patch("api.src.admin_user.modify_user")
@patch(
    "api.src.admin_user.request",
    MagicMock(
        json=admin_user_data.request_json_good,
    ),
)
def test_entry_patch(mock_modify_user):
    mock_modify_user.return_value = {}
    status = admin_user.AdminUser()
    (body, code, headers) = status.patch()

    mock_modify_user.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_user.request",
    MagicMock(
        json=admin_user_data.request_json_bad,
    ),
)
def test_entry_patch_schema():
    status = admin_user.AdminUser()
    with pytest.raises(HTTPException):
        status.patch()


# DELETE endpoint tests
@patch("api.src.admin_user.delete_user_authorized")
@patch(
    "api.src.admin_user.request",
    MagicMock(
        args=admin_user_data.request_args_good,
    ),
)
def test_entry_delete_user(mock_delete_user):
    mock_delete_user.return_value = {}
    status = admin_user.AdminUser()
    (body, code, headers) = status.delete()

    mock_delete_user.assert_called_once_with(
        admin_user_data.request_args_good["user_email"]
    )
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_user.request",
    MagicMock(
        args=admin_user_data.request_args_bad,
    ),
)
def test_entry_delete_schema():
    status = admin_user.AdminUser()
    with pytest.raises(HTTPException):
        status.delete()


# ##################################### Testing Functions ##########################################
# get_user_data
@patch("api.src.admin_user.pgquery.query_db")
def test_get_user_data_all(mock_query_db):
    mock_query_db.return_value = admin_user_data.get_user_data_return
    expected_result = admin_user_data.get_user_data_expected
    expected_query = admin_user_data.expected_get_user_query
    actual_result = admin_user.get_user_data("all", user_valid, [])

    mock_query_db.assert_called_with(expected_query, params={})
    assert actual_result == expected_result


@patch("api.src.admin_user.pgquery.query_db")
def test_get_user_data_email(mock_query_db):
    mock_query_db.return_value = admin_user_data.get_user_data_return
    expected_result = admin_user_data.get_user_data_expected[0]
    expected_query = admin_user_data.expected_get_user_query_one
    actual_result = admin_user.get_user_data("test@gmail.com", user_valid, [])

    mock_query_db.assert_called_with(
        expected_query, params=admin_user_data.expected_get_user_query_one_params
    )
    assert actual_result == expected_result


@patch("api.src.admin_user.pgquery.query_db")
def test_get_user_data_none(mock_query_db):
    # get user should return an empty object if there are no users with specified email
    mock_query_db.return_value = []
    expected_result = {}
    actual_result = admin_user.get_user_data(
        "test@gmail.com",
        user_valid,
        [],
    )

    mock_query_db.assert_called_with(
        admin_user_data.expected_get_user_query_one,
        params=admin_user_data.expected_get_user_query_one_params,
    )
    assert actual_result == expected_result


# get_modify_user_data
@patch("api.src.admin_user.get_user_data")
def test_get_modify_rsu_data_all(mock_get_user_data):
    mock_get_user_data.return_value = ["test user data"]
    expected_rsu_data = {"user_data": ["test user data"]}
    actual_result = admin_user.get_modify_user_data_authorized(
        "all",
    )

    assert actual_result == expected_rsu_data


@patch("api.src.admin_user.admin_new_user.get_allowed_selections")
@patch("api.src.admin_user.get_user_data")
def test_get_modify_rsu_data_rsu(mock_get_user_data, mock_get_allowed_selections):
    mock_get_allowed_selections.return_value = "test selections"
    mock_get_user_data.return_value = "test user data"
    expected_rsu_data = {
        "user_data": "test user data",
        "allowed_selections": "test selections",
    }
    actual_result = admin_user.get_modify_user_data_authorized(
        "test@gmail.com",
    )

    assert actual_result == expected_rsu_data


# check_safe_input
def test_check_safe_input():
    expected_result = True
    actual_result = admin_user.check_safe_input(admin_user_data.request_json_good)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_user.check_safe_input(
        admin_user_data.request_json_unsafe_input
    )
    assert actual_result == expected_result


# modify_user
@patch("api.src.admin_user.check_safe_input")
@patch("api.src.admin_user.admin_new_user.check_email")
@patch("api.src.admin_user.pgquery.write_db")
def test_modify_user_success(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "User successfully modified"}
    actual_msg = admin_user.modify_user(
        "test@gmail.com", admin_user_data.request_json_good
    )

    calls = [
        call(
            admin_user_data.modify_user_sql, params=admin_user_data.modify_user_params
        ),
        call(admin_user_data.add_org_sql, params=admin_user_data.add_org_params),
        call(admin_user_data.modify_org_sql, params=admin_user_data.modify_org_params),
        call(admin_user_data.remove_org_sql, params=admin_user_data.remove_org_params),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_user.admin_new_user.check_email")
@patch("api.src.admin_user.pgquery.write_db")
def test_modify_user_email_check_fail(mock_pgquery, mock_check_email):
    mock_check_email.return_value = False

    with pytest.raises(BadRequest) as exc_info:
        admin_user.modify_user("test@gmail.com", admin_user_data.request_json_good)

    assert str(exc_info.value) == "400 Bad Request: Email is not valid"
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_user.check_safe_input")
@patch("api.src.admin_user.admin_new_user.check_email")
@patch("api.src.admin_user.pgquery.write_db")
def test_modify_user_check_fail(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = False

    with pytest.raises(BadRequest) as exc_info:
        admin_user.modify_user(
            "test@gmail.com",
            admin_user_data.request_json_good,
        )

    assert (
        str(exc_info.value)
        == "400 Bad Request: No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )


@patch("api.src.admin_user.check_safe_input")
@patch("api.src.admin_user.admin_new_user.check_email")
@patch("api.src.admin_user.pgquery.write_db")
def test_modify_user_generic_exception(
    mock_pgquery, mock_check_email, mock_check_safe_input
):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = SQLAlchemyError("Test")

    with pytest.raises(InternalServerError) as exc_info:
        admin_user.modify_user("test@gmail.com", admin_user_data.request_json_good)

    assert (
        str(exc_info.value)
        == "500 Internal Server Error: Encountered unknown issue executing query"
    )


@patch("api.src.admin_user.check_safe_input")
@patch("api.src.admin_user.admin_new_user.check_email")
@patch("api.src.admin_user.pgquery.write_db")
def test_modify_user_sql_exception(
    mock_pgquery, mock_check_email, mock_check_safe_input
):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = IntegrityError("", {}, orig)

    with pytest.raises(InternalServerError) as exc_info:
        admin_user.modify_user("test@gmail.com", admin_user_data.request_json_good)

    assert str(exc_info.value) == "500 Internal Server Error: SQL issue encountered"


# delete_user
@patch("api.src.admin_user.pgquery.write_db")
def test_delete_user(mock_write_db):
    expected_result = {"message": "User successfully deleted"}
    actual_result = admin_user.delete_user_authorized("test@gmail.com")

    calls = [
        call(admin_user_data.delete_user_calls[0], params={"email": "test@gmail.com"}),
        call(admin_user_data.delete_user_calls[1], params={"email": "test@gmail.com"}),
    ]
    mock_write_db.assert_has_calls(calls)
    assert actual_result == expected_result
