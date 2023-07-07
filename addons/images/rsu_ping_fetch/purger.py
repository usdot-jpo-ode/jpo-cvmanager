from datetime import datetime, timedelta
import os
import logging
import pgquery_rsu

def purge_ping_data(stale_period):
  last_online_list = pgquery_rsu.get_last_online_rsu_records()

  stale_point = datetime.utcnow() - timedelta(hours=stale_period)
  stale_point_str = stale_point.strftime("%Y/%m/%dT%H:%M:%S")

  for record in last_online_list:
    logging.debug(f"Cleaning up rsu_id: {str(record[1])}")
    # Check if the RSU has been offline longer than the stale period
    if record[2] < stale_point:
      logging.debug(f"Latest record of rsu_id {str(record[1])} is a stale RSU ping record (ping_id: {str(record[0])})")
      # Create query to delete all records of the stale ping data besides the latest record
      purge_query = "DELETE FROM public.ping " \
                  f"WHERE rsu_id = {str(record[1])} AND ping_id != {str(record[0])}"
    else:
      # Create query to delete all records before the stale_point
      purge_query = "DELETE FROM public.ping " \
                  f"WHERE rsu_id = {str(record[1])} AND timestamp < '{stale_point_str}'::timestamp"

    pgquery_rsu.run_query(purge_query)

  logging.info("Ping data purging successfully completed")

if __name__ == "__main__":
  # Configure logging based on ENV var or use default if not set
  log_level = 'INFO' if "LOGGING_LEVEL" not in os.environ else os.environ['LOGGING_LEVEL']
  logging.basicConfig(format='%(levelname)s:%(message)s', level=log_level)

  stale_period = int(os.environ["STALE_PERIOD"])
  purge_ping_data(stale_period)