from unittest.mock import patch, Mock

from api.src import middleware
from api.tests.data import auth_data
from werkzeug.exceptions import Unauthorized


@patch("api.src.middleware.jwt")
@patch("api.src.middleware.KeycloakOpenID")
def test_get_user_role(mock_keycloak, mock_jwt):
    mock_instance = mock_keycloak.return_value
    introspect = auth_data.jwt_token_data_good

    # Valid Token
    introspect["active"] = True
    mock_instance.introspect.return_value = auth_data.jwt_token_data_good
    mock_jwt.decode.return_value = auth_data.jwt_token_data_good

    result = middleware.get_user_role("dummy_token")
    assert result is not None
    assert result.email == "test@gmail.com"
    assert result.first_name == "Test"
    assert result.last_name == "User"
    assert result.organizations == {
        "Test Org": "admin",
        "Test Org 2": "operator",
        "Test Org 3": "user",
    }
    assert result.super_user

    # Invalid Token
    introspect["active"] = False
    result = middleware.get_user_role("dummy_token")
    assert result is None


@patch("api.src.middleware.get_user_role")
@patch("api.src.middleware.Request")
@patch("api.src.middleware.KeycloakOpenID")
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


@patch("api.src.middleware.get_user_role")
@patch("api.src.middleware.Request")
def test_middleware_class_call_user_unauthorized(mock_request, mock_get_user_role):
    # create instance
    app = Mock()
    middleware_instance = middleware.Middleware(app)
    # call
    mock_get_user_role.return_value = None
    environ = {"REQUEST_METHOD": "GET", "PATH_INFO": "/user-auth"}
    start_response = Mock()
    # check
    response = middleware_instance(environ, start_response)
    response_body = b"".join(response).decode("utf-8")
    assert (
        str(response_body)
        == '{"error": "Unauthorized", "message": "Failed to parse Authorization token"}'
    )


@patch("api.src.middleware.get_user_role")
@patch("api.src.middleware.Request")
def test_middleware_class_call_user_authorized(mock_request, mock_get_user_role):
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

    environ = {}
    start_response = Mock()

    middleware_instance(environ, start_response)
    app.assert_called_once_with(environ, start_response)


@patch("api.src.middleware.Request")
@patch("api.src.middleware.KeycloakOpenID")
def test_middleware_class_call_exception(mock_keycloak, mock_request):
    # create instance
    app = Mock()
    mock_request.return_value.method = "GET"
    mock_request.return_value.path = "/user-auth"
    mock_request.return_value.headers = {"Authorization": "token"}

    # call
    mock_keycloak_instance = mock_keycloak.return_value
    mock_keycloak_instance.introspect.side_effect = Unauthorized("test")

    environ = {"REQUEST_METHOD": "GET", "PATH_INFO": "/user-auth"}
    start_response = Mock()
    middleware_instance = middleware.Middleware(app)

    response = middleware_instance(environ, start_response)
    print(type(response))
    response_body = b"".join(response).decode("utf-8")
    print(response_body)
    assert str(response_body) == '{"error": "Unauthorized", "message": "test"}'
    app.assert_not_called()
    mock_request.assert_called_once_with(environ)


@patch("api.src.middleware.get_user_role")
@patch("api.src.middleware.Request")
def test_middleware_class_call_contact_support(mock_request, mock_get_user_role):
    # mock
    mock_request.return_value.method = "POST"
    mock_request.return_value.path = "/contact-support"

    # create instance
    app = Mock()
    middleware_instance = middleware.Middleware(app)

    # call
    environ = {"GOOGLE_CLIENT_ID": "test"}
    start_response = Mock()
    middleware_instance(environ, start_response)

    # check
    mock_get_user_role.assert_not_called()
    app.assert_called_once_with(environ, start_response)
    mock_request.assert_called_once_with(environ)


@patch("api.src.middleware.ENABLE_RSU_FEATURES", True)
@patch("api.src.middleware.ENABLE_INTERSECTION_FEATURES", True)
@patch("api.src.middleware.ENABLE_WZDX_FEATURES", True)
def test_evaluate_tag_all_enabled():
    assert not middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.RSU)
    assert not middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.INTERSECTION)
    assert not middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.WZDX)


@patch("api.src.middleware.ENABLE_RSU_FEATURES", False)
@patch("api.src.middleware.ENABLE_INTERSECTION_FEATURES", False)
@patch("api.src.middleware.ENABLE_WZDX_FEATURES", False)
def test_evaluate_tag_all_disabled():
    from api.src import middleware as middleware

    assert middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.RSU)
    assert middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.INTERSECTION)
    assert middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.WZDX)


@patch("api.src.middleware.ENABLE_RSU_FEATURES", False)
@patch("api.src.middleware.ENABLE_INTERSECTION_FEATURES", True)
@patch("api.src.middleware.ENABLE_WZDX_FEATURES", False)
def test_evaluate_tag_different():
    from api.src import middleware as middleware

    assert middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.RSU)
    assert not middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.INTERSECTION)
    assert middleware.is_tag_disabled(middleware.FEATURE_KEYS_LITERAL.WZDX)


@patch("api.src.middleware.ENABLE_RSU_FEATURES", False)
@patch("api.src.middleware.ENABLE_INTERSECTION_FEATURES", False)
@patch("api.src.middleware.ENABLE_WZDX_FEATURES", False)
def test_is_feature_disabled_disabled():
    from api.src import middleware as middleware

    feature_tags = {
        "/a": middleware.FEATURE_KEYS_LITERAL.RSU,
        "/b": middleware.FEATURE_KEYS_LITERAL.INTERSECTION,
        "/c": middleware.FEATURE_KEYS_LITERAL.WZDX,
        "/d": None,
    }

    assert middleware.is_endpoint_disabled(feature_tags, "/a")
    assert middleware.is_endpoint_disabled(feature_tags, "/b")
    assert middleware.is_endpoint_disabled(feature_tags, "/c")
    assert not middleware.is_endpoint_disabled(feature_tags, "/d")
    assert not middleware.is_endpoint_disabled(feature_tags, "/f")


@patch("api.src.middleware.ENABLE_RSU_FEATURES", True)
@patch("api.src.middleware.ENABLE_INTERSECTION_FEATURES", True)
@patch("api.src.middleware.ENABLE_WZDX_FEATURES", True)
def test_is_feature_disabled_enabled():
    from api.src import middleware as middleware

    feature_tags = {
        "/a": middleware.FEATURE_KEYS_LITERAL.RSU,
        "/b": middleware.FEATURE_KEYS_LITERAL.INTERSECTION,
        "/c": middleware.FEATURE_KEYS_LITERAL.WZDX,
        "/d": None,
    }

    assert not middleware.is_endpoint_disabled(feature_tags, "/a")
    assert not middleware.is_endpoint_disabled(feature_tags, "/b")
    assert not middleware.is_endpoint_disabled(feature_tags, "/c")
    assert not middleware.is_endpoint_disabled(feature_tags, "/d")
    assert not middleware.is_endpoint_disabled(feature_tags, "/f")


@patch("api.src.middleware.ENABLE_RSU_FEATURES", True)
@patch("api.src.middleware.ENABLE_INTERSECTION_FEATURES", False)
@patch("api.src.middleware.ENABLE_WZDX_FEATURES", False)
def test_is_feature_disabled_different():
    from api.src import middleware as middleware

    feature_tags = {
        "/a": middleware.FEATURE_KEYS_LITERAL.RSU,
        "/b": middleware.FEATURE_KEYS_LITERAL.INTERSECTION,
        "/c": middleware.FEATURE_KEYS_LITERAL.WZDX,
        "/d": None,
    }

    assert not middleware.is_endpoint_disabled(feature_tags, "/a")
    assert middleware.is_endpoint_disabled(feature_tags, "/b")
    assert middleware.is_endpoint_disabled(feature_tags, "/c")
    assert not middleware.is_endpoint_disabled(feature_tags, "/d")
    assert not middleware.is_endpoint_disabled(feature_tags, "/f")
