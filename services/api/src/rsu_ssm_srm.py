from typing import Any
from flask_restful import Resource
import common.util as util
import api_environment
import logging
from datetime import datetime, timedelta
from pymongo import MongoClient
from werkzeug.exceptions import InternalServerError, ServiceUnavailable

from common.auth_tools import (
    ORG_ROLE_LITERAL,
    PermissionResult,
    require_permission,
    get_rsu_set_for_org,
)


def query_ssm_data_mongo() -> list:
    results = []
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())

    try:
        client = MongoClient(
            api_environment.MONGO_DB_URI, serverSelectionTimeoutMS=5000
        )
        db = client[api_environment.MONGO_DB_NAME]
        collection = db[api_environment.MONGO_SSM_COLLECTION_NAME]
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
            results.append(
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
        return results
    except Exception as e:
        logging.error(f"SSM filter failed: {e}")
        raise InternalServerError("Encountered unknown issue") from e


def query_srm_data_mongo() -> list:
    results = []
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())

    try:
        client = MongoClient(
            api_environment.MONGO_DB_URI, serverSelectionTimeoutMS=5000
        )
        db = client[api_environment.MONGO_DB_NAME]
        collection = db[api_environment.MONGO_SRM_COLLECTION_NAME]
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
            results.append(
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
        return results
    except Exception as e:
        logging.error(f"SRM filter failed: {e}")
        raise InternalServerError("Encountered unknown issue") from e


def filter_results_by_ip_address(
    results: list[dict[str, Any]], valid_ips: set[str]
) -> list:
    return [result for result in results if result["ip"] in valid_ips]


class RsuSsmSrmData(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET",
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
    def get(self, permission_result: PermissionResult):
        logging.debug("RsuSsmSrmData GET requested")
        data = []

        data.extend(query_ssm_data_mongo())
        data.extend(query_srm_data_mongo())
        data.sort(key=lambda x: x["time"])

        # Filter by RSUs within authenticated organizations
        if permission_result.user.organization:
            allowed_ips = get_rsu_set_for_org([permission_result.user.organization])
        else:
            allowed_ips = get_rsu_set_for_org(permission_result.qualified_orgs)

        return (filter_results_by_ip_address(data, allowed_ips), 200, self.headers)
