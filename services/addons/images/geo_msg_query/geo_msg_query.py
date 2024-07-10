import os
from concurrent.futures import ThreadPoolExecutor
import logging
from pymongo import MongoClient, DESCENDING, GEOSPHERE
from datetime import datetime
import traceback


def set_mongo_client(MONGO_DB_URI, MONGO_DB):
    client = MongoClient(MONGO_DB_URI)
    db = client[MONGO_DB]
    return db


def create_message(original_message, msg_type):
    try:
        latitude = None
        longitude = None
        if msg_type == "Bsm":
            longitude = original_message["payload"]["data"]["coreData"]["position"][
                "longitude"
            ]
            latitude = original_message["payload"]["data"]["coreData"]["position"][
                "latitude"
            ]
        elif msg_type == "Psm":
            longitude = original_message["payload"]["data"]["position"]["longitude"]
            latitude = original_message["payload"]["data"]["position"]["latitude"]
        if latitude and longitude:
            timestamp_str = original_message["metadata"]["odeReceivedAt"]
            # checking if the timestamp is using nanoseconds and then truncating
            # to milliseconds to avoid exceptions when creating the datetime object.
            if len(timestamp_str) > 26:
                timestamp_str = timestamp_str[:26] + "Z"
            new_message = {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        longitude,
                        latitude,
                    ],
                },
                "properties": {
                    "id": original_message["metadata"]["originIp"],
                    "timestamp": datetime.strptime(
                        timestamp_str,
                        "%Y-%m-%dT%H:%M:%S.%fZ",
                    ),
                    "msg_type": msg_type,
                },
            }
            return new_message
        else:
            logging.warn(
                f"create_message: Could not create a message for type: {msg_type}"
            )
            return None
    except Exception as e:
        logging.error(f"create_message: Exception occurred: {str(e)}")
        logging.error(traceback.format_exc())
        return None


def process_message(message, db, collection, msg_type):
    new_message = create_message(message, msg_type)
    if new_message:
        db[collection].insert_one(new_message)
    else:
        logging.error(
            f"process_message: Could not create a message from the input {msg_type} message: {message}"
        )


def set_indexes(db, collection, mongo_ttl):
    logging.info("Creating indexes for the output collection")
    output_collection_obj = db[collection]
    index_info = output_collection_obj.index_information()

    if "timestamp_geosphere_index" not in index_info:
        logging.info("Creating timestamp_geosphere_index")
        output_collection_obj.create_index(
            [
                ("properties.timestamp", DESCENDING),
                ("properties.msg_type", DESCENDING),
                ("geometry", GEOSPHERE),
            ],
            name="timestamp_geosphere_index",
        )
    else:
        logging.info("timestamp_geosphere_index already exists")
    if "ttl_index" not in index_info:
        logging.info("Creating ttl_index")
        output_collection_obj.create_index(
            [("properties.timestamp", DESCENDING)],
            name="ttl_index",
            expireAfterSeconds=int(mongo_ttl) * 24 * 60 * 60,
        )
    else:
        existing_ttl = index_info["ttl_index"]["expireAfterSeconds"]
        wanted_ttl = int(mongo_ttl) * 24 * 60 * 60
        if existing_ttl != wanted_ttl:
            logging.info("ttl_index exists but with different TTL value. Recreating...")
            output_collection_obj.drop_index("ttl_index")
            output_collection_obj.create_index(
                [("properties.timestamp", DESCENDING)],
                name="ttl_index",
                expireAfterSeconds=wanted_ttl,
            )
        else:
            logging.info("ttl_index already exists with the correct TTL value")


def watch_collection(db, input_collection, output_collection):
    try:
        msg_type = input_collection.replace("Ode", "").replace("Json", "")
        logging.info(f"Watching collection: {input_collection}")
        count = 0
        with db[input_collection].watch(full_document="updateLookup") as stream:
            for change in stream:
                if change.get("operationType") in ["insert"]:
                    count += 1
                    logging.debug(f"Change: {change}")
                    process_message(
                        change["fullDocument"], db, output_collection, msg_type
                    )
                    logging.debug(f"{msg_type} Count: {count}")
                else:
                    logging.debug(
                        f"Ignoring change with operationType: {change.get('operationType')}"
                    )
    except Exception as e:
        logging.error(
            f"An error occurred while watching collection: {input_collection}"
        )
        logging.error(str(e))
        logging.error(traceback.format_exc())


def run():
    MONGO_DB_URI = os.getenv("MONGO_DB_URI")
    MONGO_DB = os.getenv("MONGO_DB_NAME")
    MONGO_INPUT_COLLECTIONS = os.getenv("MONGO_INPUT_COLLECTIONS")
    MONGO_GEO_OUTPUT_COLLECTION = os.getenv("MONGO_GEO_OUTPUT_COLLECTION")
    MONGO_TTL = os.getenv("MONGO_TTL")  # in days

    if (
        MONGO_DB_URI is None
        or MONGO_INPUT_COLLECTIONS is None
        or MONGO_DB is None
        or MONGO_GEO_OUTPUT_COLLECTION is None
        or MONGO_TTL is None
    ):
        logging.error("Environment variables are not set! Exiting.")
        exit("Environment variables are not set! Exiting.")

    log_level = (
        "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]
    )
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    logging.debug("Starting the service with environment variables: ")
    logging.debug(f"MONGO_DB: {MONGO_DB}")
    logging.debug(f"MONGO_INPUT_COLLECTIONS: {MONGO_INPUT_COLLECTIONS}")
    logging.debug(f"MONGO_GEO_OUTPUT_COLLECTION: {MONGO_GEO_OUTPUT_COLLECTION}")
    logging.debug(f"MONGO_TTL: {MONGO_TTL}")

    db = set_mongo_client(MONGO_DB_URI, MONGO_DB)
    set_indexes(db, MONGO_GEO_OUTPUT_COLLECTION, MONGO_TTL)
    input_collections = MONGO_INPUT_COLLECTIONS.split(",")

    with ThreadPoolExecutor(max_workers=5) as executor:
        for collection in input_collections:
            executor.submit(
                watch_collection,
                db,
                collection.strip(),
                MONGO_GEO_OUTPUT_COLLECTION,
            )


if __name__ == "__main__":
    run()
