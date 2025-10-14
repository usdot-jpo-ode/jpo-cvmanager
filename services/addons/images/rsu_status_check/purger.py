from datetime import datetime, timedelta
import logging
import common.pgquery as pgquery
import rsu_status_check_environment
from common import common_environment

def get_all_rsus():
    query = "SELECT to_jsonb(row) FROM (SELECT rsu_id FROM public.rsus) AS row ORDER BY rsu_id"
    data = pgquery.query_db(query)

    rsu_obj = {}
    for row in data:
        row = dict(row[0])
        rsu_obj[row["rsu_id"]] = None

    return rsu_obj


def get_last_online_rsu_records(rsu_dict):
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT a.ping_id, a.rsu_id, a.timestamp "
        "FROM ("
        "SELECT pd.ping_id, pd.rsu_id, pd.timestamp, ROW_NUMBER() OVER (PARTITION BY pd.rsu_id order by pd.timestamp DESC) AS row_id "
        "FROM public.ping AS pd "
        "WHERE pd.result = '1'"
        ") AS a "
        "WHERE a.row_id <= 1 ORDER BY rsu_id"
        ") as row"
    )
    data = pgquery.query_db(query)

    # Create list of RSU last online ping records
    # Tuple in the format of (ping_id, rsu_id, timestamp (UTC))
    for row in data:
        row = dict(row[0])

        if row["rsu_id"] not in rsu_dict:
            # If there is ping data for a RSU not in the PostgreSQL 'rsus' table, it is most likely old and no longer tracked
            # This will allow for all of the stale ping data to be removed
            rsu_dict[row["rsu_id"]] = None
        else:
            rsu_dict[row["rsu_id"]] = {
                "ping_id": row["ping_id"],
                "timestamp": datetime.strptime(row["timestamp"], "%Y-%m-%dT%H:%M:%S"),
            }
    return rsu_dict


def purge_ping_data(stale_period):
    rsu_dict = get_all_rsus()
    online_rsu_dict = get_last_online_rsu_records(rsu_dict)

    stale_point = datetime.now() - timedelta(hours=stale_period)
    stale_point_str = stale_point.strftime("%Y-%m-%dT%H:%M:%S")

    logging.info(f"Purging all ping data before {stale_point_str}")

    for key, value in online_rsu_dict.items():
        logging.debug(f"Cleaning up rsu_id: {str(key)}")

        purge_query = ""
        if value is not None:
            if value["timestamp"] < stale_point:
                # Create query to delete all records of the stale ping data besides the latest record
                purge_query = (
                    "DELETE FROM public.ping "
                    f"WHERE rsu_id = {str(key)} AND ping_id != {str(value["ping_id"])}"
                )

        # If the RSU is no longer tracked or the last ping was within the last 24 hours, purge all data beyond the stale point
        if purge_query == "":
            # Create query to delete all records before the stale_point
            purge_query = (
                "DELETE FROM public.ping "
                f"WHERE rsu_id = {str(key)} AND timestamp < '{stale_point_str}'::timestamp"
            )

        pgquery.write_db(purge_query)

    logging.info("Ping data purging successfully completed")


if __name__ == "__main__":
    common_environment.configure_logging()

    run_service = (
        rsu_status_check_environment.RSU_PING or rsu_status_check_environment.ZABBIX
    )
    if not run_service:
        logging.info("The purger service is disabled and will not run")
        exit()

    stale_period = rsu_status_check_environment.STALE_PERIOD_HOURS
    purge_ping_data(stale_period)
