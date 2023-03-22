from unittest.mock import patch, MagicMock, call
import pytest
import src.admin_user as admin_user
import tests.data.admin_user_data as admin_user_data
import sqlalchemy
from werkzeug.exceptions import HTTPException

###################################### Testing Requests ##########################################

# OPTIONS endpoint test

def test_request_options():
    info = admin_user.AdminUser()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET,PATCH,DELETE'

# GET endpoint tests

@patch('src.admin_user.get_modify_user_data')
def test_entry_get(mock_get_modify_user_data):
    req = MagicMock()
    req.environ = admin_user_data.request_environ
    req.args = admin_user_data.request_args_good
    mock_get_modify_user_data.return_value = {}
    with patch("src.admin_user.request", req):
        status = admin_user.AdminUser()
        (body, code, headers) = status.get()

        mock_get_modify_user_data.assert_called_once_with(admin_user_data.request_args_good['user_email'])
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {}

# Test schema for string value
def test_entry_get_schema_str():
    req = MagicMock()
    req.environ = admin_user_data.request_environ
    req.args = admin_user_data.request_args_bad
    with patch("src.admin_user.request", req):
        status = admin_user.AdminUser()
        with pytest.raises(HTTPException):
            status.get()

# PATCH endpoint tests

@patch('src.admin_user.modify_user')
def test_entry_patch(mock_modify_user):
    req = MagicMock()
    req.environ = admin_user_data.request_environ
    req.json = admin_user_data.request_json_good
    mock_modify_user.return_value = {}, 200
    with patch("src.admin_user.request", req):
        status = admin_user.AdminUser()
        (body, code, headers) = status.patch()

        mock_modify_user.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {}

def test_entry_patch_schema():
    req = MagicMock()
    req.environ = admin_user_data.request_environ
    req.json = admin_user_data.request_json_bad
    with patch("src.admin_user.request", req):
        status = admin_user.AdminUser()
        with pytest.raises(HTTPException):
            status.patch()

# DELETE endpoint tests

@patch('src.admin_user.delete_user')
def test_entry_delete_user(mock_delete_user):
    req = MagicMock()
    req.environ = admin_user_data.request_environ
    req.args = admin_user_data.request_args_good
    mock_delete_user.return_value = {}
    with patch("src.admin_user.request", req):
        status = admin_user.AdminUser()
        (body, code, headers) = status.delete()

        mock_delete_user.assert_called_once_with(admin_user_data.request_args_good['user_email'])
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {}

def test_entry_delete_schema():
    req = MagicMock()
    req.environ = admin_user_data.request_environ
    req.args = admin_user_data.request_args_bad
    with patch("src.admin_user.request", req):
        status = admin_user.AdminUser()
        with pytest.raises(HTTPException):
            status.delete()

###################################### Testing Functions ##########################################

# get_user_data

@patch('src.admin_user.pgquery')
def test_get_user_data_all(mock_pgquery):
  mock_pgquery.query_db.return_value = admin_user_data.get_user_data_return
  expected_result = admin_user_data.get_user_data_expected
  expected_query = admin_user_data.expected_get_user_qeury
  actual_result = admin_user.get_user_data("all")

  mock_pgquery.query_db.assert_called_with(expected_query)
  assert actual_result == expected_result

@patch('src.admin_user.pgquery')
def test_get_user_data_email(mock_pgquery):
  user_email = "test@email.com"
  mock_pgquery.query_db.return_value = admin_user_data.get_user_data_return
  expected_result = admin_user_data.get_user_data_expected[0]
  expected_query = admin_user_data.expected_get_user_qeury + f" WHERE email = '{user_email}'"
  actual_result = admin_user.get_user_data(user_email)

  mock_pgquery.query_db.assert_called_with(expected_query)
  assert actual_result == expected_result

@patch('src.admin_user.pgquery')
def test_get_user_data_none(mock_pgquery):
  # get user should return an empty object if there are no users with specified email
  user_email = "test2@email.com"
  mock_pgquery.query_db.return_value = []
  expected_result = {}
  expected_query = admin_user_data.expected_get_user_qeury + f" WHERE email = '{user_email}'"
  actual_result = admin_user.get_user_data(user_email)

  mock_pgquery.query_db.assert_called_with(expected_query)
  assert actual_result == expected_result

# get_modify_user_data

@patch('src.admin_user.get_user_data')
def test_get_modify_rsu_data_all(mock_get_user_data):
  mock_get_user_data.return_value = ["test user data"]
  expected_rsu_data = { 
    "user_data": ["test user data"]
  }
  actual_result = admin_user.get_modify_user_data("all")

  assert actual_result == expected_rsu_data

@patch('src.admin_user.admin_new_user.get_allowed_selections')
@patch('src.admin_user.get_user_data')
def test_get_modify_rsu_data_rsu(mock_get_user_data, mock_get_allowed_selections):
  mock_get_allowed_selections.return_value = "test selections"
  mock_get_user_data.return_value = "test user data"
  expected_rsu_data = { 
    "user_data": "test user data",
    "allowed_selections": "test selections"
  }
  actual_result = admin_user.get_modify_user_data("test@email.com")

  assert actual_result == expected_rsu_data

# check_safe_input

def test_check_safe_input():
  expected_result = True
  actual_result = admin_user.check_safe_input(admin_user_data.request_json_good)
  assert actual_result == expected_result

def test_check_safe_input_bad():
  expected_result = False
  actual_result = admin_user.check_safe_input(admin_user_data.request_json_unsafe_input)
  assert actual_result == expected_result

# modify_user

@patch('src.admin_user.check_safe_input')
@patch('src.admin_user.admin_new_user.check_email')
@patch('src.admin_user.pgquery.insert_db')
def test_modify_user_success(mock_pgquery, mock_check_email, mock_check_safe_input):
  mock_check_email.return_value = True
  mock_check_safe_input.return_value = True
  expected_msg, expected_code = {"message": "User successfully modified"}, 200
  actual_msg, actual_code = admin_user.modify_user(admin_user_data.request_json_good)

  calls = [
      call(admin_user_data.modify_user_sql),
      call(admin_user_data.add_org_sql),
      call(admin_user_data.modify_org_sql),
      call(admin_user_data.remove_org_sql)
      ]
  mock_pgquery.assert_has_calls(calls)
  assert actual_msg == expected_msg
  assert actual_code == expected_code

@patch('src.admin_user.admin_new_user.check_email')
@patch('src.admin_user.pgquery.insert_db')
def test_modify_user_email_check_fail(mock_pgquery, mock_check_email):
  mock_check_email.return_value = False
  expected_msg, expected_code = {"message": "Email is not valid"}, 500
  actual_msg, actual_code = admin_user.modify_user(admin_user_data.request_json_good)

  calls = []
  mock_pgquery.assert_has_calls(calls)
  assert actual_msg == expected_msg
  assert actual_code == expected_code

@patch('src.admin_user.check_safe_input')
@patch('src.admin_user.admin_new_user.check_email')
@patch('src.admin_user.pgquery.insert_db')
def test_modify_user_check_fail(mock_pgquery, mock_check_email, mock_check_safe_input):
  mock_check_email.return_value = True
  mock_check_safe_input.return_value = False
  expected_msg, expected_code = {"message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"}, 500
  actual_msg, actual_code = admin_user.modify_user(admin_user_data.request_json_good)

  calls = []
  mock_pgquery.assert_has_calls(calls)
  assert actual_msg == expected_msg
  assert actual_code == expected_code

@patch('src.admin_user.check_safe_input')
@patch('src.admin_user.admin_new_user.check_email')
@patch('src.admin_user.pgquery.insert_db')
def test_modify_user_generic_exception(mock_pgquery, mock_check_email, mock_check_safe_input):
  mock_check_email.return_value = True
  mock_check_safe_input.return_value = True
  mock_pgquery.side_effect = Exception('Test')
  expected_msg, expected_code = {"message": "Encountered unknown issue"}, 500
  actual_msg, actual_code = admin_user.modify_user(admin_user_data.request_json_good)

  assert actual_msg == expected_msg
  assert actual_code == expected_code

@patch('src.admin_user.check_safe_input')
@patch('src.admin_user.admin_new_user.check_email')
@patch('src.admin_user.pgquery.insert_db')
def test_modify_user_sql_exception(mock_pgquery, mock_check_email, mock_check_safe_input):
  mock_check_email.return_value = True
  mock_check_safe_input.return_value = True
  orig = MagicMock()
  orig.args = ({'D': 'SQL issue encountered'},)
  mock_pgquery.side_effect = sqlalchemy.exc.IntegrityError("", {}, orig)
  expected_msg, expected_code = {"message": "SQL issue encountered"}, 500
  actual_msg, actual_code = admin_user.modify_user(admin_user_data.request_json_good)

  assert actual_msg == expected_msg
  assert actual_code == expected_code

# delete_user

@patch('src.admin_user.pgquery.insert_db')
def test_delete_user(mock_insert_db):
  expected_result = {"message": "User successfully deleted"}
  actual_result = admin_user.delete_user("test@email.com")

  calls = [
    call(admin_user_data.delete_user_calls[0]),
    call(admin_user_data.delete_user_calls[1])
    ]
  mock_insert_db.assert_has_calls(calls)
  assert actual_result == expected_result