from marshmallow import Schema, fields
import pgquery
import logging
import rsu_update
import rsufwdsnmpwalk
import rsufwdsnmpset
import ssh_commands

# Dict of functions
command_data = {
  'rsufwdsnmpwalk': {
    'function': rsufwdsnmpwalk.get,
    'roles': ['user', 'operator', 'admin'],
    'ssh_required': False,
    'snmp_required': True
  },
  'rsufwdsnmpset': {
    'function': rsufwdsnmpset.post,
    'roles': ['operator', 'admin'],
    'ssh_required': True,
    'snmp_required': True
  },
  'rsufwdsnmpset-del': {
    'function': rsufwdsnmpset.delete,
    'roles': ['operator', 'admin'],
    'ssh_required': False,
    'snmp_required': True
  },
  'reboot': {
    'function': ssh_commands.reboot,
    'roles': ['admin'],
    'ssh_required': True,
    'snmp_required': False
  },
  'snmpfilter': {
    'function': ssh_commands.snmpfilter,
    'roles': ['operator', 'admin'],
    'ssh_required': True,
    'snmp_required': False
  },
  'checkforupdates': {
    'function': None,
    'roles': ['admin'],
    'ssh_required': False,
    'snmp_required': False
  },
  'osupdate': {
    'function': ssh_commands.osupdate,
    'roles': ['admin'],
    'ssh_required': True,
    'snmp_required': False
  },
  'fwupdate': {
    'function': ssh_commands.fwupdate,
    'roles': ['admin'],
    'ssh_required': True,
    'snmp_required': False
  }
}

# Performs the RSU command based on the requested command
def execute_command(command, rsu_ip, args, rsu_info):
  logging.info(f"Executing command {command} on RSU {rsu_ip}")
  request_data = {
    "command": command,
    "manufacturer": rsu_info["manufacturer"],
    "rsu_ip": rsu_ip,
    "args": args
  }

  if command_data[command]['ssh_required']:
    request_data["creds"] = {"username": rsu_info["ssh_username"], "password": rsu_info["ssh_password"]}
  
  if command_data[command]['snmp_required']:
    request_data["snmp_creds"] = {"username": rsu_info["snmp_username"], "password": rsu_info["snmp_password"]}
  
  logging.debug(f"Request data: {str(request_data)}")
  return command_data[command]['function'](request_data)

# Queries for RSU manufacturer, SSH credentials, and SNMP credentials using a provided RSU IP address
def fetch_rsu_info(rsu_ip, organization):
  logging.info(f"Fetching RSU info for RSU {rsu_ip}")
  query = "SELECT rd.ipv4_address, man.name AS manufacturer_name, rcred.username AS ssh_username, rcred.password AS ssh_password, snmp.username AS snmp_username, snmp.password AS snmp_password " \
          "FROM public.rsus AS rd " \
          "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
          "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rd.model " \
          "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer " \
          "LEFT JOIN public.rsu_credentials AS rcred ON rcred.credential_id = rd.credential_id " \
          "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id " \
          f"WHERE ron_v.name = '{organization}' AND rd.ipv4_address = '{rsu_ip}'"
  data = pgquery.query_db(query)
  logging.info('Parsing results...')
  if len(data) > 0:
    manufacturer = data[0]["manufacturer_name"]
    ssh_user = data[0]["ssh_username"]
    ssh_pw = data[0]["ssh_password"]
    snmp_user = data[0]["snmp_username"]
    snmp_pw = data[0]["snmp_password"]
    
    rsu_info = {
      "manufacturer": manufacturer,
      "ssh_username": ssh_user,
      "ssh_password": ssh_pw,
      "snmp_username": snmp_user,
      "snmp_password": snmp_pw
    }

    return rsu_info
  
  logging.warning(f'RSU info cannot be found for {rsu_ip}')
  return None

# Returns the appropriate snmp_walk index given add/del command
def fetch_index(command, rsu_ip, rsu_info, message_type=None, target_ip=None):
  index = -1
  data, code = execute_command('rsufwdsnmpwalk', rsu_ip, {}, rsu_info)
  if code == 200:
    walkResult = {}
    if rsu_info["manufacturer"] == "Yunex":
      if message_type.upper() == 'BSM' or message_type.upper() == 'SSM':
        walkResult = data['RsuFwdSnmpwalk']['rsuReceivedMsgTable']
      else:
        walkResult = data['RsuFwdSnmpwalk']['rsuXmitMsgFwdingTable']
    else:
      walkResult = data['RsuFwdSnmpwalk']
    # finds the next available index
    if command == 'add':
      if rsu_info["manufacturer"] == "Yunex":
        index = 0
      for entry in walkResult:
        if (int(entry) > index):
          index = int(entry)
      index += 1

    # grabs the highest index matching the message type and target ip
    if command == 'del' and message_type != None and target_ip != None:
      for entry in walkResult:
        if (int(entry) > index and walkResult[entry]['Message Type'].upper() == message_type.upper()
            and walkResult[entry]['IP'] == target_ip):
            index = int(entry)
  return index

# Main driver function
def perform_command(command, organization, role, rsu_ip, args):
  # Check if command is a known command
  if command not in command_data:
    return f"Command unknown: {command}", 400
  # Check if the user is authorized to run the command
  if role in command_data[command]['roles']:
    # add message forwarding configuration at the next available rsu index
    if command == 'rsufwdsnmpset':
      return_dict = {}
      for rsu in rsu_ip:
        rsu_info = fetch_rsu_info(rsu, organization)
        if rsu_info is None:
          return_dict[rsu] = {'code': 400, 'data': f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu}"}
        else:
          index = fetch_index('add', rsu, rsu_info, args['msg_type'])
          if index != -1:
            args['rsu_index'] = index
            data, code = execute_command(command, rsu, args, rsu_info)
            return_dict[rsu] = {'code': code, 'data': data}
          else:
            return_dict[rsu] = {'code': 400, 'data': f"Invalid index for RSU: {rsu}"}
      return return_dict, 200

    if command == 'rsufwdsnmpset-del':
      return_dict = {}
      dest_ip = args['dest_ip']
      del args['dest_ip']
      for rsu in rsu_ip:
        rsu_info = fetch_rsu_info(rsu, organization)
        if rsu_info is None:
          return_dict[rsu] = {'code': 400, 'data': f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu}"}
        else:
          index = fetch_index('del', rsu, rsu_info, args['msg_type'], dest_ip)
          if index != -1:
            args['rsu_index'] = index
            data, code = execute_command(command, rsu, args, rsu_info)
            return_dict[rsu] = {'code': code, 'data': data}
          else:
            return_dict[rsu] = {'code': 400, 'data': f"Delete index invalid for RSU: {rsu}"}

      return return_dict, 200

    # Get the basic target RSU info
    rsu_info = fetch_rsu_info(rsu_ip[0], organization)
    if rsu_info is None:
      return f"Provided RSU IP does not have complete RSU data for organization: {organization}::{rsu_ip}", 500

    # If command is for checkforupdates, handle here
    if command == 'checkforupdates':
      return rsu_update.check_for_updates(rsu_ip), 200

    # If command is RSU update related, gather additional info on the target RSU
    if command == 'osupdate':
      info = rsu_update.get_os_update_info(rsu_ip)
      if info == None:
        return f"RSU {rsu_ip} cannot update its OS version", 500
      args = info
    elif command == 'fwupdate':
      info = rsu_update.get_firmware_update_info(rsu_ip)
      if info == None:
        return f"RSU {rsu_ip} cannot update its firmware version", 500
      args = info

    return execute_command(command, rsu_ip[0], args, rsu_info)
  else:
    return f"Unauthorized role to run {command}", 401


# REST endpoint resource class and schema
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields

class RsuCommandRequestSchema(Schema):
  command = fields.Str(required=True)
  rsu_ip = fields.List(fields.IPv4(required=True))
  args = fields.Dict(required=True)

class RsuCommandRequest(Resource):
  options_headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization,Organization',
    'Access-Control-Allow-Methods': 'GET,POST',
    'Access-Control-Max-Age': '3600'
  }

  headers = {
    'Access-Control-Allow-Origin': '*'
  }

  def options(self):
    # CORS support
    return ('', 204, self.options_headers)
  
  def get(self):
    logging.debug("RsuCommandRequest GET requested")
    print("in get")
    return self.universal()
  
  def post(self):
    logging.debug("RsuCommandRequest POST requested")
    return self.universal()

  def universal(self):
    print("in universal")
    schema = RsuCommandRequestSchema()
    print("after schema",request.json)
    print("enviorn",request.environ)
    errors = schema.validate(request.json)
    print("errors")
    if errors:
      logging.error(str(errors))
      abort(400, str(errors))
    
    data, code = perform_command(
      request.json["command"],
      request.environ['organization'],
      request.environ['role'],
      request.json["rsu_ip"],
      request.json["args"]
      )
    return (data, code, self.headers)