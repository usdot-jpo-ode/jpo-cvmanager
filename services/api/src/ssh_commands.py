from marshmallow import Schema, fields
from fabric import Connection, Config
from google.cloud import storage
import time
import os
import logging

def reboot(request):
  logging.info(f'Running command, POST reboot')
  rsu_ip = request["rsu_ip"]
  username = request["creds"]["username"]
  password = request["creds"]["password"]

  # Build configurations for ssh and sudo
  fabric_config = Config(overrides={'sudo': {'password': password}})
  kwargs = {'password': password}
  try:
    with Connection(rsu_ip, user=username, config=fabric_config, connect_kwargs=kwargs) as conn:
      conn.sudo('reboot')
  except Exception as e:
    # When rebooting, the connection will be dropped from the RSU's end
    # Ignore the error when this happens
    if e != '__enter__':
      logging.error(f"Encountered an error: {e}")
      return "failed", 500
  
  logging.info(f"Reboot successful")
  return "succeeded", 200

# Command to modify SNMP Message Forwarding configuration files on Commsignia RSUs
def snmpfilter(request):
  logging.info(f'Running command, POST snmpfilter')

  # First check if target RSU is not a Commsignia RSU
  if request["manufacturer"] != "Commsignia":
    logging.info(f'snmpfilter is not supported for RSUs of type {request["manufacturer"]}')
    return "Target RSU is not of type Commsignia", 400

  rsu_ip = request["rsu_ip"]
  username = request["creds"]["username"]
  password = request["creds"]["password"]

  kwargs = {'password': password}
  try:
    with Connection(rsu_ip, user=username, connect_kwargs=kwargs) as conn:
      fwd_configs = []
      # Get all MAP files
      output = conn.run('grep -rwl /rwdata/etc/data_logger_ftw -e \'"value":3758096407\'', warn=True, hide=True)
      fwd_configs = fwd_configs + output.stdout.split('\n')[:-1]

      # Get all SPaT files
      output = conn.run('grep -rwl /rwdata/etc/data_logger_ftw -e \'"value":32770\'', warn=True, hide=True)
      fwd_configs = fwd_configs + output.stdout.split('\n')[:-1]
      
      # Iterate through the data logger configs for MAP and SPaT and apply the filter for outgoing traffic only
      # The Unix command, sed, is used for inline text editing. It substitutes strings in place
      for config in fwd_configs:
        logging.info('Applying filter to ' + config)
        output = conn.run('sed -i \'s/"direction":"both"/"direction":"out"/g\' ' + config, hide=True)
      
      # Only if files were edited, reboot to force the RSU to apply the changes
      if len(fwd_configs) != 0:
        conn.run('reboot')
  except Exception as e:
    logging.error(f"Encountered an error: {e}")
    return "filter failed to be applied", 500
  
  return "filter applied successfully", 200

def download_blob(bucket_name, object_path, dest_path):
  storage_client = storage.Client()
  bucket = storage_client.bucket(bucket_name)
  blob = bucket.blob(object_path)
  blob.download_to_filename(dest_path)

class FWUpdate(Schema):
  manufacturer = fields.Str(required=True)
  model = fields.Str(required=True)
  update_type = fields.Str(required=True)
  update_name = fields.Int(required=True)
  image_name = fields.Str(required=True)
  bash_script = fields.Str(required=True)

def fwupdate(request):
  logging.info(f'Running command, POST fwupdate')
  # Check if the args match what is needed for the snmpset command
  schema = FWUpdate()
  errors = schema.validate(request["args"])
  if errors:
    return f"The provided args does not match required values: {str(errors)}", 400

  # Prep update files
  bucket_name = os.environ["RSU_CLOUD_STORAGE"]
  file_root = request['manufacturer'] + '/' + request['model'] + '/' + \
              request['update_type'] + '/' + request['update_name'] + '/'
  
  update_image_path = file_root + '/' + request['image_name']
  download_blob(bucket_name, update_image_path, '/home/' + request['image_name'])

  bash_script_path = file_root + '/' + request['bash_script']
  download_blob(bucket_name, bash_script_path, '/home/' + request['bash_script'])

  rsuip = request['rsu_ip']
  username = request['username']
  password = request['password']
  # Build configurations for ssh and sudo
  fabric_config = Config(overrides={'sudo': {'password': password}})
  kwargs = {'password': password}

  try:
    with Connection(rsuip, user=username, config=fabric_config, connect_kwargs=kwargs) as conn:
      conn.put('/home/' + request['image_name'], '/home/admin')
      conn.put('/home/' + request['bash_script'], '/home/admin')
      conn.run('chmod 777 /home/admin/' + request['bash_script'])
      conn.run('/home/admin/' + request['bash_script'])
  except Exception as e:
    # When rebooting a unit, the connection will be dropped from the RSU's end and throw an exception
    # Ignore the error when this happens (it will be a "__enter__" exception)
    os.remove('/home/' + request['image_name'])
    os.remove('/home/' + request['bash_script'])
    if e != '__enter__':
      logging.error(f"Encountered an error: {e}")
      return "failed", 500