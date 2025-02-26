from flask_restful import Resource
import common.util as util
import os
import logging
from datetime import datetime, timedelta
from pymongo import MongoClient
from werkzeug.exceptions import InternalServerError, ServiceUnavailable

from common.auth_tools import require_permission


def query_ssm_data_mongo(result: list) -> list:
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())

    try:        
        client: MongoClient = MongoClient(
            os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000
        )
        mongo_db_name = os.getenv("MONGO_DB_NAME")
        srm_db_name = os.getenv("SRM_DB_NAME")
        if not mongo_db_name or not srm_db_name:
            raise Exception("Missing environment variables for MongoDB")
        db = client[mongo_db_name]
        collection = db[srm_db_name]
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo counts collection with error message: {e}"
        )
        raise ServiceUnavailable("Failed to connect to MongoDB") from e

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

    logging.debug("Running filter on SSM mongoDB collection")

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
        return result
    except Exception as e:
        logging.error(f"SSM filter failed: {e}")
        raise InternalServerError("Encountered unknown issue") from e


def query_srm_data_mongo(result: list) -> list:
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())

    try:
        client: MongoClient = MongoClient(
            os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000
        )
        mongo_db_name = os.getenv("MONGO_DB_NAME")
        srm_db_name = os.getenv("SRM_DB_NAME")
        if not mongo_db_name or not srm_db_name:
            raise Exception("Missing environment variables for MongoDB")
        db = client[mongo_db_name]
        collection = db[srm_db_name]
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo counts collection with error message: {e}"
        )
        raise ServiceUnavailable("Failed to connect to MongoDB") from e

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

    logging.debug("Running filter on SRM mongoDB collection")

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
        return result
    except Exception as e:
        logging.error(f"SRM filter failed: {e}")
        raise InternalServerError("Encountered unknown issue") from e


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

    @require_permission(required_role=None)
    def get(self):
        logging.debug("RsuSsmSrmData GET requested")
        data = []

        # TODO: Filter by RSUs within authenticated organizations
        ssmRes = query_ssm_data_mongo(data)
        finalRes = query_srm_data_mongo(ssmRes)
        finalRes.sort(key=lambda x: x["time"])
        return (finalRes, 200, self.headers)
