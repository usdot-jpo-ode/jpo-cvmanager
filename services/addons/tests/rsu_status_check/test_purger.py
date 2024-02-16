from mock import MagicMock, call, patch
from datetime import datetime, timedelta
from addons.images.rsu_status_check import purger
from freezegun import freeze_time


@freeze_time("2023-07-06")
@patch("addons.images.rsu_status_check.purger.pgquery.query_db")
def test_get_last_online_rsu_records(mock_query_db):
    # mock
    mock_query_db.return_value = [(1, 1, datetime.now())]

    # call
    result = purger.get_last_online_rsu_records()

    # check
    assert len(result) == 1
    assert result[0][0] == 1
    assert result[0][1] == 1
    assert result[0][2].strftime("%Y/%m/%d") == "2023/07/06"


@freeze_time("2023-07-06")
@patch("addons.images.rsu_status_check.purger.pgquery.write_db")
def test_purge_ping_data(mock_write_db):
    now_dt = datetime.now()
    purger.get_last_online_rsu_records = MagicMock(
        return_value=[
            [0, 0, now_dt - timedelta(hours=10)],
            [1, 1, now_dt - timedelta(days=3)],
        ]
    )
    purger.logging.info = MagicMock()
    purger.logging.debug = MagicMock()

    purger.purge_ping_data(24)

    purger.get_last_online_rsu_records.assert_called_once()
    mock_write_db.assert_has_calls(
        [
            call(
                "DELETE FROM public.ping WHERE rsu_id = 0 AND timestamp < '2023/07/05T00:00:00'::timestamp"
            ),
            call("DELETE FROM public.ping WHERE rsu_id = 1 AND ping_id != 1"),
        ]
    )
    purger.logging.info.assert_called_once()


@freeze_time("2023-07-06")
@patch("addons.images.rsu_status_check.purger.pgquery.write_db")
def test_purge_ping_data_none(mock_write_db):
    now_dt = datetime.now()
    purger.get_last_online_rsu_records = MagicMock(return_value=[])
    purger.logging.info = MagicMock()
    purger.logging.debug = MagicMock()

    purger.purge_ping_data(24)

    purger.get_last_online_rsu_records.assert_called_once()
    mock_write_db.assert_not_called()
    purger.logging.info.assert_called_once()
