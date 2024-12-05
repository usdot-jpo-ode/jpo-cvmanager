from unittest.mock import patch, MagicMock
import pytest
import api.src.admin_new_intersection as admin_new_intersection
import api.tests.data.admin_new_intersection_data as admin_new_intersection_data
from werkzeug.exceptions import HTTPException


# ##################################### Testing Requests ##########################################
def test_request_options():
    info = admin_new_intersection.AdminNewIntersection()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_intersection.get_allowed_selections")
def test_entry_get(mock_get_allowed_selections):
    mock_get_allowed_selections.return_value = {}
    status = admin_new_intersection.AdminNewIntersection()
    (body, code, headers) = status.get()

    mock_get_allowed_selections.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch("api.src.admin_new_intersection.add_intersection_authorized")
@patch(
    "api.src.admin_new_intersection.request",
    MagicMock(
        json=admin_new_intersection_data.request_json_good,
    ),
)
def test_entry_post(mock_add_intersection):
    mock_add_intersection.return_value = {}
    status = admin_new_intersection.AdminNewIntersection()
    (body, code, headers) = status.post()

    mock_add_intersection.assert_called_once()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert body == {}


@patch(
    "api.src.admin_new_intersection.request",
    MagicMock(
        json=admin_new_intersection_data.request_json_bad,
    ),
)
def test_entry_post_schema_bad_json():
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
