import common.util as util
import os
import logging
from datetime import datetime
from pymongo import MongoClient, ASCENDING, DESCENDING, GEOSPHERE
import math
from bson.json_util import dumps
from bson.json_util import loads
import json

coord_resolution = 0.0001  # lats more than this are considered different
time_resolution = 10  # time deltas bigger than this are considered different


def geo_hash(id, timestamp, long, lat):
    # Generate a hash of the message based on the id, timestamp, and coordinates
    # this is used to deduplicate messages - we hope to remove this in the future
    # when the `ProcessedBsm` and `ProcessedPsm` collections are sourced from deduplicated
    # topics
    return (
        id
        + "_"
        + str(int(timestamp / time_resolution))
        + "_"
        + str(int(long / coord_resolution))
        + "_"
        + str(int(lat / coord_resolution))
    )


def get_collection(msg_type):
    """Get MongoDB collection based on message type"""
    mongo_uri = os.getenv("MONGO_DB_URI")
    db_name = os.getenv("MONGO_DB_NAME")

    if msg_type.lower() == "bsm":
        coll_name = os.getenv("MONGO_PROCESSED_BSM_COLLECTION_NAME", "ProcessedBsm")
    elif msg_type.lower() == "psm":
        coll_name = os.getenv("MONGO_PROCESSED_PSM_COLLECTION_NAME", "ProcessedPsm")
    else:
        return None, None, 400

    try:
        logging.debug(
            f"Connecting to Mongo {coll_name} collection with URI: {mongo_uri} with db: {db_name}"
        )
        client = MongoClient(mongo_uri, serverSelectionTimeoutMS=5000)
        db = client[db_name]
        collection = db[coll_name]
        return collection, coll_name, 200
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo {coll_name} collection with error message: {e}"
        )
        return None, None, 503


def create_geo_filter(pointList, start, end):
    """Create MongoDB filter for geo query"""
    start_date = util.format_date_utc(start, "DATETIME")
    end_date = util.format_date_utc(end, "DATETIME")

    return {
        "properties.timeStamp": {
            "$gte": datetime.strftime(start_date, "%Y-%m-%dT%H:%M:%SZ"),
            "$lte": datetime.strftime(end_date, "%Y-%m-%dT%H:%M:%SZ"),
        },
        "geometry": {
            "$geoWithin": {"$geometry": {"type": "Polygon", "coordinates": [pointList]}}
        },
    }


def create_or_update_mongodb_index(collection):
    collection.create_index(
        [("properties.timeStamp", ASCENDING), ("geometry", GEOSPHERE)]
    )


def query_geo_data_mongo(pointList, start, end, msg_type):
    collection, coll_name, status_code = get_collection(msg_type)
    if status_code != 200:
        return [], status_code

    create_or_update_mongodb_index(collection)
    filter = create_geo_filter(pointList, start, end)
    hashmap = {}
    count = 0
    total_count = 0
    # If MAX_GEO_QUERY_RECORDS is not set or is an empty string, use 10000 as default
    max_records = os.getenv("MAX_GEO_QUERY_RECORDS", 10000) or 10000
    # Convert to int in a separate step to avoid errors
    max_records = int(max_records)

    try:
        logging.debug(f"Running filter: {filter} on mongo collection {coll_name}")
        num_docs = collection.count_documents(filter)

        filter_record = math.ceil(num_docs / max_records)

        for doc in collection.find(filter=filter):
            if doc["properties"]["schemaVersion"] != 8:
                logging.warning(
                    f"Skipping message with schema version {doc['properties']['schemaVersion']}"
                )
                continue

            message_hash = geo_hash(
                doc["properties"]["id"],
                int(
                    util.format_date_utc(
                        doc["properties"]["timeStamp"], "DATETIME"
                    ).timestamp()
                ),
                doc["geometry"]["coordinates"][0],
                doc["geometry"]["coordinates"][1],
            )

            if message_hash not in hashmap:
                # Add first, last, and every nth record
                if (
                    count == 0
                    or num_docs == (total_count + 1)
                    or total_count % filter_record == 0
                ):
                    geo_msg = build_geo_data_response(doc)
                    hashmap[message_hash] = geo_msg
                    count += 1
                    total_count += 1
                    doc.pop("_id")
                    doc["properties"]["id"] = "ABC12345"
                    doc["properties"]["originIp"] = "8.8.8.8"
                else:
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


def build_geo_data_response(doc):
    """
    Builds a response object from a processed BSM/PSM document that conforms to RsuGeoMsg schema.

    Args:
        doc: A processed BSM/PSM document from MongoDB

    Returns:
        dict: A document conforming to RsuGeoMsg schema
    """
    # Create the properties object
    properties = {
        "schemaVersion": 1,
        "id": doc["properties"]["id"],
        "originIp": doc["properties"]["originIp"],
        "messageType": doc["properties"]["messageType"],
        "time": doc["properties"]["timeStamp"],
        "heading": doc["properties"].get("heading", 0.0),
        "msgCnt": doc["properties"].get("msgCnt", 0),
        "speed": doc["properties"].get("speed", 0.0),
    }

    # Create the full GeoMsg object
    geo_msg = {"type": "Feature", "geometry": doc["geometry"], "properties": properties}

    # Use the schema to validate and serialize
    schema = RsuGeoMsg()
    return schema.load(geo_msg)


# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource
from marshmallow import Schema, fields

RsuGeoMsgTypes = ["BSM", "PSM"]


class RsuGeoMsgProperties(Schema):
    schemaVersion = fields.Integer(required=True)
    id = fields.String(required=True)
    originIp = fields.String(required=True)
    messageType = fields.String(required=True, validate=lambda x: x in RsuGeoMsgTypes)
    time = fields.String(required=True)
    heading = fields.Float(required=True)
    msgCnt = fields.Integer(required=True)
    speed = fields.Float(required=True)


class RsuGeoMsg(Schema):
    type = fields.String(required=True, default="Feature")
    geometry = fields.Dict(
        required=True,
        keys=fields.String(),
        values=fields.Raw(),
        validate=lambda x: x.get("type") in ["Point", "Polygon", "LineString"],
    )
    properties = fields.Nested(RsuGeoMsgProperties, required=True)


class RsuGeoDataSchema(Schema):
    geometry = fields.String(required=False)
    start = fields.DateTime(required=False)
    end = fields.DateTime(required=False)
    msg_type = fields.String(required=False)


class RsuGeoData(Resource):
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
        logging.debug("RsuGeoData POST requested")

        # Get arguments from request
        try:
            data = request.json
            msg_type = data["msg_type"]
            pointList = data["geometry"]
            start = data["start"]
            end = data["end"]
        except:
            return (
                'Body format: {"start": string, "end": string, "geometry": coordinate list}',
                400,
                self.headers,
            )

        data, code = query_geo_data_mongo(pointList, start, end, msg_type.capitalize())

        return (data, code, self.headers)
