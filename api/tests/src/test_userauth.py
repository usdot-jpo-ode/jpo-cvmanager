from unittest.mock import MagicMock, patch, Mock
from src import userauth


@patch("src.userauth.Resource", new=MagicMock())
def test_rga_options():
    expected_options = {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }
    # instantiate UserAuth
    rga = userauth.UserAuth()

    # call options()
    result = rga.options()

    # check result
    assert result == ("", 204, expected_options)


@patch("src.userauth.Resource", new=MagicMock())
def test_rga_get():
    expected_headers = {"Access-Control-Allow-Origin": "*", "Content-Type": "application/json"}

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


@patch("src.userauth.Resource", new=MagicMock())
@patch("src.userauth.request", new=MagicMock())
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
