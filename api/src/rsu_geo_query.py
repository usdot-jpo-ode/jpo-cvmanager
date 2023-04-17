import pgquery
import util
import os
import logging

def query_org_rsus(orgName):

  query = "SELECT mi.ipv4_address " \
          "FROM public.map_info AS mi " \
          "JOIN public.rsus AS rd ON rd.ipv4_address = mi.ipv4_address " \
          "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
          f"WHERE ron_v.name = '{orgName}'"

  logging.info(query)
  data = pgquery.query_db(query)
  
  result = set()
  for row in data:
    device_ip = str(row[0]).replace("'", "")
    result.add(device_ip)
  
  logging.info(f"Successfully Query for query_org_rsus")

  return result, 200

def query_rsu_devices(ipList, pointList):
  geogString = "POLYGON(("
  for elem in pointList:
    long = str(elem.pop(0))
    lat = str(elem.pop(0))
    geogString += (long + " " + lat + ",")

  geogString = geogString[:-1] + "))"
  ipList = ", ".join(ipList)
  query = "SELECT ipv4_address as Ip, " \
          f"ST_X(geography::geometry) AS long, " \
          f"ST_Y(geography::geometry) AS lat " \
          f"FROM rsus " \
          f"WHERE ipv4_address = ANY('{{{ipList}}}'::inet[]) " \
          f"AND ST_Contains(ST_SetSRID(ST_GeomFromText(\'{geogString}\'), 4326), rsus.geography::geometry)"

  logging.info(f"Running query_rsu_devices")

  query_job = pgquery.query_db(query)

  result = []
  count = 0
  for row in query_job:
    result.append(str(row["ip"]))
    count += 1

  logging.info(f"Query successful. Record returned: {count}")
  logging.info(result)

  return result, 200

# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource
from marshmallow import Schema, fields

class RsuGeoQuerySchema(Schema):
  geometry = fields.String(required=False)
  start = fields.DateTime(required=False)
  end = fields.DateTime(required=False)

class RsuGeoQuery(Resource):
  options_headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization,Organization',
    'Access-Control-Allow-Methods': 'POST',
    'Access-Control-Max-Age': '3600'
  }

  headers = {
    'Access-Control-Allow-Origin': '*',
    'Content-Type': 'application/json'
  }

  def options(self):
    # CORS support
    return ('', 204, self.options_headers)

  def post(self):
    logging.debug("RsuGeoQuery POST requested")
    
    # Get arguments from request
    try:
      data = request.json
      logging.debug(data)
      organization = request.environ['organization']
      pointList = data['geometry']
    except:
      logging.debug("failed to parse request")
      return ('Body format: {"geometry": coordinate list}', 400, self.headers)
    
    ipList, code = query_org_rsus(organization)
    if (code == 200):
      data, code = query_rsu_devices(ipList, pointList)
      return (data, code, self.headers)