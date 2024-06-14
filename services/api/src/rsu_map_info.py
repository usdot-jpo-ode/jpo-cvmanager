import logging
from common.util import format_date_denver
import common.pgquery as pgquery
import os


def get_map_data(ip_address, organization):
    query = (
        "SELECT mi.geojson, mi.date "
        "FROM public.map_info AS mi "
        "JOIN public.rsus AS rd ON rd.ipv4_address = mi.ipv4_address "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{organization}' AND mi.ipv4_address = '{ip_address}'"
    )
    try:
        result = pgquery.query_db(query)
    except Exception as e:
        logging.info(f"Error selecting GeoJSON data for {ip_address}")
        return (400, f"Error selecting GeoJSON data for {ip_address}")
    return_value = {} if (len(result) > 0) else "No Data"
    for row in result:
        return_value["geojson"] = row[0]
        return_value["date"] = format_date_denver(row[1])
    return (200, return_value)


def get_ip_list(organization):
    query = (
        "SELECT mi.ipv4_address "
        "FROM public.map_info AS mi "
        "JOIN public.rsus AS rd ON rd.ipv4_address = mi.ipv4_address "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{organization}'"
    )
    try:
        result = pgquery.query_db(query)
    except Exception as e:
        logging.info(f"Error selecting ip list: {e}")
        return (400, f"Error selecting ip list")
    return_value = [] if (len(result) > 0) else "No Data"
    for row in result:
        return_value.append(str(row[0]))
    return (200, return_value)


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields


class RsuMapSchema(Schema):
    ip_address = fields.String(required=False)
    ip_list = fields.String(required=False)


class RsuMapInfo(Resource):
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
        logging.debug("RsuMapInfo GET requested")
        schema = RsuMapSchema()
        errors = schema.validate(request.args)
        if errors:
            logging.error(errors)
            abort(400, errors)

        ip = request.args.get("ip_address")
        ip_list = request.args.get("ip_list", default=False)
        if ip_list == "True":
            (code, data) = get_ip_list(request.environ["organization"])
        else:
            (code, data) = get_map_data(ip, request.environ["organization"])
        return (data, code, self.headers)
