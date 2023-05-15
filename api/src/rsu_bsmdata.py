from google.cloud import bigquery
import util
import os
import logging
from pymongo import MongoClient


def query_bsm_data_mongo(pointList, start, end):
    start_date = util.format_date_utc(start)
    end_date = util.format_date_utc(end)

    client = MongoClient(os.environ["MONGO_CONNECTION_URI"])
    db = client[os.environ["MONGO_DB_NAME"]]
    collection = db[os.environ["BSM_DB_NAME"]]

    geogString = "POLYGON(("
    for elem in pointList:
        long = str(elem.pop(0))
        lat = str(elem.pop(0))
        geogString += long + " " + lat + ","

    geogString = geogString[:-1] + "))"

    query = {
        "$and": [
            {"metadata.odeReceivedAt": {"$gte": start_date}},
            {"metadata.odeReceivedAt": {"$lte": end_date}},
            {
                "payload.data.coreData.position": {
                    "$geoWithin": {
                        "$geometry": {"type": "Polygon", "coordinates": [pointList]}
                    }
                }
            },
        ]
    }

    result = []
    count = 0

    logging.info(f"Running query on collection {collection.name}")

    for doc in collection.find(query):
        result.append(
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        doc["payload"]["data"]["coreData"]["position"]["longitude"],
                        doc["payload"]["data"]["coreData"]["position"]["latitude"],
                    ],
                },
                "properties": {
                    "id": doc["metadata"]["originIp"],
                    "time": util.format_date_denver_iso(
                        doc["metadata"]["odeReceivedAt"]
                    ),
                },
            }
        )
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
                    "time": util.format_date_denver_iso(row["time"]),
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

        data, code = query_bsm_data_bq(pointList, start, end)
        return (data, code, self.headers)
