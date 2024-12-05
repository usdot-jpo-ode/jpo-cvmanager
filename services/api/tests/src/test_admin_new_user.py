from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_user as admin_new_user
import api.tests.data.admin_new_user_data as admin_new_user_data
import sqlalchemy
from werkzeug.exceptions import HTTPException
from api.tests.data import auth_data
from common.auth_tools import PermissionResult
from common.errors import ServerErrorException

user_valid = auth_data.get_request_environ()


###################################### Testing Requests ##########################################
def test_request_options():
    info = admin_new_user.AdminNewUser()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_user.get_allowed_selections")
def test_entry_get(mock_get_allowed_selections):
    req = MagicMock()
    mock_get_allowed_selections.return_value = {}
    with patch("api.src.admin_new_user.request", req):
        status = admin_new_user.AdminNewUser()
        (body, code, headers) = status.get()

        mock_get_allowed_selections.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


@patch("api.src.admin_new_user.add_user_authorized")
def test_entry_post(mock_add_user):
    req = MagicMock()
    req.json = admin_new_user_data.request_json_good
    mock_add_user.return_value = {}
    with patch("api.src.admin_new_user.request", req):
        status = admin_new_user.AdminNewUser()
        (body, code, headers) = status.post()

        mock_add_user.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_post_schema():
    req = MagicMock()
    req.json = admin_new_user_data.request_json_bad
    with patch("api.src.admin_new_user.request", req):
        status = admin_new_user.AdminNewUser()
        with pytest.raises(HTTPException):
            status.post()


###################################### Testing Functions ##########################################
@patch("common.pgquery.query_and_return_list")
def test_get_allowed_selections(mock_query_and_return_list):
    mock_query_and_return_list.return_value = ["test"]
    expected_result = {"organizations": ["test"], "roles": ["test"]}
    actual_result = admin_new_user.get_allowed_selections(
        PermissionResult(
            allowed=True, user=user_valid, message="", qualified_orgs=["test"]
        )
    )
    calls = [
        call("SELECT name FROM public.organizations ORDER BY name ASC"),
        call("SELECT name FROM public.roles ORDER BY name"),
    ]
    mock_query_and_return_list.assert_has_calls(calls)
    assert actual_result == expected_result


def test_check_email():
    expected_result = True
    actual_result = admin_new_user.check_email(admin_new_user_data.good_input["email"])
    assert actual_result == expected_result


def test_check_email_bad():
    expected_result = False
    actual_result = admin_new_user.check_email(admin_new_user_data.bad_input["email"])
    assert actual_result == expected_result


def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_user.check_safe_input(admin_new_user_data.good_input)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_user.check_safe_input(admin_new_user_data.bad_input)
    assert actual_result == expected_result


@patch("api.src.admin_new_user.check_safe_input")
@patch("api.src.admin_new_user.check_email")
@patch("api.src.admin_new_user.pgquery.write_db")
def test_add_user_success(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "New user successfully added"}
    actual_msg = admin_new_user.add_user_authorized(
        admin_new_user_data.request_json_good
    )

    calls = [
        call(admin_new_user_data.user_insert_query),
        call(admin_new_user_data.user_org_insert_query),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_new_user.check_email")
@patch("api.src.admin_new_user.pgquery.write_db")
def test_add_user_email_fail(mock_pgquery, mock_check_email):
    mock_check_email.return_value = False
    with pytest.raises(ServerErrorException) as exc_info:
        admin_new_user.add_user_authorized(admin_new_user_data.request_json_good)

    assert str(exc_info.value) == "Email is not valid"
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_new_user.check_safe_input")
@patch("api.src.admin_new_user.check_email")
@patch("api.src.admin_new_user.pgquery.write_db")
def test_add_user_check_fail(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = False

    with pytest.raises(ServerErrorException) as exc_info:
        admin_new_user.add_user_authorized(admin_new_user_data.request_json_good)

    assert (
        str(exc_info.value)
        == "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_new_user.check_safe_input")
@patch("api.src.admin_new_user.check_email")
@patch("api.src.admin_new_user.pgquery.write_db")
def test_add_user_generic_exception(
    mock_pgquery, mock_check_email, mock_check_safe_input
):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")

    with pytest.raises(ServerErrorException) as exc_info:
        admin_new_user.add_user_authorized(admin_new_user_data.request_json_good)

    assert str(exc_info.value) == "Encountered unknown issue"


@patch("api.src.admin_new_user.check_safe_input")
@patch("api.src.admin_new_user.check_email")
@patch("api.src.admin_new_user.pgquery.write_db")
def test_add_user_sql_exception(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)

    with pytest.raises(ServerErrorException) as exc_info:
        admin_new_user.add_user_authorized(admin_new_user_data.request_json_good)

    assert str(exc_info.value) == "SQL issue encountered"
