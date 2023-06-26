from unittest.mock import patch, MagicMock, call
import pytest
import src.admin_new_rsu as admin_new_rsu
import tests.data.admin_new_rsu_data as admin_new_rsu_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################

def test_request_options():
    info = admin_new_rsu.AdminNewRsu()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET,POST'

@patch('src.admin_new_rsu.get_allowed_selections')
def test_entry_get(mock_get_allowed_selections):
    req = MagicMock()
    req.environ = admin_new_rsu_data.request_params_good
    mock_get_allowed_selections.return_value = {}
    with patch("src.admin_new_rsu.request", req):
        status = admin_new_rsu.AdminNewRsu()
        (body, code, headers) = status.get()

        mock_get_allowed_selections.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {}

@patch('src.admin_new_rsu.add_rsu')
def test_entry_post(mock_add_rsu):
    req = MagicMock()
    req.environ = admin_new_rsu_data.request_params_good
    req.json = admin_new_rsu_data.request_json_good
    mock_add_rsu.return_value = {}, 200
    with patch("src.admin_new_rsu.request", req):
        status = admin_new_rsu.AdminNewRsu()
        (body, code, headers) = status.post()

        mock_add_rsu.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {}

def test_entry_post_schema():
    req = MagicMock()
    req.environ = admin_new_rsu_data.request_params_good
    req.json = admin_new_rsu_data.request_json_bad
    with patch("src.admin_new_rsu.request", req):
        status = admin_new_rsu.AdminNewRsu()
        with pytest.raises(HTTPException):
            status.post()

###################################### Testing Functions ##########################################

@patch('src.admin_new_rsu.pgquery')
def test_query_and_return_list(mock_pgquery):
    # sqlalchemy returns a list of tuples. This test replicates the tuple list
    mock_pgquery.query_db.return_value = [('Vendor', 'Model',), ('road',)]
    expected_rsu_data = ['Vendor Model', 'road']
    expected_query = "SELECT * FROM test"
    actual_result = admin_new_rsu.query_and_return_list("SELECT * FROM test")

    mock_pgquery.query_db.assert_called_with(expected_query)
    assert actual_result == expected_rsu_data

@patch('src.admin_new_rsu.query_and_return_list')
def test_get_allowed_selections(mock_query_and_return_list):
    mock_query_and_return_list.return_value = ["test"]
    expected_result = {
        'primary_routes': ['test'],
        'rsu_models': ['test'],
        'ssh_credential_groups': ['test'],
        'snmp_credential_groups': ['test'],
        'snmp_version_groups': ['test'],
        'organizations': ['test']
    }
    actual_result = admin_new_rsu.get_allowed_selections()

    calls = [
        call("SELECT DISTINCT primary_route FROM public.rsus ORDER BY primary_route ASC"),
        call("SELECT manufacturers.name as manufacturer, rsu_models.name as model FROM public.rsu_models JOIN public.manufacturers ON rsu_models.manufacturer = manufacturers.manufacturer_id ORDER BY manufacturer, model ASC"),
        call("SELECT nickname FROM public.rsu_credentials ORDER BY nickname ASC"),
        call("SELECT nickname FROM public.snmp_credentials ORDER BY nickname ASC"),
        call("SELECT nickname FROM public.snmp_versions ORDER BY nickname ASC"),
        call("SELECT name FROM public.organizations ORDER BY name ASC")
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

@patch('src.admin_new_rsu.check_safe_input')
@patch('src.admin_new_rsu.pgquery.insert_db')
def test_add_rsu_success_commsignia(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {"message": "New RSU successfully added"} , 200
    actual_msg, actual_code = admin_new_rsu.add_rsu(admin_new_rsu_data.mock_post_body_commsignia)

    calls = [
        call(admin_new_rsu_data.rsu_query_commsignia),
        call(admin_new_rsu_data.rsu_org_query)
        ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('src.admin_new_rsu.check_safe_input')
@patch('src.admin_new_rsu.pgquery.insert_db')
def test_add_rsu_success_yunex(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {"message": "New RSU successfully added"} , 200
    actual_msg, actual_code = admin_new_rsu.add_rsu(admin_new_rsu_data.mock_post_body_yunex)

    calls = [
        call(admin_new_rsu_data.rsu_query_yunex),
        call(admin_new_rsu_data.rsu_org_query)
        ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('src.admin_new_rsu.check_safe_input')
@patch('src.admin_new_rsu.pgquery.insert_db')
def test_add_rsu_safety_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False
    expected_msg, expected_code = {"message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"}, 500
    actual_msg, actual_code = admin_new_rsu.add_rsu(admin_new_rsu_data.mock_post_body_commsignia)

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('src.admin_new_rsu.check_safe_input')
@patch('src.admin_new_rsu.pgquery.insert_db')
def test_add_rsu_fail_yunex_no_scms_id(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {"message": "SCMS ID must be specified"}, 500
    actual_msg, actual_code = admin_new_rsu.add_rsu(admin_new_rsu_data.mock_post_body_yunex_no_scms)

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('src.admin_new_rsu.check_safe_input')
@patch('src.admin_new_rsu.pgquery.insert_db')
def test_add_rsu_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception('Test')
    expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
    actual_msg, actual_code = admin_new_rsu.add_rsu(admin_new_rsu_data.mock_post_body_commsignia)

    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('src.admin_new_rsu.check_safe_input')
@patch('src.admin_new_rsu.pgquery.insert_db')
def test_add_rsu_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({'D': 'SQL issue encountered'},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
    expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
    actual_msg, actual_code = admin_new_rsu.add_rsu(admin_new_rsu_data.mock_post_body_commsignia)

    assert actual_msg == expected_msg
    assert actual_code == expected_code
