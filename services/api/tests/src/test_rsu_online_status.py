from datetime import datetime, timedelta
from unittest.mock import patch, MagicMock
from dateutil.parser import parse
from werkzeug.exceptions import HTTPException
import api.src.rsu_online_status as rsu_online_status
import common.util as util
import api.tests.data.rsu_online_status_data as data
import pytest


######################################## Test Request Handling ##################################


def test_request_options():
    info = rsu_online_status.RsuOnlineStatus()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"


@patch("api.src.rsu_online_status.get_rsu_online_statuses")
def test_request_get_rsu_online_statuses(mockData):
    req = MagicMock()
    req.environ = data.request_params_good
    req.args = {}
    mockData.return_value = {"some data"}
    with patch("api.src.rsu_online_status.request", req):
        info = rsu_online_status.RsuOnlineStatus()
        (body, code, headers) = info.get()
        mockData.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {"some data"}


@patch("api.src.rsu_online_status.get_last_online_data")
def test_request_get_last_online_data(mockData):
    req = MagicMock()
    req.environ = data.request_params_good
    req.args = {"rsu_ip": "10.0.0.1"}
    mockData.return_value = {"some data"}
    with patch("api.src.rsu_online_status.request", req):
        info = rsu_online_status.RsuOnlineStatus()
        (body, code, headers) = info.get()
        mockData.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {"some data"}


def test_request_get_last_online_data_schema():
    req = MagicMock()
    req.environ = data.request_params_good
    req.args = {"rsu_ip": "10.0.01"}
    with patch("api.src.rsu_online_status.request", req):
        info = rsu_online_status.RsuOnlineStatus()
        with pytest.raises(HTTPException):
            info.get()


####################################### Test Ping Data ##################################


@patch("api.src.rsu_online_status.pgquery")
def test_ping_data_query(mock_pgquery):
    t = datetime.utcnow() - timedelta(minutes=20)
    organization = "Test"
    expected_query = (
        "SELECT jsonb_build_object('id', rd.rsu_id, 'ip', rd.ipv4_address, 'datetime', ping_data.timestamp, 'online_status', ping_data.result) "
        "FROM public.rsus AS rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "JOIN ("
        "SELECT * FROM public.ping AS ping_data "
        f"WHERE ping_data.timestamp >= '{t.strftime('%Y/%m/%dT%H:%M:%S')}'::timestamp"
        ") AS ping_data ON rd.rsu_id = ping_data.rsu_id "
        f"WHERE ron_v.name = '{organization}' "
        "ORDER BY rd.rsu_id, ping_data.timestamp DESC"
    )

    rsu_online_status.get_ping_data(organization)
    mock_pgquery.query_db.assert_called_with(expected_query)


@patch("api.src.rsu_online_status.pgquery")
def test_ping_data_no_data(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    expected_rsu_data = {}
    actual_result = rsu_online_status.get_ping_data("Test")
    assert actual_result == expected_rsu_data


@patch("api.src.rsu_online_status.pgquery")
def test_ping_data_single_result(mock_pgquery):
    mock_pgquery.query_db.return_value = data.ping_return_single
    expected_rsu_data = data.ping_expected_single
    actual_result = rsu_online_status.get_ping_data("Test")
    assert actual_result == expected_rsu_data


@patch("api.src.rsu_online_status.pgquery")
def test_ping_data_multiple_result(mock_pgquery):
    mock_pgquery.query_db.return_value = data.ping_return_multiple
    expected_rsu_data = data.ping_expected_multiple
    actual_result = rsu_online_status.get_ping_data("Test")
    assert actual_result == expected_rsu_data


####################################### Test Last Online ##################################


@patch("api.src.rsu_online_status.pgquery")
def test_last_online_query(mock_pgquery):
    expected_query = data.last_online_query
    rsu_online_status.get_last_online_data("10.0.0.1", "Test")
    mock_pgquery.query_db.assert_called_with(expected_query)


@patch("api.src.rsu_online_status.pgquery")
def test_last_online_no_data(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    expected_rsu_data = data.last_online_no_data_expected
    actual_result = rsu_online_status.get_last_online_data("10.0.0.1", "Test")
    assert actual_result == expected_rsu_data


@patch("api.src.rsu_online_status.pgquery")
def test_last_online_single_result(mock_pgquery):
    mock_pgquery.query_db.return_value = data.last_online_query_return
    expected_rsu_data = data.last_online_data_expected
    actual_result = rsu_online_status.get_last_online_data("10.0.0.1", "Test")
    assert actual_result == expected_rsu_data


@patch("api.src.rsu_online_status.get_ping_data")
@patch("api.src.rsu_online_status.get_last_online_data")
def test_online_statuses_no_data(mock_last_online, mock_ping):
    mock_last_online.return_value = []
    mock_ping.return_value = {}
    expected_rsu_data = {}
    actual_result = rsu_online_status.get_rsu_online_statuses("Test")
    assert actual_result == expected_rsu_data


# Test to verify that the difference in times between the util.format_date_utc and
# util.format_date_denver is correct. The strftime call in format_date_denver strips
# the time zone offset from the time, so by verifying the difference between the two
# calls we can greenlight the slightly odd behavior in the two tests below.
# The information currently displays correctly in MST on the rsu-manager webpage.
def test_util_format_date_denver():
    utc_tmp = util.format_date_utc("2022-06-14T20:26:58")
    den_tmp = util.format_date_denver("2022-06-14T20:26:58")
    utc_dt = parse(utc_tmp)
    den_dt = parse(den_tmp)
    diff = utc_dt - den_dt
    assert (diff.total_seconds() / 3600) == 6


@patch("api.src.rsu_online_status.get_ping_data")
def test_online_statuses_single_result(mock_ping):
    mock_ping.return_value = data.mock_ping_return_single
    expected_rsu_data = data.online_status_expected_single
    actual_result = rsu_online_status.get_rsu_online_statuses("Test")
    assert actual_result == expected_rsu_data


@patch("api.src.rsu_online_status.get_ping_data")
def test_online_statuses_multiple_result(mock_ping):
    mock_ping.return_value = data.mock_ping_return_multiple
    expected_rsu_data = data.online_status_expected_multiple
    actual_result = rsu_online_status.get_rsu_online_statuses("Test")
    assert actual_result == expected_rsu_data
