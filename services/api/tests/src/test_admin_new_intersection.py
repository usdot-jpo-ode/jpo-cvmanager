from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_intersection as admin_new_intersection
import api.tests.data.admin_new_intersection_data as admin_new_intersection_data
import common.pgquery as pgquery
from werkzeug.exceptions import HTTPException
from api.tests.data import auth_data
from common.auth_tools import ENVIRON_USER_KEY

user_valid = auth_data.get_request_environ()

###################################### Testing Requests ##########################################


def test_request_options():
    info = admin_new_intersection.AdminNewIntersection()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_intersection.get_allowed_selections_authorized")
def test_entry_get(mock_get_allowed_selections):
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    mock_get_allowed_selections.return_value = {}
    with patch("api.src.admin_new_intersection.request", req):
        status = admin_new_intersection.AdminNewIntersection()
        (body, code, headers) = status.get()

        mock_get_allowed_selections.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


@patch("api.src.admin_new_intersection.add_intersection_authorized")
def test_entry_post(mock_add_intersection):
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.json = admin_new_intersection_data.request_json_good
    mock_add_intersection.return_value = {}, 200
    with patch("api.src.admin_new_intersection.request", req):
        status = admin_new_intersection.AdminNewIntersection()
        (body, code, headers) = status.post()

        mock_add_intersection.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_post_schema_bad_json():
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    req.json = admin_new_intersection_data.request_json_bad
    with patch("api.src.admin_new_intersection.request", req):
        status = admin_new_intersection.AdminNewIntersection()
        with pytest.raises(HTTPException):
            status.post()


###################################### Testing Functions ##########################################
def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_intersection.check_safe_input(
        admin_new_intersection_data.request_json_good
    )
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_intersection.check_safe_input(
        admin_new_intersection_data.bad_input_check_safe_input
    )
    assert actual_result == expected_result
