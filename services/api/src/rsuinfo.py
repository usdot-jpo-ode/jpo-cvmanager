import logging
import common.pgquery as pgquery
import os

from services.api.src.auth_tools import ENVIRON_USER_KEY, EnvironWithOrg


def get_rsu_data(organization):
    # Execute the query and fetch all results
    query = (
        "SELECT jsonb_build_object('type', 'Feature', 'id', row.rsu_id, 'geometry', ST_AsGeoJSON(row.geography)::jsonb, 'properties', to_jsonb(row)) "
        "FROM ("
        "SELECT rd.rsu_id, rd.geography, rd.milepost, rd.ipv4_address, rd.serial_number, rd.primary_route, rm.name AS model_name, man.name AS manufacturer_name "
        "FROM public.rsus AS rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rd.model "
        "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer "
        f"WHERE ron_v.name = '{organization}'"
        ") AS row"
    )

    logging.debug(f'Executing query "{query};"')
    data = pgquery.query_db(query)

    logging.info("Parsing results...")
    result = {"rsuList": []}
    for row in data:
        row = dict(row[0])
        result["rsuList"].append(row)
    return result


# REST endpoint resource class
from flask import request
from flask_restful import Resource


class RsuInfo(Resource):
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
        logging.debug("RsuInfo GET requested")
        user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]
        return (get_rsu_data(user.organization), 200, self.headers)
