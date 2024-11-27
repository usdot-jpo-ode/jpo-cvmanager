from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_intersection as admin_intersection
import api.tests.data.admin_intersection_data as admin_intersection_data
import sqlalchemy
from werkzeug.exceptions import HTTPException
from api.tests.data import auth_data
from common.auth_tools import ENVIRON_USER_KEY
from api.src.errors import ServerErrorException

user_valid = auth_data.get_request_environ()


###################################### Testing Requests ##########################################
# OPTIONS endpoint test
def test_request_options():
    info = admin_intersection.AdminIntersection()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests
@patch("api.src.admin_intersection.get_modify_intersection_data_authorized")
def test_entry_get_intersection(mock_get_modify_intersection_data):
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.args = admin_intersection_data.request_args_intersection_good
    mock_get_modify_intersection_data.return_value = {}
    with patch("api.src.admin_intersection.request", req):
        status = admin_intersection.AdminIntersection()
        (body, code, headers) = status.get()

        mock_get_modify_intersection_data.assert_called_once_with(
            admin_intersection_data.request_args_intersection_good["intersection_id"],
            user_valid,
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


@patch("api.src.admin_intersection.get_modify_intersection_data_authorized")
def test_entry_get_all(mock_get_modify_intersection_data):
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.args = admin_intersection_data.request_args_all_good
    mock_get_modify_intersection_data.return_value = {}
    with patch("api.src.admin_intersection.request", req):
        status = admin_intersection.AdminIntersection()
        (body, code, headers) = status.get()

        mock_get_modify_intersection_data.assert_called_once_with(
            admin_intersection_data.request_args_all_good["intersection_id"],
            user_valid,
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


# Test schema for string value
def test_entry_get_schema_str():
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.json = admin_intersection_data.request_args_str_bad
    with patch("api.src.admin_intersection.request", req):
        status = admin_intersection.AdminIntersection()
        with pytest.raises(HTTPException):
            status.get()


# PATCH endpoint tests
@patch("api.src.admin_intersection.modify_intersection_authorized")
def test_entry_patch(mock_modify_intersection):
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.json = admin_intersection_data.request_json_good
    mock_modify_intersection.return_value = {}
    with patch("api.src.admin_intersection.request", req):
        status = admin_intersection.AdminIntersection()
        (body, code, headers) = status.patch()

        mock_modify_intersection.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_patch_schema():
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.json = admin_intersection_data.request_json_bad
    with patch("api.src.admin_intersection.request", req):
        status = admin_intersection.AdminIntersection()
        with pytest.raises(HTTPException):
            status.patch()


# DELETE endpoint tests
@patch("api.src.admin_intersection.delete_intersection_authorized")
def test_entry_delete_intersection(mock_delete_intersection):
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.args = admin_intersection_data.request_args_intersection_good
    mock_delete_intersection.return_value = {}
    with patch("api.src.admin_intersection.request", req):
        status = admin_intersection.AdminIntersection()
        (body, code, headers) = status.delete()

        mock_delete_intersection.assert_called_once_with(
            admin_intersection_data.request_args_intersection_good["intersection_id"],
            user_valid,
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


###################################### Testing Functions ##########################################
# get_intersection_data
@patch("api.src.admin_intersection.pgquery.query_db")
def test_get_intersection_data_all(mock_query_db):
    mock_query_db.return_value = admin_intersection_data.get_intersection_data_return
    expected_intersection_data = admin_intersection_data.expected_get_intersection_all
    expected_query = admin_intersection_data.expected_get_intersection_query_all
    actual_result = admin_intersection.get_intersection_data_authorized(
        "all", user_valid
    )

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_intersection_data


@patch("api.src.admin_intersection.pgquery.query_db")
def test_get_intersection_data_intersection(mock_query_db):
    mock_query_db.return_value = admin_intersection_data.get_intersection_data_return
    expected_intersection_data = admin_intersection_data.expected_get_intersection_all[
        0
    ]
    expected_query = admin_intersection_data.expected_get_intersection_query_one
    actual_result = admin_intersection.get_intersection_data_authorized(
        "1123", user_valid
    )

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_intersection_data


@patch("api.src.admin_intersection.pgquery.query_db")
def test_get_intersection_data_none(mock_query_db):
    # get Intersection should return an empty object if there are no Intersections with specified IP
    mock_query_db.return_value = []
    expected_intersection_data = {}
    expected_query = admin_intersection_data.expected_get_intersection_query_one
    actual_result = admin_intersection.get_intersection_data_authorized(
        "1123", user_valid
    )

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_intersection_data


# get_modify_intersection_data
@patch("api.src.admin_intersection.get_intersection_data_authorized")
def test_get_modify_intersection_data_all(mock_get_intersection_data):
    mock_get_intersection_data.return_value = ["test intersection data"]
    expected_intersection_data = {"intersection_data": ["test intersection data"]}
    actual_result = admin_intersection.get_modify_intersection_data_authorized(
        "all", user_valid
    )

    assert actual_result == expected_intersection_data


@patch(
    "api.src.admin_intersection.admin_new_intersection.get_allowed_selections_authorized"
)
@patch("api.src.admin_intersection.get_intersection_data_authorized")
def test_get_modify_intersection_data_intersection(
    mock_get_intersection_data, mock_get_allowed_selections
):
    mock_get_allowed_selections.return_value = "test selections"
    mock_get_intersection_data.return_value = "test intersection data"
    expected_intersection_data = {
        "intersection_data": "test intersection data",
        "allowed_selections": "test selections",
    }
    actual_result = admin_intersection.get_modify_intersection_data_authorized(
        "1123", user_valid
    )

    assert actual_result == expected_intersection_data


# modify_intersection
@patch("api.src.admin_intersection.admin_new_intersection.check_safe_input")
@patch("api.src.admin_intersection.pgquery.write_db")
def test_modify_intersection_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg = {"message": "Intersection successfully modified"}
    actual_msg = admin_intersection.modify_intersection_authorized(
        admin_intersection_data.request_json_good, user_valid
    )

    calls = [
        call(admin_intersection_data.modify_intersection_sql),
        call(admin_intersection_data.add_org_sql),
        call(admin_intersection_data.remove_org_sql_3),
        call(admin_intersection_data.remove_org_sql_4),
        call(admin_intersection_data.add_rsu_sql),
        call(admin_intersection_data.remove_rsu_sql_3),
        call(admin_intersection_data.remove_rsu_sql_4),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_intersection.admin_new_intersection.check_safe_input")
@patch("api.src.admin_intersection.pgquery.write_db")
def test_modify_intersection_check_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False

    with pytest.raises(ServerErrorException) as exc_info:
        admin_intersection.modify_intersection_authorized(
            admin_intersection_data.request_json_good, user_valid
        )

    assert (
        str(exc_info.value)
        == "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    )
    mock_pgquery.assert_has_calls([])


@patch("api.src.admin_intersection.admin_new_intersection.check_safe_input")
@patch("api.src.admin_intersection.pgquery.write_db")
def test_modify_intersection_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")

    with pytest.raises(ServerErrorException) as exc_info:
        admin_intersection.modify_intersection_authorized(
            admin_intersection_data.request_json_good, user_valid
        )

    assert str(exc_info.value) == "Encountered unknown issue"


@patch("api.src.admin_intersection.admin_new_intersection.check_safe_input")
@patch("api.src.admin_intersection.pgquery.write_db")
def test_modify_intersection_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)

    with pytest.raises(ServerErrorException) as exc_info:
        admin_intersection.modify_intersection_authorized(
            admin_intersection_data.request_json_good, user_valid
        )

    assert str(exc_info.value) == "SQL issue encountered"


# delete_intersection
@patch("api.src.admin_intersection.pgquery.write_db")
def test_delete_intersection(mock_write_db):
    expected_result = {"message": "Intersection successfully deleted"}
    actual_result = admin_intersection.delete_intersection_authorized(
        "1111", user_valid
    )

    calls = [
        call(admin_intersection_data.delete_intersection_calls[0]),
        call(admin_intersection_data.delete_intersection_calls[1]),
        call(admin_intersection_data.delete_intersection_calls[2]),
    ]
    mock_write_db.assert_has_calls(calls)
    assert actual_result == expected_result
