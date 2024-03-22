import os
from concurrent.futures import ThreadPoolExecutor
import logging
from pymongo import MongoClient, DESCENDING, GEOSPHERE
from datetime import datetime


def set_mongo_client(MONGO_DB_URI, MONGO_DB, MONGO_BSM_INPUT_COLLECTION):
    client = MongoClient(MONGO_DB_URI)
    db = client[MONGO_DB]
    collection = db[MONGO_BSM_INPUT_COLLECTION]
    return db, collection


def create_message(original_message):
    new_message = {
        "type": "Feature",
        "geometry": {
            "type": "Point",
            "coordinates": [
                original_message["payload"]["data"]["coreData"]["position"][
                    "longitude"
                ],
                original_message["payload"]["data"]["coreData"]["position"]["latitude"],
            ],
        },
        "properties": {
            "id": original_message["metadata"]["originIp"],
            "timestamp": datetime.strptime(
                original_message["metadata"]["odeReceivedAt"], "%Y-%m-%dT%H:%M:%S.%fZ"
            ),
        },
    }
    return new_message


def process_message(message, db, collection):
    new_message = create_message(message)
    db[collection].insert_one(new_message)


def run():
    MONGO_DB_URI = os.getenv("MONGO_DB_URI")
    MONGO_DB = os.getenv("MONGO_DB_NAME")
    MONGO_BSM_INPUT_COLLECTION = os.getenv("MONGO_BSM_INPUT_COLLECTION")
    MONGO_GEO_OUTPUT_COLLECTION = os.getenv("MONGO_GEO_OUTPUT_COLLECTION")

    if (
        MONGO_DB_URI is None
        or MONGO_BSM_INPUT_COLLECTION is None
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

    db, collection = set_mongo_client(
        MONGO_DB_URI, MONGO_DB, MONGO_BSM_INPUT_COLLECTION
    )

    count = 0
    with collection.watch() as stream:
        for change in stream:
            count += 1
            executor.submit(
                process_message, change["fullDocument"], db, MONGO_GEO_OUTPUT_COLLECTION
            )
            logging.info(count)
            if count == 1:
                output_collection = db[MONGO_GEO_OUTPUT_COLLECTION]
                index_info = output_collection.index_information()
                # If the geo-spatial index doesn't exist, create it
                if "timestamp_geosphere_index" not in index_info:
                    output_collection.create_index(
                        [("properties.timestamp", DESCENDING), ("geometry", GEOSPHERE)],
                        name="timestamp_geosphere_index",
                    )


if __name__ == "__main__":
    run()
