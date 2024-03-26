import os
from concurrent.futures import ThreadPoolExecutor
import logging
from pymongo import MongoClient, DESCENDING, GEOSPHERE
from datetime import datetime


def set_mongo_client(MONGO_DB_URI, MONGO_DB):
    client = MongoClient(MONGO_DB_URI)
    db = client[MONGO_DB]
    return db


def create_message(original_message, msg_type):
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
                    original_message["metadata"]["odeReceivedAt"],
                    "%Y-%m-%dT%H:%M:%S.%fZ",
                ),
                "msg_type": msg_type,
            },
        }
        return new_message
    else:
        logging.warn(f"create_message: Could not create a message for type: {msg_type}")
        return None


def process_message(message, db, collection):
    new_message = create_message(message)
    db[collection].insert_one(new_message)


def watch_collection(db, input_collection, output_collection, executor):
    count = 0
    with db[input_collection].watch() as stream:
        for change in stream:
            count += 1
            executor.submit(
                process_message, change["fullDocument"], db, output_collection
            )
            logging.info(count)
            if count == 1:
                output_collection_obj = db[output_collection]
                index_info = output_collection_obj.index_information()
                # If the geo-spatial index doesn't exist, create it
                if "timestamp_geosphere_index" not in index_info:
                    output_collection_obj.create_index(
                        [("properties.timestamp", DESCENDING), ("geometry", GEOSPHERE)],
                        name="timestamp_geosphere_index",
                    )


def run():
    # MONGO_DB_URI = os.getenv("MONGO_DB_URI")
    MONGO_DB_URI = "mongodb://admin:admin@172.29.11.116:27017/"
    # MONGO_DB = os.getenv("MONGO_DB_NAME")
    MONGO_DB = "ode"
    # MONGO_INPUT_COLLECTIONS = os.getenv("MONGO_INPUT_COLLECTIONS")
    MONGO_INPUT_COLLECTIONS = "OdeBsmJson,OdePsmJson"
    # MONGO_GEO_OUTPUT_COLLECTION = os.getenv("MONGO_GEO_OUTPUT_COLLECTION")
    MONGO_GEO_OUTPUT_COLLECTION = "geo_messages"
    if (
        MONGO_DB_URI is None
        or MONGO_INPUT_COLLECTIONS is None
        or MONGO_DB is None
        or MONGO_GEO_OUTPUT_COLLECTION is None
    ):
        logging.error("Environment variables are not set! Exiting.")
        exit("Environment variables are not set! Exiting.")

    log_level = (
        "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]
    )
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    executor = ThreadPoolExecutor(max_workers=5)

    db = set_mongo_client(MONGO_DB_URI, MONGO_DB)

    # Parse MONGO_INPUT_COLLECTIONS into a list of strings
    input_collections = MONGO_INPUT_COLLECTIONS.split(",")

    for collection in input_collections:
        executor.submit(
            watch_collection,
            db,
            collection.strip(),
            MONGO_GEO_OUTPUT_COLLECTION,
            executor,
        )


if __name__ == "__main__":
    run()
