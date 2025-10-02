from unittest.mock import MagicMock, Mock

from mock import patch
from api.src import wzdx_feed
import os


@patch("api.src.environment.WZDX_ENDPOINT", "myendpoint")
@patch("api.src.environment.WZDX_API_KEY", "myapikey")
# test that get_wzdx_data is calling json.loads with expected arguments
def test_get_wzdx_data():
    # mock return values for function dependencies
    wzdx_feed.json.loads = MagicMock(return_value="myvalue")

    wzdx_feed.requests.get = MagicMock(
        return_value=Mock(content=MagicMock(decode=MagicMock(return_value="mycontent")))
    )

    # call function
    result = wzdx_feed.get_wzdx_data()

    # check return value
    expectedResult = "myvalue"
    assert result == expectedResult

    # check that json.loads was called with expected arguments
    expectedContent = "mycontent"
    wzdx_feed.requests.get.return_value.content.decode.return_value = expectedContent


# TODO: add more tests here
