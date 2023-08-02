from google.cloud import bigquery
from datetime import datetime, timedelta
import pgquery
import util
import os
import logging
import json
from pymongo import MongoClient


def query_rsu_counts_mongo(allowed_ips, message_type, start, end):
    start_date = util.format_date_utc(start, "DATETIME")
    end_date = util.format_date_utc(end, "DATETIME")

    try:
        client = MongoClient(os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000)
        db = client[os.getenv("MONGO_DB_NAME")]
        collection = db[os.getenv("COUNTS_DB_NAME")]
    except Exception as e:
        logging.error(f"Failed to connect to Mongo counts collection with error message: {e}")
        return {}, 503

    filter = {
        "timestamp": {"$gte": start_date, "$lt": end_date},
        "message_type": message_type.upper(),
    }

    result = {}
    count = 0
    try:
        logging.debug(f"Running filter: {filter}, on collection: {collection.name}")
        for doc in collection.find(filter=filter):
            if doc["ip"] in allowed_ips:
                count += 1
                item = {"road": doc["road"], "count": doc["count"]}
                result[doc["ip"]] = item

        logging.info(f"Filter successful. Length of data: {count}")
        return result, 200
    except Exception as e:
        logging.error(f"Filter failed: {e}")
        return {}, 500


def query_rsu_counts_bq(allowed_ips, message_type, start, end):
    start_date = util.format_date_utc(start)
    end_date = util.format_date_utc(end)
    client = bigquery.Client()
    tablename = os.environ["COUNTS_DB_NAME"]

    query = (
        "SELECT RSU, Road, SUM(Count) as Count "
        f"FROM `{tablename}` "
        f'WHERE Date >= DATETIME("{start_date}") '
        f'AND Date < DATETIME("{end_date}") '
        f'AND Type = "{message_type.upper()}" '
        f"GROUP BY RSU, Road "
    )

    logging.info(f"Running query on table {tablename}")

    query_job = client.query(query)

    result = {}
    count = 0
    for row in query_job:
        if row["RSU"] in allowed_ips:
            count += 1
            item = {"road": row["Road"], "count": row["Count"]}
            result[row["RSU"]] = item

    logging.info(f"Query successful. Length of data: {count}")

    return result, 200


def get_organization_rsus(organization):
    logging.info(f"Preparing to query for all RSU IPs for {organization}...")

    # Execute the query and fetch all results
    query = (
        "SELECT jsonb_build_object('ip', rd.ipv4_address) "
        "FROM public.rsus AS rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{organization}' "
        "ORDER BY rd.ipv4_address"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)
    logging.debug(str(data))
    ips = [rsu[0]["ip"] for rsu in data]
    return ips


# REST endpoint resource class and schema
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields


class RsuQueryCountsSchema(Schema):
    message = fields.String(required=False)
    start = fields.DateTime(required=False)
    end = fields.DateTime(required=False)


class RsuQueryCounts(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }

    headers = {"Access-Control-Allow-Origin": "*", "Content-Type": "application/json"}

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def get(self):
        logging.debug("RsuQueryCounts GET requested")
        # Schema check for arguments
        schema = RsuQueryCountsSchema()
        errors = schema.validate(request.args)
        if errors:
            abort(400, str(errors))
        # Get arguments from request and set defaults if not provided
        message = request.args.get("message", default="BSM")
        start = request.args.get(
            "start",
            default=((datetime.now() - timedelta(1)).strftime("%Y-%m-%dT%H:%M:%S")),
        )
        end = request.args.get("end", default=((datetime.now()).strftime("%Y-%m-%dT%H:%M:%S")))
        # Validate request with supported message types
        logging.debug(f"COUNTS_MSG_TYPES: {os.getenv('COUNTS_MSG_TYPES','NOT_SET')}")
        msgList = json.loads(os.getenv('COUNTS_MSG_TYPES','["TIM","BSM","SPAT","PSM","MAP"]'))
        msgList = [x.upper() for x in msgList]
        if message.upper() not in msgList:
            return (
                "Invalid Message Type.\nValid message types: " + ', '.join(msgList),
                400,
                self.headers,
            )
        db_type = os.getenv("COUNTS_DB_TYPE", "BIGQUERY").upper()
        data = 0
        code = 204

        rsus = get_organization_rsus(request.environ["organization"])
        if db_type == "BIGQUERY":
            data, code = query_rsu_counts_bq(rsus, message.upper(), start, end)
        elif db_type == "MONGODB":
            data, code = query_rsu_counts_mongo(rsus, message.upper(), start, end)

        return (data, code, self.headers)
