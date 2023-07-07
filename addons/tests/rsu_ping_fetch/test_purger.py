from mock import MagicMock, call
from datetime import datetime, timedelta
from images.rsu_ping_fetch import purger
from freezegun import freeze_time

@freeze_time("2023-07-06")
def test_purge_ping_data():
  now_dt = datetime.now()
  purger.pgquery_rsu.get_last_online_rsu_records = MagicMock(return_value= [
    [0, 0, now_dt - timedelta(hours = 10)],
    [1, 1, now_dt - timedelta(days = 3)]
  ])
  purger.pgquery_rsu.run_query = MagicMock()
  purger.logging.info = MagicMock()
  purger.logging.debug = MagicMock()

  purger.purge_ping_data(24)

  purger.pgquery_rsu.get_last_online_rsu_records.assert_called_once()
  purger.pgquery_rsu.run_query.assert_has_calls(
    [
      call('DELETE FROM public.ping WHERE rsu_id = 0 AND timestamp < \'2023/07/05T00:00:00\'::timestamp'), 
      call('DELETE FROM public.ping WHERE rsu_id = 1 AND ping_id != 1')
    ]
  )
  purger.logging.info.assert_called_once()

@freeze_time("2023-07-06")
def test_purge_ping_data_none():
  now_dt = datetime.now()
  purger.pgquery_rsu.get_last_online_rsu_records = MagicMock(return_value= [])
  purger.pgquery_rsu.run_query = MagicMock()
  purger.logging.info = MagicMock()
  purger.logging.debug = MagicMock()

  purger.purge_ping_data(24)

  purger.pgquery_rsu.get_last_online_rsu_records.assert_called_once()
  purger.pgquery_rsu.run_query.assert_not_called()
  purger.logging.info.assert_called_once()