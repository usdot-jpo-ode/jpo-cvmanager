from google.cloud import bigquery
import util
import pytz
import json
import os
import logging
from pymongo import MongoClient
from bson.json_util import loads


def query_bsm_data_mongo(pointList, start, end):
    start_date = util.format_date_utc_as_date(start)
    end_date = util.format_date_utc_as_date(end)

    client = MongoClient(os.getenv("MONGO_DB_URI"))
    db = client[os.getenv("MONGO_DB_NAME")]
    collection = db[os.getenv("BSM_DB_NAME")]

    query = {
        "properties.timestamp": {"$gte": start_date, "$lte": end_date},
        "geometry": {
            "$geoWithin": {"$geometry": {"type": "Polygon", "coordinates": [pointList]}}
        },
    }
    result = []
    count = 0

    logging.debug(
        f"Running query: {query} on mongo collection {os.getenv('BSM_DB_NAME')}"
    )

    for doc in collection.find(query):
        doc["properties"]["time"] = doc["properties"]["timestamp"].strftime(
            "%Y-%m-%dT%H:%M:%SZ"
        )
        doc.pop("_id")
        doc["properties"].pop("timestamp")
        result.append(doc)
        count += 1

    logging.info(f"Query successful. Records returned: {count}")

    return result, 200


def query_bsm_data_bq(pointList, start, end):
    start_date = util.format_date_utc(start)
    end_date = util.format_date_utc(end)
    client = bigquery.Client()
    tablename = os.environ["BSM_DB_NAME"]

    geogString = "POLYGON(("
    for elem in pointList:
        long = str(elem.pop(0))
        lat = str(elem.pop(0))
        geogString += long + " " + lat + ","

    geogString = geogString[:-1] + "))"

    query = (
        "SELECT DISTINCT bsm.metadata.originIp as Ip, "
        f"bsm.payload.data.coreData.position.longitude as long, "
        f"bsm.payload.data.coreData.position.latitude as lat, "
        f"bsm.metadata.odeReceivedAt as time "
        f"FROM `{tablename}` "
        f'WHERE TIMESTAMP(bsm.metadata.odeReceivedAt) >= TIMESTAMP("{start_date}") '
        f'AND TIMESTAMP(bsm.metadata.odeReceivedAt) <= TIMESTAMP("{end_date}") '
        f"AND ST_CONTAINS(ST_GEOGFROM('{geogString}'), "
        f"ST_GEOGPOINT(bsm.payload.data.coreData.position.longitude, bsm.payload.data.coreData.position.latitude))"
    )

    logging.info(f"Running query on table {tablename}")

    query_job = client.query(query)

    result = []
    count = 0
    for row in query_job:
        result.append(
            {
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [row["long"], row["lat"]]},
                "properties": {
                    "id": row["Ip"],
                    "time": util.format_date_utc(row["time"]),
                },
            }
        )
        count += 1

    logging.info(f"Query successful. Record returned: {count}")

    return result, 200


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
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {"Access-Control-Allow-Origin": "*", "Content-Type": "application/json"}

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
        db_type = os.getenv("GEO_BSM_DB_TYPE", "BIGQUERY")
        data = []
        code = None

        if db_type == "BIGQUERY":
            data, code = query_bsm_data_bq(pointList, start, end)
        elif db_type == "MONGODB":
            data, code = query_bsm_data_mongo(pointList, start, end)

        return (data, code, self.headers)
