from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_rsu as admin_rsu
import api.tests.data.admin_rsu_data as admin_rsu_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################

# OPTIONS endpoint test


def test_request_options():
    info = admin_rsu.AdminRsu()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests


@patch("api.src.admin_rsu.get_modify_rsu_data")
def test_entry_get_rsu(mock_get_modify_rsu_data):
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.args = admin_rsu_data.request_args_rsu_good
    mock_get_modify_rsu_data.return_value = {}
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        (body, code, headers) = status.get()

        mock_get_modify_rsu_data.assert_called_once_with(
            admin_rsu_data.request_args_rsu_good["rsu_ip"]
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


@patch("api.src.admin_rsu.get_modify_rsu_data")
def test_entry_get_all(mock_get_modify_rsu_data):
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.args = admin_rsu_data.request_args_all_good
    mock_get_modify_rsu_data.return_value = {}
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        (body, code, headers) = status.get()

        mock_get_modify_rsu_data.assert_called_once_with(
            admin_rsu_data.request_args_all_good["rsu_ip"]
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


# Test schema for string value
def test_entry_get_schema_str():
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.json = admin_rsu_data.request_args_str_bad
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        with pytest.raises(HTTPException):
            status.get()


# Test schema for IPv4 string if not "all"
def test_entry_get_schema_ipv4():
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.json = admin_rsu_data.request_args_ipv4_bad
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        with pytest.raises(HTTPException):
            status.get()


# PATCH endpoint tests


@patch("api.src.admin_rsu.modify_rsu")
def test_entry_patch(mock_modify_rsu):
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.json = admin_rsu_data.request_json_good
    mock_modify_rsu.return_value = {}, 200
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        (body, code, headers) = status.patch()

        mock_modify_rsu.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_patch_schema():
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.json = admin_rsu_data.request_json_bad
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        with pytest.raises(HTTPException):
            status.patch()


# DELETE endpoint tests


@patch("api.src.admin_rsu.delete_rsu")
def test_entry_delete_rsu(mock_delete_rsu):
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.args = admin_rsu_data.request_args_rsu_good
    mock_delete_rsu.return_value = {}
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        (body, code, headers) = status.delete()

        mock_delete_rsu.assert_called_once_with(
            admin_rsu_data.request_args_rsu_good["rsu_ip"]
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


# Check single schema that requires IPv4 string
def test_entry_delete_schema():
    req = MagicMock()
    req.environ = admin_rsu_data.request_environ
    req.json = admin_rsu_data.request_args_ipv4_bad
    with patch("api.src.admin_rsu.request", req):
        status = admin_rsu.AdminRsu()
        with pytest.raises(HTTPException):
            status.delete()


###################################### Testing Functions ##########################################

# get_rsu_data


@patch("api.src.admin_rsu.pgquery.query_db")
def test_get_rsu_data_all(mock_query_db):
    mock_query_db.return_value = admin_rsu_data.get_rsu_data_return
    expected_rsu_data = admin_rsu_data.expected_get_rsu_all
    expected_query = admin_rsu_data.expected_get_rsu_query_all
    actual_result = admin_rsu.get_rsu_data("all")

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_rsu_data


@patch("api.src.admin_rsu.pgquery.query_db")
def test_get_rsu_data_rsu(mock_query_db):
    mock_query_db.return_value = admin_rsu_data.get_rsu_data_return
    expected_rsu_data = admin_rsu_data.expected_get_rsu_all[0]
    expected_query = admin_rsu_data.expected_get_rsu_query_one
    actual_result = admin_rsu.get_rsu_data("10.11.81.12")

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_rsu_data


@patch("api.src.admin_rsu.pgquery.query_db")
def test_get_rsu_data_none(mock_query_db):
    # get RSU should return an empty object if there are no RSUs with specified IP
    mock_query_db.return_value = []
    expected_rsu_data = {}
    expected_query = admin_rsu_data.expected_get_rsu_query_one
    actual_result = admin_rsu.get_rsu_data("10.11.81.12")

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_rsu_data


# get_modify_rsu_data


@patch("api.src.admin_rsu.get_rsu_data")
def test_get_modify_rsu_data_all(mock_get_rsu_data):
    mock_get_rsu_data.return_value = ["test rsu data"]
    expected_rsu_data = {"rsu_data": ["test rsu data"]}
    actual_result = admin_rsu.get_modify_rsu_data("all")

    assert actual_result == expected_rsu_data


@patch("api.src.admin_rsu.admin_new_rsu.get_allowed_selections")
@patch("api.src.admin_rsu.get_rsu_data")
def test_get_modify_rsu_data_rsu(mock_get_rsu_data, mock_get_allowed_selections):
    mock_get_allowed_selections.return_value = "test selections"
    mock_get_rsu_data.return_value = "test rsu data"
    expected_rsu_data = {
        "rsu_data": "test rsu data",
        "allowed_selections": "test selections",
    }
    actual_result = admin_rsu.get_modify_rsu_data("10.11.81.13")

    assert actual_result == expected_rsu_data


# modify_rsu


@patch("api.src.admin_rsu.admin_new_rsu.check_safe_input")
@patch("api.src.admin_rsu.pgquery.write_db")
def test_modify_rsu_success(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {"message": "RSU successfully modified"}, 200
    actual_msg, actual_code = admin_rsu.modify_rsu(admin_rsu_data.request_json_good)

    calls = [
        call(admin_rsu_data.modify_rsu_sql),
        call(admin_rsu_data.add_org_sql),
        call(admin_rsu_data.remove_org_sql),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_rsu.admin_new_rsu.check_safe_input")
@patch("api.src.admin_rsu.pgquery.write_db")
def test_modify_rsu_check_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False
    expected_msg, expected_code = {
        "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    }, 500
    actual_msg, actual_code = admin_rsu.modify_rsu(admin_rsu_data.request_json_good)

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_rsu.admin_new_rsu.check_safe_input")
@patch("api.src.admin_rsu.pgquery.write_db")
def test_modify_rsu_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception("Test")
    expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
    actual_msg, actual_code = admin_rsu.modify_rsu(admin_rsu_data.request_json_good)

    assert actual_msg == expected_msg
    assert actual_code == expected_code


@patch("api.src.admin_rsu.admin_new_rsu.check_safe_input")
@patch("api.src.admin_rsu.pgquery.write_db")
def test_modify_rsu_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
    expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
    actual_msg, actual_code = admin_rsu.modify_rsu(admin_rsu_data.request_json_good)

    assert actual_msg == expected_msg
    assert actual_code == expected_code


# delete_rsu


@patch("api.src.admin_rsu.pgquery.write_db")
def test_delete_rsu(mock_write_db):
    expected_result = {"message": "RSU successfully deleted"}
    actual_result = admin_rsu.delete_rsu("10.11.81.12")

    calls = [
        call(admin_rsu_data.delete_rsu_calls[0]),
        call(admin_rsu_data.delete_rsu_calls[1]),
        call(admin_rsu_data.delete_rsu_calls[2]),
        call(admin_rsu_data.delete_rsu_calls[3]),
        call(admin_rsu_data.delete_rsu_calls[4]),
    ]
    mock_write_db.assert_has_calls(calls)
    assert actual_result == expected_result
