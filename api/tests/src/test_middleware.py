from unittest.mock import MagicMock, patch, Mock
from werkzeug.wrappers import Request, Response
from src import middleware
import os


@patch("src.middleware.pgquery.query_db")
@patch("src.middleware.KeycloakOpenID")
def test_get_user_role_no_data(mock_keycloak, mock_query_db):
    mock_query_db.return_value = []

    mock_instance = mock_keycloak.return_value
    mock_instance.introspect.return_value = {"active": True}
    mock_instance.userinfo.return_value = {"email": "test@example.com"}

    result = middleware.get_user_role("dummy_token")

    assert result == None


@patch("src.middleware.pgquery.query_db")
@patch("src.middleware.KeycloakOpenID")
def test_get_user_role_with_data(mock_keycloak, mock_query_db):
    # mock
    mock_query_db.return_value = ["test"]
    mock_instance = mock_keycloak.return_value
    mock_instance.introspect.return_value = {"active": True}
    mock_instance.userinfo.return_value = {"email": "test@example.com"}

    result = middleware.get_user_role("dummy_token")
    # check
    expected_result = ["test"]
    assert result == expected_result


@patch("src.middleware.get_user_role")
@patch("src.middleware.Request")
@patch("src.middleware.KeycloakOpenID")
def test_middleware_class_call_options(mock_kc, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "OPTIONS"
    mock_request.return_value.path = "/user-auth"

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
    mock_kc.assert_not_called()
    app.assert_called_once_with(environ, start_response)


@patch("src.middleware.get_user_role")
@patch("src.middleware.Request")
@patch("src.middleware.Response")
def test_middleware_class_call_user_unauthorized(mock_response, mock_request, mock_get_user_role):
    # create instance
    app = Mock()
    middleware_instance = middleware.Middleware(app)
    # call
    mock_get_user_role.return_value = None
    environ = {}
    start_response = Mock()
    # check
    response = middleware_instance(environ, start_response)
    mock_response.assert_called_once_with("User unauthorized", status=401)


@patch("src.middleware.get_user_role")
@patch("src.middleware.Request")
@patch("src.middleware.Response")
def test_middleware_class_call_user_authorized(mock_response, mock_request, mock_get_user_role):
    # create instance
    app = Mock()
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/user-auth"
    mock_request.return_value.headers = {"Authorization": "test"}
    middleware_instance = middleware.Middleware(app)
    # call
    mock_get_user_role.return_value = [
        [
            {
                "email": "test@example.com",
                "first_name": "John",
                "last_name": "Doe",
                "organization": "admin",
                "role": "admin",
                "super_user": "1",
            }
        ]
    ]
    mock_response_instance = mock_response.return_value
    mock_response_instance.path = "admin"

    environ = {}
    start_response = Mock()

    response = middleware_instance(environ, start_response)
    app.assert_called_once_with(environ, start_response)


@patch("src.middleware.Request")
@patch("src.middleware.Response")
@patch("src.middleware.KeycloakOpenID")
def test_middleware_class_call_exception(mock_keycloak, mock_response, mock_request):
    # create instance
    app = Mock()
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/user-auth"
    mock_request.return_value.headers = {"Authorization": "token"}

    # call
    mock_keycloak_instance = mock_keycloak.return_value
    mock_keycloak_instance.introspect.side_effect = Exception("test")

    resp = MagicMock()
    mock_response.return_value = resp
    mock_response.return_value.return_value = "test"

    environ = {}
    start_response = Mock()
    middleware_instance = middleware.Middleware(app)
    result = middleware_instance(environ, start_response)

    app.assert_not_called()
    mock_request.assert_called_once_with(environ)
    mock_response.assert_called_once_with("Authorization failed", status=401)
    expected_result = resp.return_value
    assert(result == expected_result)

@patch('src.middleware.get_user_role')
@patch('src.middleware.Request')
@patch('src.middleware.Response')
def test_middleware_class_call_contact_support(mock_response, mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "POST"
    mock_request.return_value.path = "/contact-support"

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