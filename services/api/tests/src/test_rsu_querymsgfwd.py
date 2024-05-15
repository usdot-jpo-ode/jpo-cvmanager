from unittest.mock import patch, MagicMock
import pytest
import os
import api.src.rsu_querymsgfwd as rsu_querymsgfwd
import api.tests.data.rsu_querymsgfwd_data as rsu_querymsgfwd_data

##################################### Testing Requests ###########################################


def test_options_request():
    query_msgfwd = rsu_querymsgfwd.RsuQueryMsgFwd()
    (body, code, headers) = query_msgfwd.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"


@patch("api.src.rsu_querymsgfwd.query_snmp_msgfwd")
def test_get_request(mock_query):
    req = MagicMock()
    req.environ = rsu_querymsgfwd_data.request_environ
    req.args = rsu_querymsgfwd_data.request_args_good
    query_msgfwd = rsu_querymsgfwd.RsuQueryMsgFwd()
    mock_query.return_value = {"Some Data"}, 200
    with patch("api.src.rsu_querymsgfwd.request", req):
        (data, code, headers) = query_msgfwd.get()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert headers["Content-Type"] == "application/json"
        assert data == {"Some Data"}


################################### Testing Data Validation #########################################


def test_schema_validate_bad_data():
    req = MagicMock()
    req.environ = rsu_querymsgfwd_data.request_environ
    req.args = rsu_querymsgfwd_data.request_args_bad_message
    query_msgfwd = rsu_querymsgfwd.RsuQueryMsgFwd()
    with patch("api.src.rsu_querymsgfwd.request", req):
        with pytest.raises(Exception):
            assert query_msgfwd.get()


###################################### Testing Functions ##########################################


@patch("api.src.rsu_querymsgfwd.pgquery")
def test_query_snmp_msgfwd_rsudsrcfwd(mock_pgquery):
    mock_pgquery.query_db.return_value = rsu_querymsgfwd_data.return_value_rsuDsrcFwd
    result, code = rsu_querymsgfwd.query_snmp_msgfwd("10.0.0.80", "Test")

    assert code == 200
    assert result == rsu_querymsgfwd_data.result_rsuDsrcFwd


@patch("api.src.rsu_querymsgfwd.pgquery")
def test_query_snmp_msgfwd_rxtxfwd(mock_pgquery):
    mock_pgquery.query_db.return_value = rsu_querymsgfwd_data.return_value_rxtxfwd
    result, code = rsu_querymsgfwd.query_snmp_msgfwd("10.0.0.80", "Test")

    assert code == 200
    assert result == rsu_querymsgfwd_data.result_rxtxfwd
