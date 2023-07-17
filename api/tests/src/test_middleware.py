from unittest.mock import MagicMock, patch, Mock
from src import middleware
import os

@patch('src.middleware.pgquery.query_db')
def test_get_user_role_no_data(mock_query_db):
    # mock
    mock_query_db.return_value = []
    
    # call
    idinfo = {
        'email': 'test@gmail.com'
    }
    result = middleware.get_user_role(idinfo)

    # check
    assert(result == None)

@patch('src.middleware.pgquery.query_db')
def test_get_user_role_with_data(mock_query_db):
    # mock
    mock_query_db.return_value = ["test"]
    
    # call
    idinfo = {
        'email': 'test@gmail.com'
    }
    result = middleware.get_user_role(idinfo)

    # check
    expected_result = ["test"]
    assert(result == expected_result)

@patch('src.middleware.get_user_role')
@patch('src.middleware.Request')
@patch('src.middleware.id_token')
def test_middleware_class_call_options(mock_id_token, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "OPTIONS"
    mock_request.return_value.path = "/rsu-google-auth"

    # create instance
    app = Mock()
    middleware_instance = middleware.Middleware(app)

    # call
    environ = {}
    start_response = Mock()
    middleware_instance(environ, start_response)

    # check
    mock_get_user_role.assert_not_called()
    mock_request.assert_called_once_with(environ)
    mock_id_token.assert_not_called()
    app.assert_called_once_with(environ, start_response)

@patch('src.middleware.get_user_role')
@patch('src.middleware.Request')
@patch('src.middleware.id_token')
@patch('src.middleware.google.auth.transport.requests.Request')
@patch('src.middleware.Response')
def test_middleware_class_call_user_unauthorized(mock_response, mock_google_request, mock_id_token, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/rsu-google-auth"
    mock_request.return_value.headers = {
        'Authorization': 'test'
    }
    resp = MagicMock()
    resp.return_value = "test"
    mock_response.return_value = resp
    mock_google_request.return_value = "test"
    mock_id_token.verify_oauth2_token.return_value = "test"
    mock_get_user_role.return_value = None

    # mock environment variables
    os.environ['GOOGLE_CLIENT_ID'] = "test"

    # create instance
    app = Mock()
    app.return_value = "test"
    middleware_instance = middleware.Middleware(app)

    # call
    environ = {
        'GOOGLE_CLIENT_ID': 'test'
    }
    start_response = Mock()
    result = middleware_instance(environ, start_response)

    # check
    mock_get_user_role.assert_called_once_with("test")
    mock_request.assert_called_once_with(environ)
    mock_id_token.verify_oauth2_token.assert_called_once_with("test", mock_google_request.return_value, "test")
    mock_response.assert_called_once_with('User unauthorized', status=401)
    app.assert_not_called()
    assert(result == resp.return_value)

@patch('src.middleware.get_user_role')
@patch('src.middleware.Request')
@patch('src.middleware.id_token')
@patch('src.middleware.google.auth.transport.requests.Request')
@patch('src.middleware.Response')
def test_middleware_class_call_user_authorized(mock_response, mock_google_request, mock_id_token, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/rsu-google-auth"
    mock_request.return_value.headers = {
        'Authorization': 'test'
    }
    resp = MagicMock()
    resp.return_value = "test"
    mock_response.return_value = resp
    mock_google_request.return_value = "test"
    mock_id_token.verify_oauth2_token.return_value = "test"

    # mock environment variables
    os.environ['GOOGLE_CLIENT_ID'] = "test"

    # create instance
    app = Mock()
    app.return_value = "test"
    middleware_instance = middleware.Middleware(app)

    # call
    environ = {
        'GOOGLE_CLIENT_ID': 'test'
    }
    start_response = Mock()
    result = middleware_instance(environ, start_response)

    # check
    mock_get_user_role.assert_called_once_with("test")
    mock_request.assert_called_once_with(environ)
    mock_id_token.verify_oauth2_token.assert_called_once_with("test", mock_google_request.return_value, "test")
    app.assert_called_once_with(environ, start_response)
    expected_result = resp.return_value
    assert(result == expected_result)

@patch('src.middleware.get_user_role')
@patch('src.middleware.Request')
@patch('src.middleware.id_token')
@patch('src.middleware.Response')
def test_middleware_class_call_exception(mock_response, mock_id_token, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/rsu-google-auth"
    mock_id_token.verify_oauth2_token.side_effect = Exception("test")
    resp = MagicMock()
    mock_response.return_value = resp
    mock_response.return_value.return_value = "test"

    # create instance
    app = Mock()
    middleware_instance = middleware.Middleware(app)

    # call
    environ = {
        'GOOGLE_CLIENT_ID': 'test'
    }
    start_response = Mock()
    result = middleware_instance(environ, start_response)

    # check
    mock_get_user_role.assert_not_called()
    app.assert_not_called()
    mock_request.assert_called_once_with(environ)
    mock_response.assert_called_once_with('Authorization failed', status=401)
    expected_result = resp.return_value
    assert(result == expected_result)

@patch('src.middleware.get_user_role')
@patch('src.middleware.Request')
@patch('src.middleware.id_token')
@patch('src.middleware.Response')
def test_middleware_class_call_contact_support(mock_response, mock_id_token, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "POST"
    mock_request.return_value.path = "/contact-support"
    mock_id_token.verify_oauth2_token.side_effect = Exception("test")

    # create instance
    app = Mock()
    middleware_instance = middleware.Middleware(app)

    # call
    environ = {
        'GOOGLE_CLIENT_ID': 'test'
    }
    start_response = Mock()
    middleware_instance(environ, start_response)

    # check
    mock_get_user_role.assert_not_called()
    app.assert_called_once_with(environ, start_response)
    mock_request.assert_called_once_with(environ)