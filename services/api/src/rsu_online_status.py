from datetime import datetime, timedelta
import logging
import common.util as util
import common.pgquery as pgquery
import os


# Function for querying PostgreSQL db for the last 15 minutes of ping data for every RSU
def get_ping_data(organization):
    logging.info("Grabbing the last 20 minutes of the data")
    result = {}

    t = datetime.utcnow() - timedelta(minutes=20)
    # Execute the query and fetch all results
    query = (
        "SELECT jsonb_build_object('id', rd.rsu_id, 'ip', rd.ipv4_address, 'datetime', ping_data.timestamp, 'online_status', ping_data.result) "
        "FROM public.rsus AS rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "JOIN ("
        "SELECT * FROM public.ping AS ping_data "
        f"WHERE ping_data.timestamp >= '{t.strftime('%Y/%m/%dT%H:%M:%S')}'::timestamp"
        ") AS ping_data ON rd.rsu_id = ping_data.rsu_id "
        f"WHERE ron_v.name = '{organization}' "
        "ORDER BY rd.rsu_id, ping_data.timestamp DESC"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    logging.info("Parsing results...")
    for row in data:
        row = dict(row[0])
        ip = row["ip"]
        if ip not in result:
            result[ip] = {}
            result[ip]["checked_timestamps"] = []
            result[ip]["online_statuses"] = []

        result[ip]["checked_timestamps"].append(row["datetime"])
        result[ip]["online_statuses"].append(row["online_status"])

    return result


# Function for querying PostgreSQL db for the last online timestamp of a specified RSU
def get_last_online_data(ip, organization):
    logging.info(f"Preparing to query last RSU online status for {ip}...")
    result = {}

    # Execute the query and fetch all results
    query = (
        "SELECT ping.timestamp "
        "FROM public.ping "
        "JOIN ("
        "SELECT rsus.rsu_id, rsus.ipv4_address "
        "FROM public.rsus "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rsus.rsu_id "
        f"WHERE rsus.ipv4_address = '{ip}' "
        f"AND ron_v.name = '{organization}'"
        ") AS rd ON ping.rsu_id = rd.rsu_id "
        "WHERE ping.rsu_id = rd.rsu_id "
        "AND result = '1' "
        "ORDER BY ping.timestamp DESC "
        "LIMIT 1"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)
    result = [value[0] for value in data]

    return {
        "ip": ip,
        "last_online": (
            util.format_date_denver(result[0].strftime("%m/%d/%Y %I:%M:%S %p"))
            if len(result) != 0
            else "No Data"
        ),
    }


# duration - duration of online status calculated (in minutes)
def get_rsu_online_statuses(organization):
    result = {}
    # query ping data
    ping_result = get_ping_data(organization)

    # calculate online status
    for key, value in ping_result.items():
        result[key] = {}
        result[key]["current_status"] = "offline"
        for x in range(len(value["online_statuses"])):
            if value["online_statuses"][x] == "1":
                result[key]["current_status"] = "online" if x == 0 else "unstable"
                break

    return result


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields


class RsuOnlineStatusSchema(Schema):
    rsu_ip = fields.IPv4(required=False)


class RsuOnlineStatus(Resource):
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
        logging.debug("RsuOnlineStatus GET requested")
        schema = RsuOnlineStatusSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        if "rsu_ip" in request.args:
            return (
                get_last_online_data(
                    request.args["rsu_ip"], request.environ["organization"]
                ),
                200,
                self.headers,
            )
        else:
            return (
                get_rsu_online_statuses(request.environ["organization"]),
                200,
                self.headers,
            )
