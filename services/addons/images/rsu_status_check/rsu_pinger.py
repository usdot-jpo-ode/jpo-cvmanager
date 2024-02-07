import os
import logging
import time
import common.pgquery as pgquery
from datetime import datetime
from subprocess import Popen, DEVNULL


def insert_ping_data(ping_data, ping_time):
    # Build the insert query with the RSU ping data
    query = "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES"
    for rsu_id, online_status in ping_data.items():
        query += f" (TO_TIMESTAMP('{ping_time}', 'YYYY-MM-DD HH24:MI:SS'), B'{online_status}', {rsu_id}),"
    query = query[:-1]

    # Run query
    pgquery.write_db(query)


def ping_rsu_ips(rsu_list):
    p = {}
    # Start ping processes
    for rsu in rsu_list:
        # id: rsu_id
        # key: process pinging the RSU's ipv4_address
        p[rsu[0]] = Popen(["ping", "-n", "-w5", "-c3", rsu[1]], stdout=DEVNULL)

    ping_data = {}
    while p:
        for rsu_id, proc in p.items():
            # Check if process has ended
            if proc.poll() is not None:
                del p[rsu_id]

                if proc.returncode == 0:
                    # Active
                    logging.debug("%s active" % rsu_id)
                    ping_data[rsu_id] = "1"
                else:
                    # Offline/Unresponsive
                    logging.debug("%s no response" % rsu_id)
                    ping_data[rsu_id] = "0"
                break

    return ping_data


def get_rsu_ips():
    rsu_list = []
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rsu_id, ipv4_address FROM public.rsus"
        ") as row"
    )

    # Query PostgreSQL for the list of RSU IPs
    data = pgquery.query_db(query)

    for row in data:
        row = dict(row[0])
        rsu_list.append((row["rsu_id"], row["ipv4_address"]))

    return rsu_list


def run_rsu_pinger():
    rsu_list = get_rsu_ips()

    # Ping RSU IPs and collect start/end times
    dt_string = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    st = time.time()
    ping_data = ping_rsu_ips(rsu_list)
    et = time.time()

    elapsed_time = et - st
    logging.info(f"Ping execution time: {elapsed_time} seconds")

    if len(ping_data) > 0:
        insert_ping_data(ping_data, dt_string)
    else:
        logging.error("Ping results are empty, something went wrong during RSU pings")


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    log_level = "INFO" if log_level == "" else log_level
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = (
        os.environ.get("RSU_PING", "False").lower() == "true"
        and os.environ.get("ZABBIX", "False").lower() == "false"
    )
    if not run_service:
        logging.info("The rsu-pinger service is disabled and will not run")
        exit()

    run_rsu_pinger()
