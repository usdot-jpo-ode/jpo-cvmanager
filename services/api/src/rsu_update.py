import common.pgquery as pgquery
import logging

def check_for_updates(rsu_ip):
  available_updates = {
    'os': False,
    'firmware': False
  }

  logging.info(f'Querying for available RSU updates for {rsu_ip}')
  query = "SELECT rd.ipv4_address, rd.os_version, rd.firmware_version, os_upgrade.name AS new_os_name, fw_upgrade.name AS new_fw_name " \
          "FROM public.rsus AS rd " \
          "LEFT JOIN (" \
            "SELECT * FROM public.os_upgrade_rules AS osur " \
            "JOIN public.os_images AS osi ON osur.to_id = osi.os_id" \
          ") AS os_upgrade ON rd.os_version = os_upgrade.from_id " \
          "LEFT JOIN (" \
            "SELECT fwur.from_id, fwi.name FROM public.firmware_upgrade_rules AS fwur " \
            "JOIN public.firmware_images AS fwi ON fwur.to_id = fwi.firmware_id" \
          ") AS fw_upgrade ON rd.firmware_version = fw_upgrade.from_id " \
          f"WHERE rd.ipv4_address = '{rsu_ip}'"
  data = pgquery.query_db(query)

  if len(data) > 0:
    currentOS = data[0]["new_os_name"]
    currentFW = data[0]["new_fw_name"]
  else:
    logging.warning(f'RSU OS and firmware data is not available for {rsu_ip}')
    return available_updates
  
  if currentOS != None:
    available_updates['os'] = True
  
  if currentFW != None:
    available_updates['firmware'] = True

  return available_updates

def get_os_update_info(rsu_ip):
  update_info = {
    'manufacturer': None,
    'model': None,
    'update_type': 'OS',
    'update_name': None,
    'image_name': None,
    'bash_script': None,
    'rescue_name': None ,
    'rescue_bash_script': None
  }

  query = "SELECT osi.name, osi.update_image, osi.install_script, osi.rescue_image, osi.rescue_install_script, rd.ipv4_address, rd.firmware_version, md.model_name, md.manufacturer_name " \
          "FROM public.rsus AS rd " \
          "JOIN (" \
            "SELECT rm.rsu_model_id, rm.name AS model_name, m.name AS manufacturer_name " \
            "FROM public.rsu_models AS rm " \
            "JOIN public.manufacturers AS m ON rm.manufacturer = m.manufacturer_id" \
          ") AS md ON rd.model = md.rsu_model_id " \
          "JOIN public.os_upgrade_rules AS osur ON osur.from_id = rd.os_version " \
          "JOIN public.os_images AS osi ON osur.to_id = osi.os_id " \
          f"WHERE rd.ipv4_address = '{rsu_ip}'"
  data = pgquery.query_db(query)

  if len(data) > 0:
    update_info['update_name'] = data[0]["name"]
    update_info['image_name'] = data[0]["update_image"]
    update_info['bash_script'] = data[0]["install_script"]
    update_info['rescue_name'] = data[0]["rescue_image"]
    update_info['rescue_bash_script'] = data[0]["rescue_install_script"]
    update_info['model'] = data[0]["model_name"]
    update_info['manufacturer'] = data[0]["manufacturer_name"]

  return update_info

def get_firmware_update_info(rsu_ip):
  update_info = {
    'manufacturer': None,
    'model': None,
    'update_type': 'Firmware',
    'update_name': None,
    'image_name': None,
    'bash_script': None
  }

  query = "SELECT fwi.name, fwi.update_image, fwi.install_script, rd.firmware_version, md.model_name, md.manufacturer_name " \
          "FROM public.rsus AS rd " \
          "JOIN (" \
            "SELECT rm.rsu_model_id, rm.name AS model_name, m.name AS manufacturer_name " \
            "FROM public.rsu_models AS rm " \
            "JOIN public.manufacturers AS m ON rm.manufacturer = m.manufacturer_id" \
          ") AS md ON rd.model = md.rsu_model_id " \
          "JOIN public.firmware_upgrade_rules AS fwur ON fwur.from_id = rd.firmware_version " \
          "JOIN public.firmware_images AS fwi ON fwur.to_id = fwi.firmware_id " \
          f"WHERE rd.ipv4_address = '{rsu_ip}'"
  data = pgquery.query_db(query)

  if len(data) > 0:
    update_info['update_name'] = data[0]["name"]
    update_info['image_name'] = data[0]["update_image"]
    update_info['bash_script'] = data[0]["install_script"]
    update_info['model'] = data[0]["model_name"]
    update_info['manufacturer'] = data[0]["manufacturer_name"]

  return update_info