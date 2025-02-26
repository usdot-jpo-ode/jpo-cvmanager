from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_org as admin_org
import api.tests.data.admin_org_data as admin_org_data
from sqlalchemy.exc import IntegrityError
from werkzeug.exceptions import HTTPException
from api.tests.data import auth_data
from werkzeug.exceptions import BadRequest, Conflict, InternalServerError

user_valid = auth_data.get_request_environ()


# ##################################### Testing Requests ##########################################
# OPTIONS endpoint test
def test_request_options():
    info = admin_org.AdminOrg()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests
@patch("api.src.admin_org.get_modify_org_data_authorized")
def test_entry_get(mock_get_modify_org_data):
    req = MagicMock()
    req.args = admin_org_data.request_json_get_delete_good
    mock_get_modify_org_data.return_value = {}
    with patch("api.src.admin_org.request", req):
        status = admin_org.AdminOrg()
        (body, code, headers) = status.get()

        mock_get_modify_org_data.assert_called_once_with(
            admin_org_data.request_json_get_delete_good["org_name"]
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


# Test schema for string value
def test_entry_get_schema_str():
    req = MagicMock()
    req.args = admin_org_data.request_json_get_delete_bad
    with patch("api.src.admin_org.request", req):
        status = admin_org.AdminOrg()
        with pytest.raises(HTTPException):
            status.get()


# PATCH endpoint tests
@patch("api.src.admin_org.modify_org_authorized")
def test_entry_patch(mock_modify_org):
    req = MagicMock()
    req.json = admin_org_data.request_json_good
    mock_modify_org.return_value = {}
    with patch("api.src.admin_org.request", req):
        status = admin_org.AdminOrg()
        (body, code, headers) = status.patch()

        mock_modify_org.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_patch_schema():
    req = MagicMock()
    req.json = admin_org_data.request_json_bad
    with patch("api.src.admin_org.request", req):
        status = admin_org.AdminOrg()
        with pytest.raises(HTTPException):
            status.patch()


# DELETE endpoint tests
@patch("api.src.admin_org.delete_org_authorized")
def test_entry_delete_user(mock_delete_org):
    req = MagicMock()
    req.json = admin_org_data.request_json_get_delete_good
    mock_delete_org.return_value = {"message": "Organization successfully deleted"}
    with patch("api.src.admin_org.request", req):
        status = admin_org.AdminOrg()
        (body, code, headers) = status.delete()

        mock_delete_org.assert_called_once_with(
            admin_org_data.request_json_get_delete_good["org_name"]
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {"message": "Organization successfully deleted"}


def test_entry_delete_schema():
    req = MagicMock()
    req.json = admin_org_data.request_json_get_delete_bad
    with patch("api.src.admin_org.request", req):
        status = admin_org.AdminOrg()
        with pytest.raises(HTTPException):
            status.delete()


# ##################################### Testing Functions ##########################################
# get_all_orgs
@patch("api.src.admin_org.pgquery.query_db")
def test_get_all_orgs(mock_query_db):
    mock_query_db.return_value = admin_org_data.get_all_orgs_pgdb_return
    expected_result = admin_org_data.get_all_orgs_result
    expected_query = admin_org_data.get_all_orgs_sql
    actual_result = admin_org.get_all_orgs(user_valid.user_info.organizations)

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_result


# get_org_data
@patch("api.src.admin_org.pgquery.query_db")
def test_get_org_data(mock_query_db):
    mock_query_db.side_effect = [
        admin_org_data.get_org_data_user_return,
        admin_org_data.get_org_data_rsu_return,
        admin_org_data.get_org_data_intersection_return,
    ]
    expected_result = admin_org_data.get_org_data_result
    actual_result = admin_org.get_org_data("Test Org", user_valid)

    calls = [
        call(admin_org_data.get_org_data_user_sql),
        call(admin_org_data.get_org_data_rsu_sql),
        call(admin_org_data.get_org_data_intersection_sql),
    ]
    mock_query_db.assert_has_calls(calls)
    assert actual_result == expected_result


# get_allowed_selections
@patch("api.src.admin_org.pgquery.query_db")
def test_get_allowed_selections(mock_query_db):
    mock_query_db.return_value = admin_org_data.get_allowed_selections_return
    expected_result = admin_org_data.get_allowed_selections_result
    actual_result = admin_org.get_allowed_selections()

    mock_query_db.assert_called_with(admin_org_data.get_allowed_selections_sql)
    assert actual_result == expected_result


# get_modify_org_data
@patch("api.src.admin_org.get_all_orgs")
def test_get_modify_org_data_all(mock_get_all_orgs):
    mock_get_all_orgs.return_value = ["Test Org data"]
    expected_rsu_data = {"org_data": ["Test Org data"]}
    actual_result = admin_org.get_modify_org_data_authorized(
        "all",
    )

    mock_get_all_orgs.assert_called_with(None)
    assert actual_result == expected_rsu_data


@patch("api.src.admin_org.get_allowed_selections")
@patch("api.src.admin_org.get_org_data")
def test_get_modify_org_data_specific(mock_get_org_data, mock_get_allowed_selections):
    mock_get_org_data.return_value = "Test Org data"
    mock_get_allowed_selections.return_value = ["allowed_selections"]
    expected_rsu_data = {
        "org_data": "Test Org data",
        "allowed_selections": ["allowed_selections"],
    }
    actual_result = admin_org.get_modify_org_data_authorized(
        "Test Org",
    )

    mock_get_org_data.assert_called_with("Test Org", True)
    mock_get_allowed_selections.assert_called_with()
    assert actual_result == expected_rsu_data


# check_safe_input
def test_check_safe_input():
    expected_result = True
    actual_result = admin_org.check_safe_input(admin_org_data.request_json_good)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_org.check_safe_input(admin_org_data.request_json_unsafe_input)
    assert actual_result == expected_result


# modify_org
@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_organization_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "Organization successfully modified"}
    actual_msg = admin_org.modify_org_authorized(
        "Test Org", admin_org_data.request_json_good
    )

    calls = [
        call(admin_org_data.modify_org_sql),
        call(admin_org_data.modify_org_add_user_sql),
        call(admin_org_data.modify_org_modify_user_sql),
        call(admin_org_data.modify_org_remove_user_sql),
        call(admin_org_data.modify_org_add_rsu_sql),
        call(admin_org_data.modify_org_remove_rsu_sql),
        call(admin_org_data.modify_org_add_intersection_sql),
        call(admin_org_data.modify_org_remove_intersection_sql),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_org_check_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False

    expected_message = "400 Bad Request: No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    with pytest.raises(BadRequest) as exc_info:
        admin_org.modify_org_authorized("Test Org", admin_org_data.request_json_good)

    mock_pgquery.assert_has_calls([])
    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_org_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")

    expected_message = "500 Internal Server Error: Encountered unknown issue"
    with pytest.raises(InternalServerError) as exc_info:
        admin_org.modify_org_authorized("Test Org", admin_org_data.request_json_good)

    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_org_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = IntegrityError("", {}, orig)

    expected_message = "500 Internal Server Error: SQL issue encountered"
    with pytest.raises(InternalServerError) as exc_info:
        admin_org.modify_org_authorized("Test Org", admin_org_data.request_json_good)

    assert str(exc_info.value) == expected_message


# delete_org
@patch("api.src.admin_org.pgquery.write_db")
@patch("api.src.admin_org.pgquery.query_db")
def test_delete_org(mock_query_db, mock_write_db):
    mock_query_db.return_value = []
    expected_result = {"message": "Organization successfully deleted"}
    actual_result = admin_org.delete_org_authorized("Test Org")

    calls = [
        call(admin_org_data.delete_org_calls[0]),
        call(admin_org_data.delete_org_calls[1]),
        call(admin_org_data.delete_org_calls[2]),
    ]
    mock_write_db.assert_has_calls(calls)
    assert actual_result == expected_result


@patch("api.src.admin_org.pgquery.query_db")
def test_delete_org_failure_orphan_rsu(mock_query_db):
    mock_query_db.return_value = [
        [{"user_id": 1, "count": 2}],
        [{"user_id": 2, "count": 1}],
    ]
    expected_message = "409 Conflict: Cannot delete organization that has one or more RSUs only associated with this organization"
    with pytest.raises(Conflict) as exc_info:
        admin_org.delete_org_authorized("Test Org")

    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.pgquery.query_db")
@patch("api.src.admin_org.check_orphan_rsus")
@patch("api.src.admin_org.check_orphan_intersections")
def test_delete_org_failure_orphan_user(
    mock_orphan_intersections, mock_orphan_rsus, mock_query_db
):
    mock_orphan_intersections.return_value = False
    mock_orphan_rsus.return_value = False
    mock_query_db.return_value = [
        [{"user_id": 1, "count": 2}],
        [{"user_id": 2, "count": 1}],
    ]
    expected_message = "409 Conflict: Cannot delete organization that has one or more users only associated with this organization"
    with pytest.raises(Conflict) as exc_info:
        admin_org.delete_org_authorized("Test Org")

    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.pgquery.query_db")
@patch("api.src.admin_org.check_orphan_rsus")
def test_delete_org_failure_orphan_intersection(mock_orphan_rsus, mock_query_db):
    mock_orphan_rsus.return_value = False
    mock_query_db.return_value = [
        [{"user_id": 1, "count": 2}],
        [{"user_id": 2, "count": 1}],
    ]
    expected_message = "409 Conflict: Cannot delete organization that has one or more Intersections only associated with this organization"
    with pytest.raises(Conflict) as exc_info:
        admin_org.delete_org_authorized("Test Org")

    assert str(exc_info.value) == expected_message
