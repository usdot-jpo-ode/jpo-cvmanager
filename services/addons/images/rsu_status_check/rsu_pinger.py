import logging
import time
import common.pgquery as pgquery
from datetime import datetime
import subprocess
from concurrent.futures import ThreadPoolExecutor, as_completed
import rsu_status_check_environment


def calculate_max_workers(rsu_count):
    """
    Scale workers with list size, using a minimum of 20 workers (except when rsu_count <= 0) and a maximum of 120.
    """
    if rsu_count <= 0:
        return 1
    return min(120, max(20, rsu_count // 8))


def insert_ping_data(ping_data, ping_time):
    # Build the insert query with the RSU ping data
    query = "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES"
    for rsu_id, online_status in ping_data.items():
        query += f" (TO_TIMESTAMP('{ping_time}', 'YYYY-MM-DD HH24:MI:SS'), B'{online_status}', {rsu_id}),"
    query = query[:-1]

    # Run query
    pgquery.write_db(query)


def ping_single_rsu(rsu, retries=2):
    """
    Ping a single RSU, retrying up to `retries` total attempts.
    Returns tuple: (rsu_id, status)
    """

    rsu_id = rsu[0]
    ip_address = rsu[1]

    for attempt in range(retries):
        try:
            result = subprocess.run(
                ["ping", "-n", "-c", "1", "-W", "2", ip_address],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                timeout=3,
            )

            if result.returncode == 0:
                logging.debug("%s active", rsu_id)
                return rsu_id, "1"

        except subprocess.TimeoutExpired:
            logging.debug("%s ping timeout attempt %s", rsu_id, attempt + 1)
        except Exception as ex:
            logging.warning(
                "%s ping command failed on attempt %s: %s", rsu_id, attempt + 1, ex
            )
            return rsu_id, "0"

    logging.debug("%s no response", rsu_id)
    return rsu_id, "0"


def ping_rsu_ips(rsu_list):
    ping_data = {}
    future_to_rsu_id = {}
    max_workers = calculate_max_workers(len(rsu_list))

    # Limit concurrency
    with ThreadPoolExecutor(max_workers=max_workers) as executor:

        futures = [executor.submit(ping_single_rsu, rsu) for rsu in rsu_list]
        future_to_rsu_id = {future: rsu[0] for future, rsu in zip(futures, rsu_list)}

        for future in as_completed(futures):
            rsu_id = future_to_rsu_id.get(future)
            try:
                result = future.result()
                if result is None:
                    if rsu_id is not None:
                        logging.warning("%s returned no ping result", rsu_id)
                        ping_data[rsu_id] = "0"
                    continue
                rsu_id, status = result
            except Exception as ex:
                if rsu_id is not None:
                    logging.warning(
                        "%s failed with exception in worker: %s", rsu_id, ex
                    )
                    ping_data[rsu_id] = "0"
                continue
            ping_data[rsu_id] = status

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
    run_service = (
        rsu_status_check_environment.RSU_PING and not rsu_status_check_environment.ZABBIX
    )
    if not run_service:
        logging.info("The rsu-pinger service is disabled and will not run")
        exit()

    run_rsu_pinger()
