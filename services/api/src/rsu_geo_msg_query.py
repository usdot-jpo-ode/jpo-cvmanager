from flask import request
from flask_restful import Resource
from marshmallow import Schema, fields
import common.util as util
import api_environment
import logging
from datetime import datetime
from pymongo import MongoClient, ASCENDING, GEOSPHERE
from werkzeug.exceptions import InternalServerError
from common.auth_tools import ORG_ROLE_LITERAL, require_permission

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
    mongo_uri = api_environment.MONGO_DB_URI
    db_name = api_environment.MONGO_DB_NAME

    if msg_type.lower() == "bsm":
        coll_name = api_environment.MONGO_PROCESSED_BSM_COLLECTION_NAME
    elif msg_type.lower() == "psm":
        coll_name = api_environment.MONGO_PROCESSED_PSM_COLLECTION_NAME
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

    try:
        logging.debug(f"Running filter: {filter} on mongo collection {coll_name}")
        num_docs = collection.count_documents(filter)
        logging.debug(f"Returned records: {num_docs}")

        for doc in collection.find(filter=filter):
            if doc["properties"]["schemaVersion"] != 2:
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

            if (
                message_hash not in hashmap
                and count < api_environment.MAX_GEO_QUERY_RECORDS
            ):
                doc.pop("_id")
                doc.pop("recordGeneratedAt")

                hashmap[message_hash] = doc
                count += 1

            total_count += 1

        result = list(hashmap.values())
        result.sort(key=lambda x: x["properties"]["timeStamp"])

        logging.info(
            f"Filter successful. Records returned: {count}, Total records: {total_count}"
        )
        return result
    except Exception as e:
        logging.error(f"Filter failed: {e}")
        raise InternalServerError(
            f"Encountered unknown issue querying MongoDB: {e}"
        ) from e


class RsuGeoDataSchema(Schema):
    geometry = fields.String(required=False)
    start = fields.DateTime(required=False)
    end = fields.DateTime(required=False)
    msg_type = fields.String(required=False)


class RsuGeoData(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    @require_permission(required_role=ORG_ROLE_LITERAL.USER)
    def post(self):
        logging.debug("RsuGeoData POST requested")

        # Get arguments from request
        try:
            data = request.json
            msg_type = data["msg_type"]
            pointList = data["geometry"]
            start = data["start"]
            end = data["end"]
        except KeyError:
            return (
                'Body format: {"start": string, "end": string, "geometry": coordinate list}',
                400,
                self.headers,
            )

        return (
            query_geo_data_mongo(pointList, start, end, msg_type.capitalize()),
            200,
            self.headers,
        )
