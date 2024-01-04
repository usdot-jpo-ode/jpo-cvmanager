from unittest.mock import MagicMock, patch, Mock
from api.src import userauth

@patch('api.src.userauth.Resource', new=MagicMock())
def test_rga_options():
    expected_options = {
        'Access-Control-Allow-Origin': 'test.com',
        'Access-Control-Allow-Headers': 'Content-Type,Authorization',
        'Access-Control-Allow-Methods': 'GET',
        'Access-Control-Max-Age': '3600'
    }
    # instantiate UserAuth
    rga = userauth.UserAuth()

    # call options()
    result = rga.options()

    # check result
    assert result == ("", 204, expected_options)

@patch('api.src.userauth.Resource', new=MagicMock())
def test_rga_get():
    expected_headers = {
        'Access-Control-Allow-Origin': 'test.com',
        'Content-Type': 'application/json'
    }

    # instantiate UserAuth
    rga = userauth.UserAuth()

    # mock request.environ
    request = Mock()
    request.environ = {"user_info": "test"}
    userauth.request = request

    # call get()
    result = rga.get()

    # check result
    assert result == ('"test"', 200, expected_headers)

@patch('api.src.userauth.Resource', new=MagicMock())
@patch('api.src.userauth.request', new=MagicMock())
def test_rga_get_unauthorized_user():
    # instantiate UserAuth
    rga = userauth.UserAuth()

    # mock request.environ
    request = Mock()
    request.environ = {"user_info": None}
    userauth.request = request

    # call get()
    result = rga.get()

    # check result
    assert result == ("Unauthorized user", 401)
