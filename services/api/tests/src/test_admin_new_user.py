from unittest.mock import patch, MagicMock, call
import pytest
import api.src.admin_new_user as admin_new_user
import api.tests.data.admin_new_user_data as admin_new_user_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################

def test_request_options():
    info = admin_new_user.AdminNewUser()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET,POST'

@patch('api.src.admin_new_user.get_allowed_selections')
def test_entry_get(mock_get_allowed_selections):
    req = MagicMock()
    req.environ = admin_new_user_data.request_params_good
    mock_get_allowed_selections.return_value = {}
    with patch("api.src.admin_new_user.request", req):
        status = admin_new_user.AdminNewUser()
        (body, code, headers) = status.get()

        mock_get_allowed_selections.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "test.com"
        assert body == {}

@patch('api.src.admin_new_user.add_user')
def test_entry_post(mock_add_user):
    req = MagicMock()
    req.environ = admin_new_user_data.request_params_good
    req.json = admin_new_user_data.request_json_good
    mock_add_user.return_value = {}, 200
    with patch("api.src.admin_new_user.request", req):
        status = admin_new_user.AdminNewUser()
        (body, code, headers) = status.post()

        mock_add_user.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "test.com"
        assert body == {}

def test_entry_post_schema():
    req = MagicMock()
    req.environ = admin_new_user_data.request_params_good
    req.json = admin_new_user_data.request_json_bad
    with patch("api.src.admin_new_user.request", req):
        status = admin_new_user.AdminNewUser()
        with pytest.raises(HTTPException):
            status.post()

###################################### Testing Functions ##########################################

@patch('api.src.admin_new_user.pgquery')
def test_query_and_return_list(mock_pgquery):
    # sqlalchemy returns a list of tuples. This test replicates the tuple list
    mock_pgquery.query_db.return_value = [('Vendor', 'Model',), ('road',)]
    expected_rsu_data = ['Vendor Model', 'road']
    expected_query = "SELECT * FROM test"
    actual_result = admin_new_user.query_and_return_list("SELECT * FROM test")

    mock_pgquery.query_db.assert_called_with(expected_query)
    assert actual_result == expected_rsu_data

@patch('api.src.admin_new_user.query_and_return_list')
def test_get_allowed_selections(mock_query_and_return_list):
    mock_query_and_return_list.return_value = ["test"]
    expected_result = {
        'organizations': ['test'],
        'roles': ['test']
    }
    actual_result = admin_new_user.get_allowed_selections()

    calls = [
        call("SELECT name FROM public.organizations ORDER BY name ASC"),
        call("SELECT name FROM public.roles ORDER BY name")
        ]
    mock_query_and_return_list.assert_has_calls(calls)
    assert actual_result == expected_result

def test_check_email():
    expected_result = True
    actual_result = admin_new_user.check_email(admin_new_user_data.good_input['email'])
    assert actual_result == expected_result

def test_check_email_bad():
    expected_result = False
    actual_result = admin_new_user.check_email(admin_new_user_data.bad_input['email'])
    assert actual_result == expected_result

def test_check_safe_input():
    expected_result = True
    actual_result = admin_new_user.check_safe_input(admin_new_user_data.good_input)
    assert actual_result == expected_result

def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_new_user.check_safe_input(admin_new_user_data.bad_input)
    assert actual_result == expected_result

@patch('api.src.admin_new_user.check_safe_input')
@patch('api.src.admin_new_user.check_email')
@patch('api.src.admin_new_user.pgquery.write_db')
def test_add_user_success(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    expected_msg, expected_code = {"message": "New user successfully added"}, 200
    actual_msg, actual_code = admin_new_user.add_user(admin_new_user_data.request_json_good)

    calls = [
        call(admin_new_user_data.user_insert_query),
        call(admin_new_user_data.user_org_insert_query)
        ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_user.check_email')
@patch('api.src.admin_new_user.pgquery.write_db')
def test_add_user_email_fail(mock_pgquery, mock_check_email):
    mock_check_email.return_value = False
    expected_msg, expected_code = {"message": "Email is not valid"}, 500
    actual_msg, actual_code = admin_new_user.add_user(admin_new_user_data.request_json_good)

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_user.check_safe_input')
@patch('api.src.admin_new_user.check_email')
@patch('api.src.admin_new_user.pgquery.write_db')
def test_add_user_check_fail(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = False
    expected_msg, expected_code = {"message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"}, 500
    actual_msg, actual_code = admin_new_user.add_user(admin_new_user_data.request_json_good)

    calls = []
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_user.check_safe_input')
@patch('api.src.admin_new_user.check_email')
@patch('api.src.admin_new_user.pgquery.write_db')
def test_add_user_generic_exception(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = Exception('Test')
    expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
    actual_msg, actual_code = admin_new_user.add_user(admin_new_user_data.request_json_good)

    assert actual_msg == expected_msg
    assert actual_code == expected_code

@patch('api.src.admin_new_user.check_safe_input')
@patch('api.src.admin_new_user.check_email')
@patch('api.src.admin_new_user.pgquery.write_db')
def test_add_user_sql_exception(mock_pgquery, mock_check_email, mock_check_safe_input):
    mock_check_email.return_value = True
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({'D': 'SQL issue encountered'},)
    mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
    expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
    actual_msg, actual_code = admin_new_user.add_user(admin_new_user_data.request_json_good)

    assert actual_msg == expected_msg
    assert actual_code == expected_code