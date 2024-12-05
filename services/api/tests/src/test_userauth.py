from unittest.mock import MagicMock, patch

import pytest
from api.src import userauth
from common.auth_tools import ENVIRON_USER_KEY, EnvironNoAuth
from common.errors import UnauthorizedException
from api.tests.data import auth_data


user_valid = auth_data.get_request_environ()


@patch("api.src.userauth.Resource", new=MagicMock())
def test_rga_options():
    expected_options = {
        "Access-Control-Allow-Origin": "test.com",
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


@patch("api.src.userauth.Resource", new=MagicMock())
def test_rga_get():
    expected_headers = {
        "Access-Control-Allow-Origin": "test.com",
        "Content-Type": "application/json",
    }
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}
    with patch("api.src.userauth.request", req):
        result = userauth.UserAuth().get()

    # check result
    assert result == (
        '{"email": "test@gmail.com", "organizations": [{"org": "Test Org", "role": "admin"}, {"org": "Test Org 2", "role": "operator"}, {"org": "Test Org 3", "role": "user"}], "super_user": true, "first_name": "Test", "last_name": "User", "name": "Test User"}',
        200,
        expected_headers,
    )


def test_rga_get_unauthorized_user():
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: EnvironNoAuth()}
    with patch("api.src.userauth.request", req):
        with pytest.raises(UnauthorizedException) as exc_info:
            userauth.UserAuth().get()

    assert str(exc_info.value) == "Unauthorized user"
