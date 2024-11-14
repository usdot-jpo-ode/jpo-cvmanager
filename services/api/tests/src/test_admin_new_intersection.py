from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_intersection as admin_new_intersection
import api.tests.data.admin_new_intersection_data as admin_new_intersection_data
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################


def test_request_options():
    info = admin_new_intersection.AdminNewIntersection()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,POST"


@patch("api.src.admin_new_intersection.get_allowed_selections")
def test_entry_get(mock_get_allowed_selections):
    req = MagicMock()
    req.environ = admin_new_intersection_data.request_params_good
    mock_get_allowed_selections.return_value = {}
    with patch("api.src.admin_new_intersection.request", req):
        status = admin_new_intersection.AdminNewIntersection()
        (body, code, headers) = status.get()

        mock_get_allowed_selections.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


@patch("api.src.admin_new_intersection.add_intersection")
def test_entry_post(mock_add_intersection):
    req = MagicMock()
    req.environ = admin_new_intersection_data.request_params_good
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
    req.environ = admin_new_intersection_data.request_params_good
    req.json = admin_new_intersection_data.request_json_bad
    with patch("api.src.admin_new_intersection.request", req):
        status = admin_new_intersection.AdminNewIntersection()
        with pytest.raises(HTTPException):
            status.post()


###################################### Testing Functions ##########################################
@patch("api.src.admin_new_intersection.pgquery")
def test_query_and_return_list(mock_pgquery):
    # sqlalchemy returns a list of tuples. This test replicates the tuple list
    mock_pgquery.query_db.return_value = [
        (
            "AAA",
            "BBB",
        ),
        ("CCC",),
    ]
    expected_intersection_data = ["AAA BBB", "CCC"]
    expected_query = "SELECT * FROM test"
    actual_result = admin_new_intersection.query_and_return_list("SELECT * FROM test")

    mock_pgquery.query_db.assert_called_with(expected_query)
    assert actual_result == expected_intersection_data


@patch("api.src.admin_new_intersection.query_and_return_list")
def test_get_allowed_selections(mock_query_and_return_list):
    mock_query_and_return_list.return_value = ["test"]
    expected_result = {
        "organizations": ["test"],
        "rsus": ["test"],
    }
    actual_result = admin_new_intersection.get_allowed_selections()

    calls = [
        call("SELECT name FROM public.organizations ORDER BY name ASC"),
        call(
            "SELECT ipv4_address::text AS ipv4_address FROM public.rsus ORDER BY ipv4_address ASC"
        ),
    ]
    mock_query_and_return_list.assert_has_calls(calls)
    assert actual_result == expected_result


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
