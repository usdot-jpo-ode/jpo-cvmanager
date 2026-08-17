import subprocess
from mock import MagicMock, patch
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


def test_calculate_max_workers():
    assert rsu_pinger.calculate_max_workers(0) == 1
    assert rsu_pinger.calculate_max_workers(1) == 20
    assert rsu_pinger.calculate_max_workers(160) == 20
    assert rsu_pinger.calculate_max_workers(600) == 75
    assert rsu_pinger.calculate_max_workers(10000) == 120


@patch("addons.images.rsu_status_check.rsu_pinger.subprocess.run")
def test_ping_single_rsu_online(mock_run):
    mock_run.return_value = MagicMock(returncode=0)

    result = rsu_pinger.ping_single_rsu((1, "1.1.1.1"), retries=2)

    assert result == (1, "1")
    assert mock_run.call_count == 1
    mock_run.assert_called_with(
        ["ping", "-n", "-c", "1", "-W", "2", "1.1.1.1"],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        timeout=3,
    )


@patch("addons.images.rsu_status_check.rsu_pinger.subprocess.run")
def test_ping_single_rsu_offline_after_retries(mock_run):
    mock_run.return_value = MagicMock(returncode=1)

    result = rsu_pinger.ping_single_rsu((2, "2.2.2.2"), retries=2)

    assert result == (2, "0")
    assert mock_run.call_count == 2


@patch("addons.images.rsu_status_check.rsu_pinger.subprocess.run")
def test_ping_single_rsu_timeout_then_success(mock_run):
    timeout_error = subprocess.TimeoutExpired(cmd="ping", timeout=3)
    mock_run.side_effect = [timeout_error, MagicMock(returncode=0)]

    result = rsu_pinger.ping_single_rsu((3, "3.3.3.3"), retries=2)

    assert result == (3, "1")
    assert mock_run.call_count == 2


@patch("addons.images.rsu_status_check.rsu_pinger.subprocess.run")
def test_ping_single_rsu_generic_exception_returns_immediately(mock_run):
    mock_run.side_effect = RuntimeError("unexpected failure")

    result = rsu_pinger.ping_single_rsu((4, "4.4.4.4"), retries=2)

    assert result == (4, "0")
    assert mock_run.call_count == 1


@patch("addons.images.rsu_status_check.rsu_pinger.calculate_max_workers")
@patch("addons.images.rsu_status_check.rsu_pinger.ping_single_rsu")
def test_ping_rsu_ips_uses_calculated_workers_and_collects_results(
    mock_ping_single_rsu, mock_calculate_max_workers
):
    rsu_list = [(1, "1.1.1.1"), (2, "2.2.2.2")]
    mock_calculate_max_workers.return_value = 33
    mock_ping_single_rsu.side_effect = [(1, "1"), (2, "0")]

    result = rsu_pinger.ping_rsu_ips(rsu_list)

    assert result == {1: "1", 2: "0"}
    mock_calculate_max_workers.assert_called_once_with(len(rsu_list))


@patch("addons.images.rsu_status_check.rsu_pinger.ping_single_rsu")
def test_ping_rsu_ips_worker_exception_only_affects_failed_rsu(mock_ping_single_rsu):
    def _side_effect(rsu):
        if rsu[0] == 2:
            raise ValueError("worker failure")
        return rsu[0], "1"

    rsu_list = [(1, "1.1.1.1"), (2, "2.2.2.2"), (3, "3.3.3.3")]
    mock_ping_single_rsu.side_effect = _side_effect

    result = rsu_pinger.ping_rsu_ips(rsu_list)

    assert result == {1: "1", 2: "0", 3: "1"}


@patch("addons.images.rsu_status_check.rsu_pinger.ping_single_rsu")
def test_ping_rsu_ips_none_result_marks_rsu_offline(mock_ping_single_rsu):
    rsu_list = [(1, "1.1.1.1"), (2, "2.2.2.2")]
    mock_ping_single_rsu.side_effect = [(1, "1"), None]

    result = rsu_pinger.ping_rsu_ips(rsu_list)

    assert result == {1: "1", 2: "0"}


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
