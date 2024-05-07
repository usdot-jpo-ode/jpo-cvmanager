import os
import logging
from pymongo import MongoClient
from datetime import datetime, timedelta

message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]

def write_counts(mongo_db, counts):
    output_collection = mongo_db["CVCounts"]
    output_collection.insert_many(counts)


def count_query(mongo_db, message_type, start_dt, end_dt):
    collection = mongo_db[f"Ode{message_type.capitalize()}Json"]
    # Perform mongoDB aggregate query
    agg_result = collection.aggregate(
        [
            {
                "$match": {
                    "recordGeneratedAt": {
                        "$gte": start_dt,
                        "$lt": end_dt,
                    }
                }
            },
            {
                "$group": {
                    "_id": "$metadata.originIp",
                    "count": {"$sum": 1},
                }
            },
        ]
    )

    counts = []
    for record in agg_result:
        if not record["_id"]:
            continue
        count_record = {
            "messageType": message_type,
            "rsuIp": record["_id"],
            "timestamp": start_dt,
            "count": record["count"],
        }
        counts.append(count_record)

    return counts


def run_mongo_counter(mongo_db):
    start_dt = (datetime.now() - timedelta(days=1)).replace(
        minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(minute=0, second=0, microsecond=0)

    logging.info(f"Making counts for time period: {start_dt.strftime("%Y-%m-%d %H:%M:%S")} to {end_dt.strftime("%Y-%m-%d %H:%M:%S")}")

    rsu_counts = []
    for message_type in message_types:
        # Append counts to list so they can be written to MongoDB in one request
        rsu_counts = rsu_counts + count_query(mongo_db, message_type, start_dt, end_dt)

    logging.info("Writing counts to MongoDB")
    write_counts(mongo_db, rsu_counts)


if __name__ == "__main__":
    logging.info("Starting the MongoDB counter")
    client = MongoClient(os.getenv("MONGO_DB_URI"))
    mongo_db = client[os.getenv("MONGO_DB_NAME")]
    run_mongo_counter(mongo_db)
    logging.info("MongoDB counter has finished")
