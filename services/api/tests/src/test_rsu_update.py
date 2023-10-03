from unittest.mock import patch
from api.src import rsu_update

@patch('api.src.rsu_update.pgquery')
def test_check_for_updates(mock_pgquery):
    # mock query_db
    mock_pgquery.query_db.return_value = [
        {
            'new_os_name': 'new_os_name',
            'new_fw_name': 'new_fw_name'
        }
    ]

    #call function
    rsu_ip = '192.168.0.10'
    rsu_update.check_for_updates(rsu_ip)

    # assert query_db was called with correct query
    expected_query = "SELECT rd.ipv4_address, rd.os_version, rd.firmware_version, os_upgrade.name AS new_os_name, fw_upgrade.name AS new_fw_name FROM public.rsus AS rd LEFT JOIN (SELECT * FROM public.os_upgrade_rules AS osur JOIN public.os_images AS osi ON osur.to_id = osi.os_id) AS os_upgrade ON rd.os_version = os_upgrade.from_id LEFT JOIN (SELECT fwur.from_id, fwi.name FROM public.firmware_upgrade_rules AS fwur JOIN public.firmware_images AS fwi ON fwur.to_id = fwi.firmware_id) AS fw_upgrade ON rd.firmware_version = fw_upgrade.from_id WHERE rd.ipv4_address = '192.168.0.10'"
    mock_pgquery.query_db.assert_called_with(expected_query)

@patch('api.src.rsu_update.pgquery')
def test_get_os_update_info(mock_pgquery):
    # mock query_db
    mock_pgquery.query_db.return_value = [
        {
            'name': 'new_os_name',
            'update_image': 'new_os_image',
            'install_script': 'new_os_install_script',
            'rescue_image': 'new_os_rescue_image',
            'rescue_install_script': 'new_os_rescue_install_script',
            'model_name': 'new_os_model_name',
            'manufacturer_name': 'new_os_manufacturer_name'
        }
    ]

    #call function
    rsu_ip = '192.168.0.10'
    rsu_update.get_os_update_info(rsu_ip)

    # assert query_db was called with correct query
    expected_query = "SELECT osi.name, osi.update_image, osi.install_script, osi.rescue_image, osi.rescue_install_script, rd.ipv4_address, rd.firmware_version, md.model_name, md.manufacturer_name FROM public.rsus AS rd JOIN (SELECT rm.rsu_model_id, rm.name AS model_name, m.name AS manufacturer_name FROM public.rsu_models AS rm JOIN public.manufacturers AS m ON rm.manufacturer = m.manufacturer_id) AS md ON rd.model = md.rsu_model_id JOIN public.os_upgrade_rules AS osur ON osur.from_id = rd.os_version JOIN public.os_images AS osi ON osur.to_id = osi.os_id WHERE rd.ipv4_address = '192.168.0.10'"
    mock_pgquery.query_db.assert_called_with(expected_query)

@patch('api.src.rsu_update.pgquery')
def test_get_firmware_update_info(mock_pgquery):
    # mock query_db
    mock_pgquery.query_db.return_value = [
        {
            "name": 'new_fw_name',
            "update_image": 'new_fw_image',
            'bash_script': 'new_fw_bash_script',
            'rescue_image': 'new_fw_rescue_image',
            'rescue_bash_script': 'new_fw_rescue_bash_script',
            'model_name': 'new_fw_model_name',
            'manufacturer_name': 'new_fw_manufacturer_name',
            'install_script': 'new_fw_install_script',
        }
    ]

    #call function
    rsu_ip = '192.168.0.10'
    rsu_update.get_firmware_update_info(rsu_ip)

    # assert query_db was called with correct query
    expected_query = "SELECT fwi.name, fwi.update_image, fwi.install_script, rd.firmware_version, md.model_name, md.manufacturer_name FROM public.rsus AS rd JOIN (SELECT rm.rsu_model_id, rm.name AS model_name, m.name AS manufacturer_name FROM public.rsu_models AS rm JOIN public.manufacturers AS m ON rm.manufacturer = m.manufacturer_id) AS md ON rd.model = md.rsu_model_id JOIN public.firmware_upgrade_rules AS fwur ON fwur.from_id = rd.firmware_version JOIN public.firmware_images AS fwi ON fwur.to_id = fwi.firmware_id WHERE rd.ipv4_address = '192.168.0.10'"
    mock_pgquery.query_db.assert_called_with(expected_query)