import os
import copy
import threading
import logging
import pgquery

from kafka_counter import KafkaMessageCounter

# Set based on project and subscription, set these outside of the script if deployed

thread_pool = []
rsu_location_dict = {}
rsu_count_dict = {}

# Query for RSU data from CV Manager PostgreSQL database
def get_rsu_data():
    result = []

    # Execute the query and fetch all results
    query = "SELECT ipv4_address, primary_route FROM public.rsus ORDER BY ipv4_address"
    data = pgquery.query_db(query)

    logging.debug("Parsing results...")
    for point in data:
        result.append({"ipAddress": str(point[0]), "primaryRoute": str(point[1])})

    return result

# Create template dictionaries for RSU roads and counts using HTTP JSON data
def populateRsuDict(rsu_data):
    for rsu in rsu_data:
        rsuip = rsu["ipAddress"]
        proute = rsu["primaryRoute"]

        rsu_location_dict[rsuip] = proute
        # Add IP to dict if the road exists in the dict already
        if proute in rsu_count_dict:
            rsu_count_dict[proute][rsuip] = 0
        else:
            rsu_count_dict[proute] = {rsuip: 0}

    rsu_count_dict["Unknown"] = {}


def run():
    # Pull list of message types to run counts for from environment variable
    messageTypesString = os.getenv("MESSAGE_TYPES", "")
    if messageTypesString == "":
        logging.error("MESSAGE_TYPES environment variable not set! Exiting.")
        exit("MESSAGE_TYPES environment variable not set! Exiting.")
    message_types = [
        msgtype.strip().lower() for msgtype in messageTypesString.split(",")
    ]

    # Configure logging based on ENV var or use default if not set
    log_level = (
        "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]
    )
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    rsu_data = get_rsu_data()

    logging.debug(f"RSU_Data received: {rsu_data}")
    logging.debug("Creating RSU and count dictionaries...")
    populateRsuDict(rsu_data)

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


if __name__ == "__main__":
    run()
