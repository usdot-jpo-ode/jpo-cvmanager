from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_rsu as admin_new_rsu
import api.tests.data.admin_new_rsu_data as admin_new_rsu_data
import sqlalchemy
from werkzeug.exceptions import HTTPException
from api.tests.data import auth_data
from werkzeug.exceptions import BadRequest, InternalServerError

user_valid = auth_data.get_request_environ()


# ##################################### Testing Requests ##########################################
def test_request_options():
    info = admin_new_rsu.AdminNewRsu()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_rsu.get_allowed_selections")
@patch(
    "api.src.admin_new_rsu.request",
    MagicMock(
        json=admin_new_rsu_data.request_json_good,
    ),
)
def test_entry_get(mock_get_allowed_selections):
    mock_get_allowed_selections.return_value = {}
    status = admin_new_rsu.AdminNewRsu()
    (body, code, headers) = status.get()

    mock_get_allowed_selections.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch("api.src.admin_new_rsu.add_rsu_authorized")
@patch(
    "api.src.admin_new_rsu.request",
    MagicMock(
        json=admin_new_rsu_data.request_json_good,
    ),
)
def test_entry_post(mock_add_rsu):
    mock_add_rsu.return_value = {}
    status = admin_new_rsu.AdminNewRsu()
    (body, code, headers) = status.post()

    mock_add_rsu.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_new_rsu.request",
    MagicMock(
        json=admin_new_rsu_data.request_json_bad,
    ),
)
def test_entry_post_schema():
    status = admin_new_rsu.AdminNewRsu()
    with pytest.raises(HTTPException):
        status.post()


###################################### Testing Functions ##########################################
@patch("common.pgquery.query_and_return_list")
def test_get_allowed_selections(mock_query_and_return_list):
    mock_query_and_return_list.return_value = ["test"]
    expected_result = {
        "primary_routes": ["test"],
        "rsu_models": ["test"],
        "ssh_credential_groups": ["test"],
        "snmp_credential_groups": ["test"],
        "snmp_version_groups": ["test"],
        "organizations": ["test"],
    }
    actual_result = admin_new_rsu.get_allowed_selections(user_valid)

    calls = [
        call(
            "SELECT DISTINCT primary_route FROM public.rsus ORDER BY primary_route ASC"
        ),
        call(
            "SELECT manufacturers.name as manufacturer, rsu_models.name as model FROM public.rsu_models JOIN public.manufacturers ON rsu_models.manufacturer = manufacturers.manufacturer_id ORDER BY manufacturer, model ASC"
        ),
        call("SELECT nickname FROM public.rsu_credentials ORDER BY nickname ASC"),
        call("SELECT nickname FROM public.snmp_credentials ORDER BY nickname ASC"),
        call("SELECT nickname FROM public.snmp_protocols ORDER BY nickname ASC"),
        call("SELECT name FROM public.organizations ORDER BY name ASC"),
    ]
    mock_query_and_return_list.assert_has_calls(calls)
    assert actual_result == expected_result


def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_rsu.check_safe_input(admin_new_rsu_data.good_input)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_rsu.check_safe_input(admin_new_rsu_data.bad_input)
    assert actual_result == expected_result


@patch("api.src.admin_new_rsu.check_safe_input")
@patch("api.src.admin_new_rsu.pgquery.write_db")
def test_add_rsu_success_commsignia(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "New RSU successfully added"}
    actual_msg = admin_new_rsu.add_rsu_authorized(
        admin_new_rsu_data.mock_post_body_commsignia
    )

    calls = [
        call(admin_new_rsu_data.rsu_query_commsignia),
        call(admin_new_rsu_data.rsu_org_query),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_new_rsu.check_safe_input")
@patch("api.src.admin_new_rsu.pgquery.write_db")
def test_add_rsu_success_yunex(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "New RSU successfully added"}
    actual_msg = admin_new_rsu.add_rsu_authorized(
        admin_new_rsu_data.mock_post_body_yunex
    )

    calls = [
        call(admin_new_rsu_data.rsu_query_yunex),
        call(admin_new_rsu_data.rsu_org_query),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_new_rsu.check_safe_input")
@patch("api.src.admin_new_rsu.pgquery.write_db")
def test_add_rsu_safety_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False

    with pytest.raises(BadRequest) as exc_info:
        admin_new_rsu.add_rsu_authorized(admin_new_rsu_data.mock_post_body_commsignia)

    assert (
        str(exc_info.value)
        == "400 Bad Request: No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_new_rsu.check_safe_input")
@patch("api.src.admin_new_rsu.pgquery.write_db")
def test_add_rsu_fail_yunex_no_scms_id(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True

    with pytest.raises(BadRequest) as exc_info:
        admin_new_rsu.add_rsu_authorized(
            admin_new_rsu_data.mock_post_body_yunex_no_scms
        )

    assert str(exc_info.value) == "400 Bad Request: SCMS ID must be specified"
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_new_rsu.check_safe_input")
@patch("api.src.admin_new_rsu.pgquery.write_db")
def test_add_rsu_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")

    with pytest.raises(InternalServerError) as exc_info:
        admin_new_rsu.add_rsu_authorized(admin_new_rsu_data.mock_post_body_commsignia)

    assert str(exc_info.value) == "500 Internal Server Error: Encountered unknown issue"


@patch("api.src.admin_new_rsu.check_safe_input")
@patch("api.src.admin_new_rsu.pgquery.write_db")
def test_add_rsu_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)

    with pytest.raises(InternalServerError) as exc_info:
        admin_new_rsu.add_rsu_authorized(admin_new_rsu_data.mock_post_body_commsignia)

    assert str(exc_info.value) == "500 Internal Server Error: SQL issue encountered"
