import os
import copy
import threading
import logging
import daily_emailer
import common.pgquery as pgquery

from apscheduler.schedulers.background import BackgroundScheduler
from kafka_counter import KafkaMessageCounter

# Set based on project and subscription, set these outside of the script if deployed

thread_pool = []
rsu_location_dict = {}
rsu_count_dict = {}


# Query for RSU data from CV Manager PostgreSQL database
def get_rsu_list():
    result = []

    # Execute the query and fetch all results
    query = "SELECT to_jsonb(row) FROM (SELECT ipv4_address, primary_route FROM public.rsus ORDER BY ipv4_address) as row"
    data = pgquery.query_db(query)

    logging.debug("Parsing results...")
    for row in data:
        row = dict(row[0])
        result.append(row)

    return result


# Create template dictionaries for RSU roads and counts using HTTP JSON data
def populateRsuDict():
    rsu_list = get_rsu_list()
    for rsu in rsu_list:
        rsu_ip = rsu["ipv4_address"]
        p_route = rsu["primary_route"]

        rsu_location_dict[rsu_ip] = p_route
        # Add IP to dict if the road exists in the dict already
        if p_route in rsu_count_dict:
            rsu_count_dict[p_route][rsu_ip] = 0
        else:
            rsu_count_dict[p_route] = {rsu_ip: 0}

    rsu_count_dict["Unknown"] = {}


def run_counter():
    # Pull list of message types to run counts for from environment variable
    messageTypesString = os.getenv("MESSAGE_TYPES", "")
    if messageTypesString == "":
        logging.error("MESSAGE_TYPES environment variable not set! Exiting.")
        exit("MESSAGE_TYPES environment variable not set! Exiting.")
    message_types = [
        msgtype.strip().lower() for msgtype in messageTypesString.split(",")
    ]

    # Configure logging based on ENV var or use default if not set
    log_level = os.getenv("LOGGING_LEVEL", "INFO")
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    logging.debug("Creating RSU and count dictionaries...")
    populateRsuDict()

    logging.info("Creating Data-In Kafka count threads...")
    # Start the Kafka counters on their own threads
    for message_type in message_types:
        counter = KafkaMessageCounter(
            f"KAFKA_IN_{message_type.upper()}",
            message_type,
            copy.deepcopy(rsu_location_dict),
            copy.deepcopy(rsu_count_dict),
            copy.deepcopy(rsu_count_dict),
            0,
        )
        new_thread = threading.Thread(target=counter.start_counter)
        new_thread.start()
        thread_pool.append(new_thread)

    logging.info("Creating Data-Out Kafka count threads...")
    # Start the Kafka counters on their own threads
    for message_type in message_types:
        counter = KafkaMessageCounter(
            f"KAFKA_OUT_{message_type.upper()}",
            message_type,
            copy.deepcopy(rsu_location_dict),
            copy.deepcopy(rsu_count_dict),
            copy.deepcopy(rsu_count_dict),
            1,
        )
        new_thread = threading.Thread(target=counter.start_counter)
        new_thread.start()
        thread_pool.append(new_thread)

    for thread in thread_pool:
        thread.join()
        logging.debug("Closed thread")


def init_background_daily_emailer_task():
    logging.info("Initiating daily counts emailer background task scheduler...")
    # Run scheduler for async daily counts emails
    scheduler = BackgroundScheduler({"apscheduler.timezone": "UTC"})
    scheduler.add_job(daily_emailer.run_daily_emailer, "cron", minute="45")
    scheduler.start()


if __name__ == "__main__":
    log_level = (
        "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]
    )
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    logging.info("driver started")
    # Emailer does not work with counts generated from this service, only with the ODE counts in mongoDB
    if os.environ["ENABLE_EMAILER"].lower() == "true":
        init_background_daily_emailer_task()
        # Keeps the script going so it can run as a Dockerized container.
        # Yes, this sucks. We will switch this all to cron once we can remove the deprecated counter
        while True:
            continue
    # This counter is deprecated and will be losing support in the next release. Use the ODE with mongoDB
    else:
        run_counter()
