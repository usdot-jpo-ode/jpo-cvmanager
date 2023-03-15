import subprocess
import logging
import snmpcredential
import snmperrorcheck

def message_type(val):
  # Check for which J2735 PSID matches val
  # BSM  - 20
  # SPaT - 8002
  # MAP  - E0000017
  # SSM  - E0000015
  # SRM  - E0000016
  # Hex octets are spaced out in the output and are always 4 octets long
  if val == '\" \"' or val == '00 00 00 20':
    return 'BSM'
  elif val == '00 00 80 02' or val == '80 02':
    return 'SPaT'
  elif val == 'E0 00 00 17':
    return 'MAP'
  elif val == 'E0 00 00 15':
    return 'SSM'
  elif val == 'E0 00 00 16':
    return 'SRM'
  return 'Other'

# Little endian
def ip(val):
	hex = val.split()
	ipaddr = f'{str(int(hex[-4], 16))}.' \
					f'{str(int(hex[-3], 16))}.' \
					f'{str(int(hex[-2], 16))}.' \
					f'{str(int(hex[-1], 16))}'
	return ipaddr

def yunex_ip(val):
	# Yunex RSUs can display IPs in 2 forms:
	# As regular IPv4 address: "10.0.0.1"
	# As (weird) IPv6/IPv4 hybrid: "::ffff:10.0.0.1"
	# This supports both cases by first trimming the quotes
	trimmed_val = val[1:-1]
	if ':' in trimmed_val:
		trimmed_val = trimmed_val.split(':')[-1]
	return trimmed_val

def protocol(val):
	if val == '1':
		return 'TCP'
	elif val == '2':
		return 'UDP'
	return 'Other'

def fwdon(val):
	if val == '1':
		return 'On'
	return 'Off'

def active(val):
  # This value represents an active state
  # Currently 1 and 4 are supported
  # 1 - active
  # 4 - create (represents active to Commsignia models)
	if val == '1' or val == '4':
		return 'Enabled'
	return 'Disabled'

def toint(val):
	return int(val)

def startend(val):
	hex = val.split()
	year = str(int(hex[0] + hex[1], 16))
	month = str(int(hex[2], 16)) 
	month = month if len(month) == 2 else '0' + month
	day = str(int(hex[3], 16)) 
	day = day if len(day) == 2 else '0' + day
	hour = str(int(hex[4], 16)) 
	hour = hour if len(hour) == 2 else '0' + hour
	min = str(int(hex[5], 16)) 
	min = min if len(min) == 2 else '0' + min
	return f'{year}-{month}-{day} {hour}:{min}'

# SNMP property to string name and processing function
# Supports SNMP RSU 4.1 Spec and Yunex SNMP tables
prop_namevalue = {
  # These values are based off the RSU 4.1 Spec
	'iso.0.15628.4.1.7.1.2': ('Message Type', message_type),
	'iso.0.15628.4.1.7.1.3': ('IP', ip),
	'iso.0.15628.4.1.7.1.4': ('Port', toint),
	'iso.0.15628.4.1.7.1.5': ('Protocol', protocol),
	'iso.0.15628.4.1.7.1.6': ('RSSI', toint),
	'iso.0.15628.4.1.7.1.7': ('Frequency', toint),
	'iso.0.15628.4.1.7.1.8': ('Start DateTime', startend),
	'iso.0.15628.4.1.7.1.9': ('End DateTime', startend),
	'iso.0.15628.4.1.7.1.10': ('Forwarding', fwdon),
	'iso.0.15628.4.1.7.1.11': ('Config Active', active),
  # These values are based off the Yunex rsuReceivedMsgTable table
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.2': ('Message Type', message_type),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.3': ('IP', yunex_ip),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.4': ('Port', toint),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.5': ('Protocol', protocol),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.6': ('RSSI', toint),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.7': ('Frequency', toint),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.8': ('Start DateTime', startend),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.9': ('End DateTime', startend),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.10': ('Config Active', active),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.11': ('Full WSMP', active),
  'iso.3.6.1.4.1.1206.4.2.18.5.2.1.12': ('Yunex Filter', active),
  # These values are based off the Yunex rsuXmitMsgFwdingTable table
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.2': ('Message Type', message_type),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.3': ('IP', yunex_ip),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.4': ('Port', toint),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.5': ('Protocol', protocol),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.6': ('Start DateTime', startend),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.7': ('End DateTime', startend),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.8': ('Full WSMP', active),
  'iso.3.6.1.4.1.1206.4.2.18.20.2.1.9': ('Config Active', active)
}

def snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip):
  # Create the SNMPWalk command based on the road
  cmd = 'snmpwalk -v 3 {auth} {rsuip} 1.0.15628.4.1.7'.format(auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip)
  output = ''
  try:
    # Example console output of a single configuration for message forwarding
    # iso.0.15628.4.1.7.1.2.1 = STRING: " "    #BSM
    # iso.0.15628.4.1.7.1.3.1 = Hex-STRING: 00 00 00 00 00 00 00 00 00 00 FF FF 0A 01 01 03    #10.1.1.3
    # iso.0.15628.4.1.7.1.4.1 = INTEGER: 46800    #port
    # iso.0.15628.4.1.7.1.5.1 = INTEGER: 2    #UDP
    # iso.0.15628.4.1.7.1.6.1 = INTEGER: -100    #rssi
    # iso.0.15628.4.1.7.1.7.1 = INTEGER: 1    #Forward every message
    # iso.0.15628.4.1.7.1.8.1 = Hex-STRING: 07 B2 0C 1F 11 00    # 1970-12-31 17:00
    # iso.0.15628.4.1.7.1.9.1 = Hex-STRING: 07 F4 0C 1F 11 00    # 2036-12-31 17:00
    # iso.0.15628.4.1.7.1.10.1 = INTEGER: 1    # turn this configuration on
    # iso.0.15628.4.1.7.1.11.1 = INTEGER: 1    # activate this index
    logging.info(f'Running snmpwalk: {cmd}')
    output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
    output = output.stdout.decode("utf-8").split('\n')[:-1]
  except subprocess.CalledProcessError as e:
    output = e.stderr.decode("utf-8").split('\n')[:-1]
    logging.error(f'Encountered error while running snmpwalk: {output[-1]}')
    err_message = snmperrorcheck.check_error_type(output[-1])
    return {"RsuFwdSnmpwalk": err_message}, 500

  # Placeholder for possible other failed scenarios
  # A proper message forwarding configuration will be exactly 10 lines of output.
  # Any RSU with an output of less than 10 can be assumed to be an RSU with 
  # no message forwarding configurations, or that some form error occurred in 
  # reading an RSU's SNMP configuration data. In either scenario, simply returning an 
  # empty response will suffice for the first implementation.
  if len(output) < 10:
    return {"RsuFwdSnmpwalk": {}}, 200

  snmp_config = {}

  # Parse each line of the output to build out readable SNMP configurations
  for line in output:
    # split configuration line into a property and value
    prop, raw_value = line.strip().split(' = ')
    # grab the configuration substring value for the property id while removing the index value
    prop_substr = prop[:-(len(prop.split('.')[-1])+1)]
    # grab the index value for the config
    key = prop.split('.')[-1]

    # If the index value already exists in the dict, ensure to add the new configuration value to it to build out a full SNMP configuration
    config = snmp_config[key] if key in snmp_config else {}
    # Assign the processed value of the the property to the readable property value and store the info based on the index value
    # The value is processed based on the type of property it is
    # The readable property name is based on the property
    config[prop_namevalue[prop_substr][0]] = prop_namevalue[prop_substr][1](raw_value.split(': ')[1])
    snmp_config[key] = config
  
  return { "RsuFwdSnmpwalk": snmp_config }, 200

def snmpwalk_yunex(snmp_creds, rsu_ip):
  snmpwalk_results = {
    'rsuReceivedMsgTable': {},
    'rsuXmitMsgFwdingTable': {}
  }
  # Start with rsuReceivedMsgTable
  output = ''
  try:
    # Create the SNMPWalk command based on the road
    cmd = 'snmpwalk -v 3 {auth} {rsuip} 1.3.6.1.4.1.1206.4.2.18.5.2.1'.format(auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip)

    # Example console output of a single configuration for rsuReceivedMsgTable
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.2.1 = STRING: " "    #BSM
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.3.1 = STRING: "10.235.1.36" #destination ip
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.4.1 = INTEGER: 46800    #port
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.5.1 = INTEGER: 2    #UDP
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.6.1 = INTEGER: -100    #rssi
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.7.1 = INTEGER: 1    #Forward every message
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.8.1 = Hex-STRING: 07 E6 01 01 00 00 00 00    # 2022-01-01 00:00:00.00
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.9.1 = Hex-STRING: 07 E8 09 01 00 00 00 00    # 2024-01-01 00:00:00.00
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.10.1 = INTEGER: 1    # turn this configuration on
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.11.1 = INTEGER: 0    # 0 - Only forward payload. 1 - Forward entire WSMP message.
    # iso.3.6.1.4.1.1206.4.2.18.5.2.1.12.1 = INTEGER: 0    # Yunex feature. 0 means off. Only 0 is supported.
    logging.info(f'Running snmpwalk: {cmd}')
    output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
    output = output.stdout.decode("utf-8").split('\n')[:-1]
  except subprocess.CalledProcessError as e:
    output = e.stderr.decode("utf-8").split('\n')[:-1]
    logging.error(f'Encountered error while running snmpwalk: {output[-1]}')
    err_message = snmperrorcheck.check_error_type(output[-1])
    return {"RsuFwdSnmpwalk": err_message}, 500

  # Placeholder for possible other failed scenarios
  # A proper rsuReceivedMsgTable configuration will be exactly 11 lines of output.
  # Any RSU with an output of less than 11 can be assumed to be an RSU with 
  # no rsuReceivedMsgTable configurations, or that some form error occurred in 
  # reading an RSU's SNMP configuration data. In either scenario, simply returning an 
  # empty response will suffice for the first implementation.
  if len(output) >= 11:
    snmp_config = {}

    # Parse each line of the output to build out readable SNMP configurations
    for line in output:
      # split configuration line into a property and value
      prop, raw_value = line.strip().split(' = ')
      # grab the configuration substring value for the property id while removing the index value
      prop_substr = prop[:-(len(prop.split('.')[-1])+1)]
      # grab the index value for the config
      key = prop.split('.')[-1]

      # If the index value already exists in the dict, ensure to add the new configuration value to it to build out a full SNMP configuration
      config = snmp_config[key] if key in snmp_config else {}
      # Assign the processed value of the the property to the readable property value and store the info based on the index value
      # The value is processed based on the type of property it is
      # The readable property name is based on the property
      config[prop_namevalue[prop_substr][0]] = prop_namevalue[prop_substr][1](raw_value.split(': ')[1])
      snmp_config[key] = config
    
    snmpwalk_results['rsuReceivedMsgTable'] = snmp_config
  

  # Second, check rsuXmitMsgFwdingTable
  output = ''
  try:
    # Create the SNMPWalk command based on the road
    cmd = 'snmpwalk -v 3 {auth} {rsuip} 1.3.6.1.4.1.1206.4.2.18.20.2.1'.format(auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip)

    # Example console output of a single configuration for rsuXmitMsgFwdingTable
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.2.1 = Hex-STRING: 80 02    #SPaT
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.3.1 = STRING: "10.235.1.36" #destination ip
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.4.1 = INTEGER: 46800    #port
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.5.1 = INTEGER: 2    #UDP
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.6.1 = Hex-STRING: 07 E6 01 01 00 00 00 00    # 2022-01-01 00:00:00.00
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.7.1 = Hex-STRING: 07 E8 09 01 00 00 00 00    # 2024-01-01 00:00:00.00
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.8.1 = INTEGER: 0    # 0 - Only forward payload. 1 - Forward entire WSMP message.
    # iso.3.6.1.4.1.1206.4.2.18.20.2.1.9.1 = INTEGER: 1    # turn this configuration on
    logging.info(f'Running snmpwalk: {cmd}')
    output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
    output = output.stdout.decode("utf-8").split('\n')[:-1]
  except subprocess.CalledProcessError as e:
    output = e.stderr.decode("utf-8").split('\n')[:-1]
    logging.error(f'Encountered error while running snmpwalk: {output[-1]}')
    err_message = snmperrorcheck.check_error_type(output[-1])
    return {"RsuFwdSnmpwalk": err_message}, 500

  # Placeholder for possible other failed scenarios
  # A proper rsuXmitMsgFwdingTable configuration will be exactly 8 lines of output.
  # Any RSU with an output of less than 8 can be assumed to be an RSU with 
  # no rsuXmitMsgFwdingTable configurations, or that some form error occurred in 
  # reading an RSU's SNMP configuration data. In either scenario, simply returning an 
  # empty response will suffice for the first implementation.
  if len(output) >= 8:
    snmp_config = {}

    # Parse each line of the output to build out readable SNMP configurations
    for line in output:
      # split configuration line into a property and value
      prop, raw_value = line.strip().split(' = ')
      # grab the configuration substring value for the property id while removing the index value
      prop_substr = prop[:-(len(prop.split('.')[-1])+1)]
      # grab the index value for the config
      key = prop.split('.')[-1]

      # If the index value already exists in the dict, ensure to add the new configuration value to it to build out a full SNMP configuration
      config = snmp_config[key] if key in snmp_config else {}
      # Assign the processed value of the the property to the readable property value and store the info based on the index value
      # The value is processed based on the type of property it is
      # The readable property name is based on the property
      config[prop_namevalue[prop_substr][0]] = prop_namevalue[prop_substr][1](raw_value.split(': ')[1])
      snmp_config[key] = config
    
    snmpwalk_results['rsuXmitMsgFwdingTable'] = snmp_config
  
  return { "RsuFwdSnmpwalk": snmpwalk_results }, 200

def get(request):
  logging.info(f'Running command, GET rsuFwdSnmpwalk')

  if request['manufacturer'] == 'Kapsch' or request['manufacturer'] == 'Commsignia':
    return snmpwalk_rsudsrcfwd(request['snmp_creds'], request["rsu_ip"])
  elif request['manufacturer'] == 'Yunex':
    return snmpwalk_yunex(request['snmp_creds'], request["rsu_ip"])
  else:
    return "Supported RSU manufacturers are currently only Commsignia, Kapsch and Yunex", 501