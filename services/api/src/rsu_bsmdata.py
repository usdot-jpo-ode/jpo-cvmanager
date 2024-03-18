import common.util as util
import os
import logging
from datetime import datetime
from pymongo import MongoClient

coord_resolution = 0.0001  # lats more than this are considered different
time_resolution = 10  # time deltas bigger than this are considered different


def bsm_hash(ip, timestamp, long, lat):
    return (
        ip
        + "_"
        + str(int(timestamp / time_resolution))
        + "_"
        + str(int(long / coord_resolution))
        + "_"
        + str(int(lat / coord_resolution))
    )


def query_bsm_data_mongo(pointList, start, end):
    start_date = util.format_date_utc(start, "DATETIME")
    end_date = util.format_date_utc(end, "DATETIME")

    try:
        client = MongoClient(os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000)
        db = client[os.getenv("MONGO_DB_NAME")]
        collection = db[os.getenv("BSM_DB_NAME")]
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo counts collection with error message: {e}"
        )
        return [], 503

    filter = {
        "properties.timestamp": {"$gte": start_date, "$lte": end_date},
        "geometry": {
            "$geoWithin": {"$geometry": {"type": "Polygon", "coordinates": [pointList]}}
        },
    }
    hashmap = {}
    count = 0
    total_count = 0

    try:
        logging.debug(
            f"Running filter: {filter} on mongo collection {os.getenv('BSM_DB_NAME')}"
        )
        for doc in collection.find(filter=filter):
            message_hash = bsm_hash(
                doc["properties"]["id"],
                int(datetime.timestamp(doc["properties"]["timestamp"])),
                doc["geometry"]["coordinates"][0],
                doc["geometry"]["coordinates"][1],
            )

            if message_hash not in hashmap:
                doc["properties"]["time"] = doc["properties"]["timestamp"].strftime(
                    "%Y-%m-%dT%H:%M:%Sz"
                )
                doc.pop("_id")
                doc["properties"].pop("timestamp")
                hashmap[message_hash] = doc
                count += 1
                total_count += 1
            else:
                total_count += 1

        logging.info(
            f"Filter successful. Records returned: {count}, Total records: {total_count}"
        )
        return list(hashmap.values()), 200
    except Exception as e:
        logging.error(f"Filter failed: {e}")
        return [], 500


# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource
from marshmallow import Schema, fields


class RsuBsmDataSchema(Schema):
    geometry = fields.String(required=False)
    start = fields.DateTime(required=False)
    end = fields.DateTime(required=False)


class RsuBsmData(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def post(self):
        logging.debug("RsuBsmData POST requested")

        # Get arguments from request
        try:
            data = request.json
            pointList = data["geometry"]
            start = data["start"]
            end = data["end"]
        except:
            return (
                'Body format: {"start": string, "end": string, "geometry": coordinate list}',
                400,
                self.headers,
            )

        data, code = query_bsm_data_mongo(pointList, start, end)

        return (data, code, self.headers)
