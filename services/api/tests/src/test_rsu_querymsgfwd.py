from unittest.mock import patch, MagicMock
import pytest
import os
import api.src.rsu_querymsgfwd as rsu_querymsgfwd
import api.tests.data.rsu_querymsgfwd_data as rsu_querymsgfwd_data
from api.tests.data import auth_data
from common.auth_tools import ENVIRON_USER_KEY

user_valid = auth_data.get_request_environ()


# #################################### Testing Requests ###########################################
def test_options_request():
    query_msgfwd = rsu_querymsgfwd.RsuQueryMsgFwd()
    (body, code, headers) = query_msgfwd.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"


@patch("api.src.rsu_querymsgfwd.query_snmp_msgfwd_authorized")
@patch(
    "api.src.rsu_querymsgfwd.request",
    MagicMock(
        args=rsu_querymsgfwd_data.request_args_good,
    ),
)
def test_get_request(mock_query):
    query_msgfwd = rsu_querymsgfwd.RsuQueryMsgFwd()
    mock_query.return_value = {"Some Data"}
    (data, code, headers) = query_msgfwd.get()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert headers["Content-Type"] == "application/json"
    assert data == {"Some Data"}


# ################################## Testing Data Validation #########################################
@patch(
    "api.src.rsu_querymsgfwd.request",
    MagicMock(
        args=rsu_querymsgfwd_data.request_args_bad_message,
    ),
)
def test_schema_validate_bad_data():
    query_msgfwd = rsu_querymsgfwd.RsuQueryMsgFwd()
    with pytest.raises(Exception):
        assert query_msgfwd.get()


# ##################################### Testing Functions ##########################################
@patch("api.src.rsu_querymsgfwd.pgquery")
def test_query_snmp_msgfwd_rsudsrcfwd(mock_pgquery):
    mock_pgquery.query_db.return_value = rsu_querymsgfwd_data.return_value_rsuDsrcFwd
    result = rsu_querymsgfwd.query_snmp_msgfwd_authorized("10.0.0.80")
    assert result == rsu_querymsgfwd_data.result_rsuDsrcFwd


@patch("api.src.rsu_querymsgfwd.pgquery")
def test_query_snmp_msgfwd_rxtxfwd(mock_pgquery):
    mock_pgquery.query_db.return_value = rsu_querymsgfwd_data.return_value_rxtxfwd
    result = rsu_querymsgfwd.query_snmp_msgfwd_authorized("10.0.0.80")
    assert result == rsu_querymsgfwd_data.result_rxtxfwd
