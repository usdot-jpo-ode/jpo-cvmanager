import common.pgquery as pgquery
import logging
import os


def query_org_rsus(orgName):
    query = (
        "SELECT ipv4_address from public.rsus as rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{orgName}'"
    )

    logging.debug(query)
    data = pgquery.query_db(query)

    result = set()
    for row in data:
        ip = row[0]
        device_ip = str(ip).replace("'", "")
        result.add(device_ip)

    logging.info(f"Successfully Query for query_org_rsus")

    return result


def query_rsu_devices(ipList, pointList, vendor=None):
    geogString = "POLYGON(("
    for elem in pointList:
        long = str(elem.pop(0))
        lat = str(elem.pop(0))
        geogString += long + " " + lat + ","

    geogString = geogString[:-1] + "))"
    ipList = ", ".join(ipList)
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT ipv4_address as ip, "
        f"ST_X(geography::geometry) AS long, "
        f"ST_Y(geography::geometry) AS lat "
        f"FROM rsus "
        f"WHERE ipv4_address = ANY('{{{ipList}}}'::inet[]) "
    )
    if vendor is not None:
        query += (
            f" AND ipv4_address IN (SELECT rd.ipv4_address "
            "FROM public.rsus as rd "
            "JOIN public.rsu_models as rm ON rm.rsu_model_id = rd.model "
            "JOIN public.manufacturers as man on man.manufacturer_id = rm.manufacturer "
            f"WHERE man.name = '{vendor}') "
        )
    query +=  f"AND ST_Contains(ST_SetSRID(ST_GeomFromText('{geogString}'), 4326), rsus.geography::geometry)) as row"

    logging.debug(query)
    logging.info(f"Running query_rsu_devices")

    query_job = pgquery.query_db(query)

    result = []
    count = 0
    for row in query_job:
        row = dict(row[0])
        result.append(str(row["ip"]))
        count += 1

    logging.info(f"Query successful. Record returned: {count}")
    logging.info(result)

    return result, 200


# REST endpoint resource class and schema
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate


class RsuGeoQuerySchema(Schema):
    geometry = fields.List(
        fields.List(fields.Float, required=True, validate=validate.Length(min=2)),
        required=False,
        validate=validate.Length(min=1),
    )
    vendor = fields.String(required=False)


class RsuGeoQuery(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def post(self):
        logging.debug("RsuGeoQuery POST requested")

        schema = RsuGeoQuerySchema()
        errors = schema.validate(request.args)
        if errors:
            logging.debug(errors)
            abort(400, str(errors))

        # Get arguments from request
        try:
            data = request.json
            logging.debug(data)
            organization = request.environ["organization"]
            pointList = data["geometry"]
            vendor = data["vendor"] if data["vendor"] != "Select Vendor" else None
        except:
            logging.debug("failed to parse request")
            return ('Body format: {"geometry": coordinate list}', 400, self.headers)

        ipList = query_org_rsus(organization)
        if ipList:
            data, code = query_rsu_devices(ipList, pointList, vendor)
            return (data, code, self.headers)
