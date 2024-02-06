from google.cloud import bigquery
import common.util as util
import os
import logging
from datetime import datetime, timedelta


def query_ssm_data(result):
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())
    client = bigquery.Client()
    tablename = os.environ["SSM_DB_NAME"]

    query = (
        "SELECT rtdh_timestamp as time, ssm.metadata.originIp as ip, "
        f"ssm.payload.data.status.signalStatus[ordinal(1)].sigStatus.signalStatusPackage[ordinal(1)].requester.request, "
        f"ssm.payload.data.status.signalStatus[ordinal(1)].sigStatus.signalStatusPackage[ordinal(1)].requester.typeData.role, "
        f"ssm.payload.data.status.signalStatus[ordinal(1)].sigStatus.signalStatusPackage[ordinal(1)].status, "
        f"ssm.metadata.recordType as type "
        f'FROM `{tablename}` WHERE TIMESTAMP(rtdh_timestamp) >= "{start_utc}" '
        f'AND TIMESTAMP(rtdh_timestamp) <= "{end_utc}" '
        f"ORDER BY rtdh_timestamp ASC"
    )

    logging.info(f"Running query on table {tablename}")

    query_job = client.query(query)

    for row in query_job:
        result.append(
            {
                "time": util.format_date_denver(row["time"].isoformat()),
                "ip": row["ip"],
                "requestId": row["request"],
                "role": row["role"],
                "status": row["status"],
                "type": row["type"],
            }
        )

    return 200, result


def query_srm_data(result):
    end_date = datetime.now()
    end_utc = util.format_date_utc(end_date.isoformat())
    start_date = end_date - timedelta(days=1)
    start_utc = util.format_date_utc(start_date.isoformat())
    client = bigquery.Client()
    tablename = os.environ["SRM_DB_NAME"]

    query = (
        "SELECT rtdh_timestamp as time, srm.metadata.originIp as ip, "
        f"srm.payload.data.requests.signalRequestPackage[ordinal(1)].request.requestID as request, "
        f"srm.payload.data.requestor.type.role, "
        f"srm.payload.data.requestor.position.position.latitude as lat, "
        f"srm.payload.data.requestor.position.position.longitude as long, "
        f"srm.metadata.recordType as type "
        f'FROM `{tablename}` WHERE TIMESTAMP(rtdh_timestamp) >= "{start_utc}" '
        f'AND TIMESTAMP(rtdh_timestamp) <= "{end_utc}" '
        f"ORDER BY rtdh_timestamp ASC"
    )

    logging.info(f"Running query on table {tablename}")

    query_job = client.query(query)

    for row in query_job:
        result.append(
            {
                "time": util.format_date_denver(row["time"].isoformat()),
                "ip": row["ip"],
                "requestId": row["request"],
                "role": row["role"],
                "lat": row["lat"],
                "long": row["long"],
                "type": row["type"],
                "status": "N/A",
            }
        )

    return 200, result


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
        code, ssmRes = query_ssm_data(data)
        code, finalRes = query_srm_data(ssmRes)
        finalRes.sort(key=lambda x: x["time"])
        return (finalRes, code, self.headers)
