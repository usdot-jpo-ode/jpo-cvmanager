import common.util as util
import os
import logging
from datetime import datetime, timedelta
from pymongo import MongoClient


def query_ssm_data_mongo(result):
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())

    try:
        client = MongoClient(os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000)
        db = client[os.getenv("MONGO_DB_NAME")]
        collection = db[os.getenv("SSM_DB_NAME")]
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo counts collection with error message: {e}"
        )
        return [], 503

    filter = {"recordGeneratedAt": {"$gte": start_utc, "$lte": end_utc}}
    project = {
        "recordGeneratedAt": 1,
        "metadata.originIp": 1,
        "metadata.recordType": 1,
        "payload.data.status.signalStatus.sigStatus.signalStatusPackage.requester.request": 1,
        "payload.data.status.signalStatus.sigStatus.signalStatusPackage.requester.role": 1,
        "payload.data.status.signalStatus.sigStatus.signalStatusPackage.status": 1,
        "_id": 0,
    }

    logging.debug(f"Running filter on SSM mongoDB collection")

    # The data schema for the mongoDB collection is the same for the OdeSsmJson schema
    # This can be viewed here: https://github.com/usdot-jpo-ode/jpo-ode/blob/develop/jpo-ode-core/src/main/resources/schemas/schema-ssm.json
    try:
        for doc in collection.find(filter, project):
            result.append(
                {
                    "time": util.format_date_denver(doc["recordGeneratedAt"]),
                    "ip": doc["metadata"]["originIp"],
                    "requestId": doc["payload"]["data"]["status"]["signalStatus"][0][
                        "sigStatus"
                    ]["signalStatusPackage"][0]["requester"]["request"],
                    "role": doc["payload"]["data"]["status"]["signalStatus"][0][
                        "sigStatus"
                    ]["signalStatusPackage"][0]["requester"]["role"],
                    "status": doc["payload"]["data"]["status"]["signalStatus"][0][
                        "sigStatus"
                    ]["signalStatusPackage"][0]["status"],
                    "type": doc["metadata"]["recordType"],
                }
            )
        return 200, result
    except Exception as e:
        logging.error(f"SSM filter failed: {e}")
        return 500, result


def query_srm_data_mongo(result):
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())

    try:
        client = MongoClient(os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000)
        db = client[os.getenv("MONGO_DB_NAME")]
        collection = db[os.getenv("SRM_DB_NAME")]
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo counts collection with error message: {e}"
        )
        return [], 503

    filter = {"recordGeneratedAt": {"$gte": start_utc, "$lte": end_utc}}
    project = {
        "recordGeneratedAt": 1,
        "metadata.originIp": 1,
        "metadata.recordType": 1,
        "payload.data.requests.signalRequestPackage.request.requestID": 1,
        "payload.data.requestor.type.role": 1,
        "payload.data.requestor.position.position.latitude": 1,
        "payload.data.requestor.position.position.longitude": 1,
        "_id": 0,
    }

    logging.debug(f"Running filter on SRM mongoDB collection")

    # The data schema for the mongoDB collection is the same for the OdeSrmJson schema
    # This can be viewed here: https://github.com/usdot-jpo-ode/jpo-ode/blob/develop/jpo-ode-core/src/main/resources/schemas/schema-srm.json
    try:
        for doc in collection.find(filter, project):
            result.append(
                {
                    "time": util.format_date_denver(doc["recordGeneratedAt"]),
                    "ip": doc["metadata"]["originIp"],
                    "requestId": doc["payload"]["data"]["requests"][
                        "signalRequestPackage"
                    ][0]["request"]["requestID"],
                    "role": doc["payload"]["data"]["requestor"]["type"]["role"],
                    "lat": doc["payload"]["data"]["requestor"]["position"]["position"][
                        "latitude"
                    ],
                    "long": doc["payload"]["data"]["requestor"]["position"]["position"][
                        "longitude"
                    ],
                    "type": doc["metadata"]["recordType"],
                    "status": "N/A",
                }
            )
        return 200, result
    except Exception as e:
        logging.error(f"SRM filter failed: {e}")
        return 500, result


# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource


class RsuSsmSrmData(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def get(self):
        logging.debug("RsuSsmSrmData GET requested")
        data = []
        code, ssmRes = query_ssm_data_mongo(data)
        code, finalRes = query_srm_data_mongo(ssmRes)
        finalRes.sort(key=lambda x: x["time"])
        return (finalRes, code, self.headers)
