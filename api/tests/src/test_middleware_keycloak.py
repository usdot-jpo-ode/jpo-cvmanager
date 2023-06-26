from unittest.mock import MagicMock, patch, Mock
from werkzeug.wrappers import Request, Response
from src import middleware_keycloak
import os

@patch('src.middleware_keycloak.pgquery.query_db')
@patch('src.middleware_keycloak.KeycloakOpenID')
def test_get_user_role_no_data(mock_keycloak, mock_query_db):
    mock_query_db.return_value = []
    
    mock_instance = mock_keycloak.return_value
    mock_instance.introspect.return_value = {"active": True}
    mock_instance.userinfo.return_value = {"email": "test@example.com"}

    result = middleware_keycloak.get_user_role("dummy_token")

    assert result == None


@patch('src.middleware_keycloak.pgquery.query_db')
@patch('src.middleware_keycloak.KeycloakOpenID')
def test_get_user_role_with_data(mock_keycloak,mock_query_db):
    # mock
    mock_query_db.return_value = ["test"]   
    mock_instance = mock_keycloak.return_value
    mock_instance.introspect.return_value = {"active": True}
    mock_instance.userinfo.return_value = {"email": "test@example.com"}

    result = middleware_keycloak.get_user_role("dummy_token")
    # check
    print(result)
    expected_result = ["test"]
    assert(result == expected_result)

@patch('src.middleware_keycloak.get_user_role')
@patch('src.middleware_keycloak.Request')
@patch('src.middleware_keycloak.KeycloakOpenID')
def test_middleware_keycloak_class_call_options(mock_kc, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "OPTIONS"
    mock_request.return_value.path = "/rsu-google-auth"

    # create instance
    app = Mock()
    middleware_keycloak_instance = middleware_keycloak.Middleware(app)

    # call
    environ = {}
    start_response = Mock()
    middleware_keycloak_instance(environ, start_response)

    # check
    mock_get_user_role.assert_not_called()
    mock_request.assert_called_once_with(environ)
    mock_kc.assert_not_called()
    app.assert_called_once_with(environ, start_response)

@patch('src.middleware_keycloak.get_user_role')
@patch('src.middleware_keycloak.Request')
#@patch('src.middleware_keycloak.id_token')
@patch('src.middleware_keycloak.pgquery.query_db')
@patch('src.middleware_keycloak.KeycloakOpenID')
#@patch('src.middleware_keycloak.google.auth.transport.requests.Request')
def test_middleware_keycloak_class_call_user_unauthorized(mock_keycloak, mock_query_db,mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/rsu-google-auth"
    mock_request.return_value.headers = {
        'Authorization': 'test'
    }

    # create instance
    app = Mock()
    middleware_keycloak_instance = middleware_keycloak.Middleware(app)
    # call

    environ = {}
    start_response = Mock()
    middleware_keycloak_instance(environ, start_response)

    #mock_response.return_value = resp
    #mock_google_request.return_value = "test"
    #ock_id_token.verify_oauth2_token.return_value = "test"
    #mock_get_user_role.return_value = None

    # # mock environment variables
    # os.environ['GOOGLE_CLIENT_ID'] = "test"

    # # create instance
    # app = Mock()
    # app.return_value = "test"
    # middleware_keycloak_instance = middleware_keycloak.middleware_keycloak(app)

    # # call
    # environ = {
    #     'GOOGLE_CLIENT_ID': 'test'
    # }
    # start_response = Mock()
    # result = middleware_keycloak_instance(environ, start_response)
    
    # mock
    # mock_query_db.return_value = ["test"]   
    # mock_instance = mock_keycloak.return_value
    # mock_instance.introspect.return_value = {"active": True}
    # mock_instance.userinfo.return_value = {"email": "test@example.com"}
    
    # # check
    print("before check")
    result = middleware_keycloak.get_user_role("test")
    print("result:")
    print(result)
    # mock_get_user_role.assert_called_once_with("test")
    # mock_request.assert_called_once_with(environ)
    # mock_id_token.verify_oauth2_token.assert_called_once_with("test", mock_google_request.return_value, "test")
    #mock_response.assert_called_once_with('User unauthorized', status=401)
    # app.assert_not_called()
    ##expected_result = "test"
    #assert(result != none)
    #assert(result == False)
    





# @patch('src.middleware_keycloak.get_user_role')
# @patch('src.middleware_keycloak.Request')
# @patch('src.middleware_keycloak.id_token')
# # @patch('src.middleware_keycloak.google.auth.transport.requests.Request')
# @patch('src.middleware_keycloak.Response')
# def test_middleware_keycloak_class_call_user_authorized(mock_response,mock_id_token, mock_request, mock_get_user_role):
#     # mock
#     mock_request.return_value.method = "GET"
#     mock_request.return_value.path = "/rsu-google-auth"
#     mock_request.return_value.headers = {
#         'Authorization': 'test'
#     }
#     resp = MagicMock()
#     resp.return_value = "test"
#     mock_response.return_value = resp
#     mock_google_request.return_value = "test"
#     mock_id_token.verify_oauth2_token.return_value = "test"

#     # mock environment variables
#     os.environ['GOOGLE_CLIENT_ID'] = "test"

#     # create instance
#     app = Mock()
#     app.return_value = "test"
#     middleware_keycloak_instance = middleware_keycloak.middleware_keycloak(app)

#     # call
#     environ = {
#         'GOOGLE_CLIENT_ID': 'test'
#     }
#     start_response = Mock()
#     result = middleware_keycloak_instance(environ, start_response)

#     # check
#     mock_get_user_role.assert_called_once_with("test")
#     mock_request.assert_called_once_with(environ)
#     mock_id_token.verify_oauth2_token.assert_called_once_with("test", mock_google_request.return_value, "test")
#     app.assert_called_once_with(environ, start_response)
#     expected_result = resp.return_value
#     assert(result == expected_result)

# @patch('src.middleware_keycloak.get_user_role')
# @patch('src.middleware_keycloak.Request')
# @patch('src.middleware_keycloak.id_token')
# @patch('src.middleware_keycloak.Response')
# def test_middleware_keycloak_class_call_exception(mock_response, mock_id_token, mock_request, mock_get_user_role):
#     # mock
#     mock_request.return_value.method = "GET"
#     mock_request.return_value.path = "/rsu-google-auth"
#     mock_id_token.verify_oauth2_token.side_effect = Exception("test")
#     resp = MagicMock()
#     mock_response.return_value = resp
#     mock_response.return_value.return_value = "test"

#     # create instance
#     app = Mock()
#     middleware_keycloak_instance = middleware_keycloak.middleware_keycloak(app)

#     # call
#     environ = {
#         'GOOGLE_CLIENT_ID': 'test'
#     }
#     start_response = Mock()
#     result = middleware_keycloak_instance(environ, start_response)

#     # check
#     mock_get_user_role.assert_not_called()
#     app.assert_not_called()
#     mock_request.assert_called_once_with(environ)
#     mock_response.assert_called_once_with('Authorization failed', status=401)
#     expected_result = resp.return_value
#     assert(result == expected_result)