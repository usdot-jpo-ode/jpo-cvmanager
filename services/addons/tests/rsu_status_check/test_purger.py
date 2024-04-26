from mock import MagicMock, call, patch
from datetime import datetime, timedelta
from addons.images.rsu_status_check import purger
from freezegun import freeze_time


@patch("addons.images.rsu_status_check.purger.pgquery.query_db")
def test_get_all_rsus(mock_query_db):
    # mock
    mock_query_db.return_value = [
        ({"rsu_id": 1},),
        ({"rsu_id": 2},),
        ({"rsu_id": 3},),
        ({"rsu_id": 4},),
    ]

    # call
    result = purger.get_all_rsus()

    # check
    assert 1 in result and result[1] is None
    assert 2 in result and result[2] is None
    assert 3 in result and result[3] is None
    assert 4 in result and result[4] is None
    assert 5 not in result


@patch("addons.images.rsu_status_check.purger.pgquery.query_db")
def test_get_last_online_rsu_records(mock_query_db):
    # mock
    mock_query_db.return_value = [
        ({"ping_id": 1, "rsu_id": 1, "timestamp": "2023-07-06T00:00:00"},),
    ]
    rsu_dict = {1: None}

    # call
    result = purger.get_last_online_rsu_records(rsu_dict)

    # check
    assert len(result) == 1
    assert result[1]["ping_id"] == 1
    assert result[1]["timestamp"].strftime("%Y/%m/%d") == "2023/07/06"


@freeze_time("2023-07-06")
@patch("addons.images.rsu_status_check.purger.get_all_rsus", MagicMock())
@patch("addons.images.rsu_status_check.purger.get_last_online_rsu_records")
@patch("addons.images.rsu_status_check.purger.pgquery.write_db")
def test_purge_ping_data(mock_write_db, mock_glorr):
    now_dt = datetime.now()
    mock_glorr.return_value = {
        1: {"ping_id": 1, "timestamp": now_dt - timedelta(hours=10)},
        2: {"ping_id": 2, "timestamp": now_dt - timedelta(days=3)},
    }

    purger.purge_ping_data(24)

    purger.get_last_online_rsu_records.assert_called_once()
    mock_write_db.assert_has_calls(
        [
            call(
                "DELETE FROM public.ping WHERE rsu_id = 1 AND timestamp < '2023-07-05T00:00:00'::timestamp"
            ),
            call("DELETE FROM public.ping WHERE rsu_id = 2 AND ping_id != 2"),
        ]
    )


@freeze_time("2023-07-06")
@patch("addons.images.rsu_status_check.purger.get_all_rsus", MagicMock())
@patch("addons.images.rsu_status_check.purger.get_last_online_rsu_records")
@patch("addons.images.rsu_status_check.purger.pgquery.write_db")
def test_purge_ping_data_none(mock_write_db, mock_glorr):
    mock_glorr.return_value = {}

    purger.purge_ping_data(24)

    purger.get_last_online_rsu_records.assert_called_once()
    mock_write_db.assert_not_called()
