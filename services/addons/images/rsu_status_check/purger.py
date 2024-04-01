from datetime import datetime, timedelta
import os
import logging
import common.pgquery as pgquery


def get_last_online_rsu_records():
    result = []

    query = (
        "SELECT a.ping_id, a.rsu_id, a.timestamp "
        "FROM ("
        "SELECT pd.ping_id, pd.rsu_id, pd.timestamp, ROW_NUMBER() OVER (PARTITION BY pd.rsu_id order by pd.timestamp DESC) AS row_id "
        "FROM public.ping AS pd "
        "WHERE pd.result = '1'"
        ") AS a "
        "WHERE a.row_id <= 1 ORDER BY rsu_id"
    )
    data = pgquery.query_db(query)

    # Create list of RSU last online ping records
    # Tuple in the format of (ping_id, rsu_id, timestamp (UTC))
    result = [value for value in data]

    return result


def purge_ping_data(stale_period):
    last_online_list = get_last_online_rsu_records()

    stale_point = datetime.utcnow() - timedelta(hours=stale_period)
    stale_point_str = stale_point.strftime("%Y/%m/%dT%H:%M:%S")

    for record in last_online_list:
        logging.debug(f"Cleaning up rsu_id: {str(record[1])}")
        # Check if the RSU has been offline longer than the stale period
        if record[2] < stale_point:
            logging.debug(
                f"Latest record of rsu_id {str(record[1])} is a stale RSU ping record (ping_id: {str(record[0])})"
            )
            # Create query to delete all records of the stale ping data besides the latest record
            purge_query = (
                "DELETE FROM public.ping "
                f"WHERE rsu_id = {str(record[1])} AND ping_id != {str(record[0])}"
            )
        else:
            # Create query to delete all records before the stale_point
            purge_query = (
                "DELETE FROM public.ping "
                f"WHERE rsu_id = {str(record[1])} AND timestamp < '{stale_point_str}'::timestamp"
            )

        pgquery.write_db(purge_query)

    logging.info("Ping data purging successfully completed")


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = (
        os.environ.get("RSU_PINGER", "False").lower() == "true"
        or os.environ.get("RSU_PING_FETCH", "False").lower() == "true"
    )
    if not run_service:
        logging.info("The purger service is disabled and will not run")
        exit()

    stale_period = int(os.environ["STALE_PERIOD"])
    purge_ping_data(stale_period)
