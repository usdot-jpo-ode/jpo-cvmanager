from mock import MagicMock, call, patch
from subprocess import DEVNULL
from addons.images.rsu_status_check import rsu_pinger


@patch("addons.images.rsu_status_check.rsu_pinger.pgquery.write_db")
def test_insert_ping_data(mock_write_db):
    ping_data = {1: "0", 2: "1", 3: "1"}
    time_str = "2023-11-01 00:00:00"

    # call
    rsu_pinger.insert_ping_data(ping_data, time_str)

    # check
    expected_query = (
        "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES "
        "(TO_TIMESTAMP('2023-11-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), B'0', 1), "
        "(TO_TIMESTAMP('2023-11-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), B'1', 2), "
        "(TO_TIMESTAMP('2023-11-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), B'1', 3)"
    )
    mock_write_db.assert_called_with(expected_query)


@patch("addons.images.rsu_status_check.rsu_pinger.Popen")
def test_ping_rsu_ips_online(mock_Popen):
    mock_p = MagicMock()
    mock_p.poll.return_value = 1
    mock_p.returncode = 0
    mock_Popen.return_value = mock_p
    rsu_list = [(1, "1.1.1.1"), (2, "2.2.2.2")]

    # call
    result = rsu_pinger.ping_rsu_ips(rsu_list)

    # check
    expected_result = {1: "1", 2: "1"}
    mock_Popen.assert_has_calls(
        [
            call(["ping", "-n", "-w5", "-c3", "1.1.1.1"], stdout=DEVNULL),
            call(["ping", "-n", "-w5", "-c3", "2.2.2.2"], stdout=DEVNULL),
        ]
    )
    assert mock_p.poll.call_count == len(rsu_list)  # 2
    assert result == expected_result


@patch("addons.images.rsu_status_check.rsu_pinger.Popen")
def test_ping_rsu_ips_offline(mock_Popen):
    mock_p = MagicMock()
    mock_p.poll.return_value = 1
    mock_p.returncode = 1
    mock_Popen.return_value = mock_p
    rsu_list = [(1, "1.1.1.1"), (2, "2.2.2.2")]

    # call
    result = rsu_pinger.ping_rsu_ips(rsu_list)

    # check
    expected_result = {1: "0", 2: "0"}
    mock_Popen.assert_has_calls(
        [
            call(["ping", "-n", "-w5", "-c3", "1.1.1.1"], stdout=DEVNULL),
            call(["ping", "-n", "-w5", "-c3", "2.2.2.2"], stdout=DEVNULL),
        ]
    )
    assert mock_p.poll.call_count == len(rsu_list)  # 2
    assert result == expected_result


@patch("addons.images.rsu_status_check.rsu_pinger.pgquery.query_db")
def test_get_rsu_ips(mock_query_db):
    mock_query_db.return_value = [
        ({"rsu_id": 1, "ipv4_address": "1.1.1.1"},),
        ({"rsu_id": 2, "ipv4_address": "2.2.2.2"},),
    ]

    # call
    result = rsu_pinger.get_rsu_ips()

    # check
    expected_result = [(1, "1.1.1.1"), (2, "2.2.2.2")]
    assert result == expected_result


@patch("addons.images.rsu_status_check.rsu_pinger.get_rsu_ips")
@patch("addons.images.rsu_status_check.rsu_pinger.ping_rsu_ips")
@patch("addons.images.rsu_status_check.rsu_pinger.insert_ping_data")
def test_run_rsu_pinger(mock_insert_ping_data, mock_ping_rsu_ips, mock_get_rsu_ips):
    mock_ping_rsu_ips.return_value = {1: "1", 2: "0", 3: "1"}

    # call
    rsu_pinger.run_rsu_pinger()

    # check
    mock_get_rsu_ips.assert_called_once()
    mock_ping_rsu_ips.assert_called_once()
    mock_insert_ping_data.assert_called_once()


@patch("addons.images.rsu_status_check.rsu_pinger.get_rsu_ips")
@patch("addons.images.rsu_status_check.rsu_pinger.ping_rsu_ips")
@patch("addons.images.rsu_status_check.rsu_pinger.insert_ping_data")
def test_run_rsu_pinger_err(mock_insert_ping_data, mock_ping_rsu_ips, mock_get_rsu_ips):
    mock_ping_rsu_ips.return_value = {}

    # call
    rsu_pinger.run_rsu_pinger()

    # check
    mock_get_rsu_ips.assert_called_once()
    mock_ping_rsu_ips.assert_called_once()
    assert mock_insert_ping_data.call_count == 0
