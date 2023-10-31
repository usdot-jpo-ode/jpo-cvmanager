from marshmallow import Schema, fields
import requests
import os
import logging

def reboot(request):
  rest_endpoint = os.environ["RSU_REST_ENDPOINT"]
  logging.info(f'Running command, POST reboot')
  try:
    body = {
      "rsu_ip": request["rsu_ip"],
      "username": request["creds"]["username"],
      "password": request["creds"]["password"]
    }
    endpoint = f'http://{rest_endpoint}/reboot'
    logging.info(f'Hitting the endpoint: {endpoint}')
    r = requests.post(endpoint, body)
    return r.json(), r.status_code
  except Exception as e:
    logging.exception(f"Encountered an error: {e}")
    return "Encountered an error with the command reboot", 500

# Command to modify SNMP Message Forwarding configuration files on Commsignia RSUs
# Forwards the work to the K8s RSU Automation API to perform modifications over SSH
def snmpfilter(request):
  rest_endpoint = os.environ["RSU_REST_ENDPOINT"]
  logging.info(f'Running command, POST snmpfilter')
  try:
    if request["manufacturer"] != "Commsignia":
      logging.info(f'snmpfilter is not supported for RSUs of type {request["manufacturer"]}')
      return "Target RSU is not of type Commsignia", 400

    body = {
      "rsu_ip": request["rsu_ip"],
      "username": request["creds"]["username"],
      "password": request["creds"]["password"]
    }
    endpoint = f'http://{rest_endpoint}/snmpfilter'
    logging.info(f'Hitting the endpoint: {endpoint}')
    r = requests.post(endpoint, body)
    return r.json(), r.status_code
  except Exception as e:
    logging.exception(f"Encountered an error: {e}")
    return "Encountered an error with the command snmpfilter", 500

class OSUpdate(Schema):
  manufacturer = fields.Str(required=True)
  model = fields.Str(required=True)
  update_type = fields.Str(required=True)
  update_name = fields.Int(required=True)
  image_name = fields.Str(required=True)
  bash_script = fields.Str(required=True)
  rescue_name = fields.Str(required=False)
  rescue_bash_script = fields.Str(required=False)

def osupdate(request):
  rest_endpoint = os.environ["RSU_REST_ENDPOINT"]
  logging.info(f'Running command, POST osupdate')
  # Check if the args match what is needed for the snmpset command
  schema = OSUpdate()
  errors = schema.validate(request["args"])
  if errors:
    return f"The provided args does not match required values: {str(errors)}", 400
  
  try:
    body = {
      "rsu_ip": request["rsu_ip"],
      "username": request["creds"]["username"],
      "password": request["creds"]["password"],
      "manufacturer": request["args"]["manufacturer"],
      "model": request["args"]["model"],
      "update_type": request["args"]["update_type"],
      "update_name": request["args"]["update_name"],
      "image_name": request["args"]["image_name"],
      "bash_script": request["args"]["bash_script"]
    }
    if "rescue_name" in request["args"]:
      body["rescue_name"] = request["args"]["rescue_name"]
      body["rescue_bash_script"] = request["args"]["rescue_bash_script"]
    
    endpoint = f'http://{rest_endpoint}/osupdate'
    logging.info(f'Hitting the endpoint: {endpoint}')
    r = requests.post(endpoint, body)
    return r.json(), r.status_code
  except Exception as e:
    logging.exception(f"Encountered an error: {e}")
    return "Encountered an error with the command osupdate", 500

class FWUpdate(Schema):
  manufacturer = fields.Str(required=True)
  model = fields.Str(required=True)
  update_type = fields.Str(required=True)
  update_name = fields.Int(required=True)
  image_name = fields.Str(required=True)
  bash_script = fields.Str(required=True)

def fwupdate(request):
  rest_endpoint = os.environ["RSU_REST_ENDPOINT"]
  logging.info(f'Running command, POST fwupdate')
  # Check if the args match what is needed for the snmpset command
  schema = FWUpdate()
  errors = schema.validate(request["args"])
  if errors:
    return f"The provided args does not match required values: {str(errors)}", 400
  
  try:
    body = {
      "rsu_ip": request["rsu_ip"],
      "username": request["creds"]["username"],
      "password": request["creds"]["password"],
      "manufacturer": request["args"]["manufacturer"],
      "model": request["args"]["model"],
      "update_type": request["args"]["update_type"],
      "update_name": request["args"]["update_name"],
      "image_name": request["args"]["image_name"],
      "bash_script": request["args"]["bash_script"]
    }
    
    endpoint = f'http://{rest_endpoint}/fwupdate'
    logging.info(f'Hitting the endpoint: {endpoint}')
    r = requests.post(endpoint, body)
    return r.json(), r.status_code
  except Exception as e:
    logging.exception(f"Encountered an error: {e}")
    return "Encountered an error with the command fwupdate", 500