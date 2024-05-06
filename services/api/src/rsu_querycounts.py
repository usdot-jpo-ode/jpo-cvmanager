from datetime import datetime, timedelta
import common.pgquery as pgquery
import common.util as util
import os
import logging
from pymongo import MongoClient

message_types = {
    "bsm": "BSM",
    "map": "Map",
    "spat": "SPaT",
    "srm": "SRM",
    "ssm": "SSM",
    "tim": "TIM",
}


def query_rsu_counts_mongo(allowed_ips_dict, message_type, start, end):
    start_dt = util.format_date_utc(start, "DATETIME")
    end_dt = util.format_date_utc(end, "DATETIME")

    try:
        client = MongoClient(os.getenv("MONGO_DB_URI"), serverSelectionTimeoutMS=5000)
        mongo_db = client[os.getenv("MONGO_DB_NAME")]
        collection = mongo_db[f"CVCounts"]
    except Exception as e:
        logging.error(
            f"Failed to connect to Mongo counts collection with error message: {e}"
        )
        return {}, 503

    result = {}
    for rsu_ip in allowed_ips_dict:
        query = {
            "messageType": message_types[message_type.lower()],
            "rsuIp": rsu_ip,
            "timestamp": {
                "$gte": start_dt,
                "$lt": end_dt,
            },
        }

        try:
            logging.debug(f"Running query: {query}, on collection: {collection.name}")
            response = collection.find_one(query)
            if not response:
                item = {"road": allowed_ips_dict[rsu_ip], "count": 0}
            else:
                item = {"road": allowed_ips_dict[rsu_ip], "count": response["count"]}
            result[rsu_ip] = item
        except Exception as e:
            logging.error(f"Filter failed: {e}")
            return {}, 500

    return result, 200


def get_organization_rsus(organization):
    logging.info(f"Preparing to query for all RSU IPs for {organization}...")

    # Execute the query and fetch all results
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rd.ipv4_address, rd.primary_route "
        "FROM public.rsus rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{organization}' "
        "ORDER BY primary_route ASC, milepost ASC"
        ") as row"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    rsu_dict = {}
    for row in data:
        row = dict(row[0])
        rsu_dict[row["ipv4_address"]] = row["primary_route"]
    return rsu_dict


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
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
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
        end = request.args.get(
            "end", default=((datetime.now()).strftime("%Y-%m-%dT%H:%M:%S"))
        )

        # Validate request with supported message types
        logging.debug(f"COUNTS_MSG_TYPES: {os.getenv('COUNTS_MSG_TYPES','NOT_SET')}")
        msgList = os.getenv("COUNTS_MSG_TYPES", "BSM,SSM,SPAT,SRM,MAP")
        msgList = [msgtype.strip().title() for msgtype in msgList.split(",")]
        if message.title() not in msgList:
            return (
                "Invalid Message Type.\nValid message types: " + ", ".join(msgList),
                400,
                self.headers,
            )
        data = 0
        code = 204

        rsu_dict = get_organization_rsus(request.environ["organization"])
        data, code = query_rsu_counts_mongo(rsu_dict, message, start, end)

        return (data, code, self.headers)
