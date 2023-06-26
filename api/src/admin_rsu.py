import logging
import pgquery
import sqlalchemy
import admin_new_rsu

def get_rsu_data(rsu_ip):
  query = "SELECT ipv4_address, ST_X(geography::geometry) AS longitude, ST_Y(geography::geometry) AS latitude, " \
    "milepost, primary_route, serial_number, iss_scms_id, concat(man.name, ' ',rm.name) AS model, " \
    "rsu_cred.nickname AS ssh_credential, snmp_cred.nickname AS snmp_credential, snmp_ver.nickname AS snmp_version, org.name AS org_name " \
    "FROM public.rsus " \
    "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rsus.model " \
    "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer " \
    "JOIN public.rsu_credentials AS rsu_cred ON rsu_cred.credential_id = rsus.credential_id " \
    "JOIN public.snmp_credentials AS snmp_cred ON snmp_cred.snmp_credential_id = rsus.snmp_credential_id " \
    "JOIN public.snmp_versions AS snmp_ver ON snmp_ver.snmp_version_id = rsus.snmp_version_id " \
    "JOIN public.rsu_organization AS ro ON ro.rsu_id = rsus.rsu_id  " \
    "JOIN public.organizations AS org ON org.organization_id = ro.organization_id"
  if rsu_ip != "all":
    query += f" WHERE ipv4_address = '{rsu_ip}'"

  data = pgquery.query_db(query)

  rsu_dict = {}
  for point in data:
    if point['ipv4_address'] not in rsu_dict:
      rsu_dict[point['ipv4_address']] = {
        'ip': str(point['ipv4_address']),
        'geo_position': { 'latitude': point['latitude'], 'longitude': point['longitude'] },
        'milepost': point['milepost'],
        'primary_route': point['primary_route'],
        'serial_number': point['serial_number'],
        'model': point['model'],
        'scms_id': point['iss_scms_id'],
        'ssh_credential_group': point['ssh_credential'],
        'snmp_credential_group': point['snmp_credential'],
        'snmp_version_group': point['snmp_version'],
        'organizations': []
      }
    rsu_dict[point['ipv4_address']]['organizations'].append(point['org_name'])

  rsu_list = list(rsu_dict.values())
  # If list is empty and a single RSU was requested, return empty object
  if len(rsu_list) == 0 and rsu_ip != "all":
    return {}
  # If list is not empty and a single RSU was requested, return the first index of the list
  elif len(rsu_list) == 1 and rsu_ip != "all":
    return rsu_list[0]
  else:
    return rsu_list

def get_modify_rsu_data(rsu_ip):
  modify_rsu_obj = {}
  modify_rsu_obj['rsu_data'] = get_rsu_data(rsu_ip)
  if rsu_ip != "all":
    modify_rsu_obj['allowed_selections'] = admin_new_rsu.get_allowed_selections()
  return modify_rsu_obj

def modify_rsu(rsu_spec):
  # Check for special characters for potential SQL injection
  if not admin_new_rsu.check_safe_input(rsu_spec):
    return {"message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\]^`{|}~. No sequences of '-' characters are allowed"}, 500
  
  # Parse model out of the "Manufacturer Model" string
  space_index = rsu_spec['model'].find(' ')
  model = rsu_spec['model'][(space_index+1):]

  try:
    # Modify the existing RSU data
    query = "UPDATE public.rsus SET " \
          f"geography=ST_GeomFromText('POINT({str(rsu_spec['geo_position']['longitude'])} {str(rsu_spec['geo_position']['latitude'])})'), " \
          f"milepost={str(rsu_spec['milepost'])}, " \
          f"ipv4_address='{rsu_spec['ip']}', " \
          f"serial_number='{rsu_spec['serial_number']}', " \
          f"primary_route='{rsu_spec['primary_route']}', " \
          f"model=(SELECT rsu_model_id FROM public.rsu_models WHERE name = '{model}'), " \
          f"credential_id=(SELECT credential_id FROM public.rsu_credentials WHERE nickname = '{rsu_spec['ssh_credential_group']}'), " \
          f"snmp_credential_id=(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = '{rsu_spec['snmp_credential_group']}'), " \
          f"snmp_version_id=(SELECT snmp_version_id FROM public.snmp_versions WHERE nickname = '{rsu_spec['snmp_version_group']}'), " \
          f"iss_scms_id='{rsu_spec['scms_id']}' " \
          f"WHERE ipv4_address='{rsu_spec['orig_ip']}'"
    pgquery.insert_db(query)

    # Add the rsu-to-organization relationships for the organizations to add
    if len(rsu_spec['organizations_to_add']) > 0:
      org_add_query = "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES"
      for organization in rsu_spec['organizations_to_add']:
        org_add_query += " (" \
                    f"(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_spec['ip']}'), " \
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{organization}')" \
                    "),"
      org_add_query = org_add_query[:-1]
      pgquery.insert_db(org_add_query)

    # Remove the rsu-to-organization relationships for the organizations to remove
    for organization in rsu_spec['organizations_to_remove']:
      org_remove_query = "DELETE FROM public.rsu_organization WHERE " \
                  f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_spec['ip']}') " \
                  f"AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = '{organization}')"
      pgquery.insert_db(org_remove_query)
  except sqlalchemy.exc.IntegrityError as e:
    failed_value = e.orig.args[0]['D']
    failed_value = failed_value.replace('(', '"')
    failed_value = failed_value.replace(')', '"')
    failed_value = failed_value.replace('=', ' = ')
    logging.error(f"Exception encountered: {failed_value}")
    return {"message": failed_value}, 500
  except Exception as e:
    logging.error(f"Exception encountered: {e}")
    return {"message": "Encountered unknown issue"}, 500

  return {"message": "RSU successfully modified"}, 200

def delete_rsu(rsu_ip):
  # Delete RSU to Organization relationships
  org_remove_query = "DELETE FROM public.rsu_organization WHERE " \
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
  pgquery.insert_db(org_remove_query)

  # Delete recorded RSU ping data
  ping_remove_query = "DELETE FROM public.ping WHERE " \
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
  pgquery.insert_db(ping_remove_query)

  # Delete recorded RSU SCMS health data
  scms_remove_query = "DELETE FROM public.scms_health WHERE " \
        f"rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '{rsu_ip}')"
  pgquery.insert_db(scms_remove_query)

  # Delete RSU data
  rsu_remove_query = "DELETE FROM public.rsus WHERE " \
        f"ipv4_address = '{rsu_ip}'"
  pgquery.insert_db(rsu_remove_query)

  return {"message": "RSU successfully deleted"}

# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields

class AdminRsuGetAllSchema(Schema):
  rsu_ip = fields.Str(required=True)

class AdminRsuGetDeleteSchema(Schema):
  rsu_ip = fields.IPv4(required=True)

class GeoPositionSchema(Schema):
  latitude = fields.Decimal(required=True)
  longitude = fields.Decimal(required=True)

class AdminRsuPatchSchema(Schema):
  orig_ip = fields.IPv4(required=True)
  ip = fields.IPv4(required=True)
  geo_position = fields.Nested(GeoPositionSchema, required=True)
  milepost = fields.Decimal(required=True)
  primary_route = fields.Str(required=True)
  serial_number = fields.Str(required=True)
  model = fields.Str(required=True)
  scms_id = fields.Str(required=True)
  ssh_credential_group = fields.Str(required=True)
  snmp_credential_group = fields.Str(required=True)
  snmp_version_group = fields.Str(required=True)
  organizations_to_add = fields.List(fields.String(), required=True)
  organizations_to_remove = fields.List(fields.String(), required=True)

class AdminRsu(Resource):
  options_headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization',
    'Access-Control-Allow-Methods': 'GET,PATCH,DELETE',
    'Access-Control-Max-Age': '3600'
  }

  headers = {
    'Access-Control-Allow-Origin': '*',
    'Content-Type': 'application/json'
  }

  def options(self):
    # CORS support
    return ('', 204, self.options_headers)

  def get(self):
    logging.debug("AdminRsu GET requested")
    schema = AdminRsuGetAllSchema()
    errors = schema.validate(request.args)
    if errors:
      logging.error(errors)
      abort(400, errors)

    # If rsu_ip is "all", allow without checking for an IPv4 address
    if request.args['rsu_ip'] != "all":
      schema = AdminRsuGetDeleteSchema()
      errors = schema.validate(request.args)
      if errors:
        logging.error(errors)
        abort(400, errors)

    return (get_modify_rsu_data(request.args['rsu_ip']), 200, self.headers)

  def patch(self):
    logging.debug("AdminRsu PATCH requested")
    # Check for main body values
    schema = AdminRsuPatchSchema()
    errors = schema.validate(request.json)
    if errors:
      logging.error(str(errors))
      abort(400, str(errors))
    
    data, code = modify_rsu(request.json)
    return (data, code, self.headers)

  def delete(self):
    logging.debug("AdminRsu DELETE requested")
    schema = AdminRsuGetDeleteSchema()
    errors = schema.validate(request.args)
    if errors:
      logging.error(errors)
      abort(400, errors)
    
    return (delete_rsu(request.args['rsu_ip']), 200, self.headers)