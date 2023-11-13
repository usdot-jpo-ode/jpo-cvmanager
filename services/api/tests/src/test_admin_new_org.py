from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_org as admin_new_org
import api.tests.data.admin_new_org_data as admin_new_org_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################

def test_request_options():
    info = admin_new_org.AdminNewOrg()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'POST'

@patch('api.src.admin_new_org.add_organization')
def test_entry_post(mock_add_org):
    req = MagicMock()
    req.environ = admin_new_org_data.request_params_good
    req.json = admin_new_org_data.request_json_good
    mock_add_org.return_value = {}, 200
    with patch("api.src.admin_new_org.request", req):
        status = admin_new_org.AdminNewOrg()
        (body, code, headers) = status.post()

        mock_add_org.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "test.com"
        assert body == {}

def test_entry_post_schema():
    req = MagicMock()
    req.environ = admin_new_org_data.request_params_good
    req.json = admin_new_org_data.request_json_bad
    with patch("api.src.admin_new_org.request", req):
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

@patch('api.src.admin_new_org.check_safe_input')
@patch('api.src.admin_new_org.pgquery.write_db')
def test_add_org_success_commsignia(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {"message": "New organization successfully added"}, 200
    actual_msg, actual_code = admin_new_org.add_organization(admin_new_org_data.request_json_good)

    calls = [
        call(admin_new_org_data.org_insert_query)
        ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_org.check_safe_input')
@patch('api.src.admin_new_org.pgquery.write_db')
def test_add_org_safety_fail(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = False
    expected_msg, expected_code = {"message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"}, 500
    actual_msg, actual_code = admin_new_org.add_organization(admin_new_org_data.request_json_good)

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_org.check_safe_input')
@patch('api.src.admin_new_org.pgquery.write_db')
def test_add_org_generic_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception('Test')
    expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
    actual_msg, actual_code = admin_new_org.add_organization(admin_new_org_data.request_json_good)

    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_org.check_safe_input')
@patch('api.src.admin_new_org.pgquery.write_db')
def test_add_org_sql_exception(mock_pgquery, mock_check_safe_input):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({'D': 'SQL issue encountered'},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
    expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
    actual_msg, actual_code = admin_new_org.add_organization(admin_new_org_data.request_json_good)

    assert actual_msg == expected_msg
    assert actual_code == expected_code
