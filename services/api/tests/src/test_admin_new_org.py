from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_org as admin_new_org
import api.tests.data.admin_new_org_data as admin_new_org_data
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from werkzeug.exceptions import HTTPException
from werkzeug.exceptions import BadRequest, InternalServerError


###################################### Testing Requests ##########################################
def test_request_options():
    info = admin_new_org.AdminNewOrg()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "POST"


@patch("api.src.admin_new_org.add_organization")
@patch(
    "api.src.admin_new_org.request",
    MagicMock(
        json=admin_new_org_data.request_json_good,
    ),
)
def test_entry_post(mock_add_org):
    mock_add_org.return_value = {}
    status = admin_new_org.AdminNewOrg()
    (body, code, headers) = status.post()

    mock_add_org.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_new_org.request",
    MagicMock(
        json=admin_new_org_data.request_json_bad,
    ),
)
def test_entry_post_schema():
    status = admin_new_org.AdminNewOrg()
    with pytest.raises(HTTPException):
        status.post()


###################################### Testing Functions ##########################################
def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_org.check_safe_input(admin_new_org_data.good_input)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_org.check_safe_input(admin_new_org_data.bad_input)
    assert actual_result == expected_result


@patch("api.src.admin_new_org.check_safe_input")
@patch("api.src.admin_new_org.pgquery.write_db")
def test_add_org_success_commsignia(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "New organization successfully added"}
    actual_msg = admin_new_org.add_organization(admin_new_org_data.request_json_good)

    calls = [call(admin_new_org_data.org_insert_query)]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_new_org.check_safe_input")
@patch("api.src.admin_new_org.pgquery.write_db")
def test_add_org_safety_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False

    with pytest.raises(BadRequest) as exc_info:
        admin_new_org.add_organization(admin_new_org_data.request_json_good)

    assert (
        str(exc_info.value)
        == "400 Bad Request: No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_new_org.check_safe_input")
@patch("api.src.admin_new_org.pgquery.write_db")
def test_add_org_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = SQLAlchemyError("Test")

    with pytest.raises(InternalServerError) as exc_info:
        admin_new_org.add_organization(admin_new_org_data.request_json_good)

    assert (
        str(exc_info.value)
        == "500 Internal Server Error: Encountered unknown issue executing query"
    )


@patch("api.src.admin_new_org.check_safe_input")
@patch("api.src.admin_new_org.pgquery.write_db")
def test_add_org_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = IntegrityError("", {}, orig)

    with pytest.raises(InternalServerError) as exc_info:
        admin_new_org.add_organization(admin_new_org_data.request_json_good)

    assert str(exc_info.value) == "500 Internal Server Error: SQL issue encountered"
