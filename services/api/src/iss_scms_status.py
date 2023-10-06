import logging
import common.pgquery as pgquery
import util
import os

def get_iss_scms_status(organization):
  # Execute the query and fetch all results
  query = "SELECT jsonb_build_object('ip', rd.ipv4_address, 'health', scms_health_data.health, 'expiration', scms_health_data.expiration) " \
          "FROM public.rsus AS rd " \
          "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
          "LEFT JOIN (" \
            "SELECT a.rsu_id, a.health, a.timestamp, a.expiration " \
            "FROM (" \
              "SELECT sh.rsu_id, sh.health, sh.timestamp, sh.expiration, ROW_NUMBER() OVER (PARTITION BY sh.rsu_id order by sh.timestamp DESC) AS row_id " \
              "FROM public.scms_health AS sh" \
            ") AS a " \
            "WHERE a.row_id <= 1 ORDER BY rsu_id" \
          ") AS scms_health_data ON rd.rsu_id = scms_health_data.rsu_id " \
          f"WHERE ron_v.name = '{organization}' " \
          "ORDER BY rd.ipv4_address"

  logging.debug(f'Executing query "{query};"')
  data = pgquery.query_db(query)

  logging.info('Parsing results...')
  result = {}
  for point in data:
    point_dict = dict(point[0])
    result[point_dict['ip']] = {
        'health': point_dict['health'],
        'expiration': util.format_date_denver(point_dict['expiration'])
      } if point_dict['health'] else None
  return result

# REST endpoint resource class
from flask import request
from flask_restful import Resource

class IssScmsStatus(Resource):
  options_headers = {
    'Access-Control-Allow-Origin': os.environ["CORS_DOMAIN"],
    'Access-Control-Allow-Headers': 'Content-Type,Authorization,Organization',
    'Access-Control-Allow-Methods': 'GET',
    'Access-Control-Max-Age': '3600'
  }

  headers = {
    'Access-Control-Allow-Origin': os.environ["CORS_DOMAIN"],
    'Content-Type': 'application/json'
  }

  def options(self):
    # CORS support
    return ('', 204, self.options_headers)
  
  def get(self):
    logging.debug("IssScmsStatus GET requested")
    return (get_iss_scms_status(request.environ['organization']), 200, self.headers)