import datetime
from unittest.mock import MagicMock, call, patch, Mock
from api.src import rsufwdsnmpset

# static values
rsu_ip = '192.168.0.20'
snmp_creds = {'username': 'test_username', 'password': 'test_password'}
dest_ip = '192.168.0.10'
rsu_index = 1

def test_ip_to_hex():
    ip = '192.168.0.10'
    expected = '00000000000000000000FFFFc0a8000a'
    assert rsufwdsnmpset.ip_to_hex(ip) == expected

def test_hex_datetime():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    expected = '07e401010101'
    assert rsufwdsnmpset.hex_datetime(dt) == expected

@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_set_rsu_status_operate(mock_run):
    # mock
    mock_run.return_value = Mock()
    mock_run.return_value.stdout = Mock()
    mock_run.return_value.stdout.decode.return_value = 'test_output'

    # call
    response = rsufwdsnmpset.set_rsu_status(rsu_ip, snmp_creds, True)

    # check
    mock_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 RSU-MIB:rsuMode.0 i 4', shell=True, capture_output=True, check=True)
    expected_response = 'success'
    assert response == expected_response

@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_set_rsu_status_standby(mock_run):
    # mock
    mock_run.return_value = Mock()
    mock_run.return_value.stdout = Mock()
    mock_run.return_value.stdout.decode.return_value = 'test_output'

    # call
    response = rsufwdsnmpset.set_rsu_status(rsu_ip, snmp_creds, False)

    # check
    mock_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 RSU-MIB:rsuMode.0 i 2', shell=True, capture_output=True, check=True)
    expected_response = 'success'
    assert response == expected_response

@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_perform_snmp_mods(subprocess_run):
    # mock
    subprocess_run.return_value = Mock()
    subprocess_run.return_value.stdout = Mock()
    subprocess_run.return_value.stdout.decode.return_value = 'test_output'

    # call
    snmp_mods = [
        {'oid': 'test_oid1', 'value': 'test_value1'},
        {'oid': 'test_oid2', 'value': 'test_value2'}
    ]
    response = rsufwdsnmpset.perform_snmp_mods(snmp_mods)
    
    # check
    subprocess_run.assert_has_calls([
        call(snmp_mods[0], shell=True, capture_output=True, check=True),
        call().stdout.decode('utf-8'),
        call(snmp_mods[1], shell=True, capture_output=True, check=True),
        call().stdout.decode('utf-8'),
    ])
    expected_response = None
    assert response == expected_response

@patch('api.src.rsufwdsnmpset.subprocess.run')
@patch('api.src.rsufwdsnmpset.perform_snmp_mods')
def test_config_msgfwd_yunex_tx(mock_perform_snmp_mods, mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'

    # call
    udp_port = 1234
    psid = 5678
    tx = True
    result = rsufwdsnmpset.config_msgfwd_yunex(rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, tx)

    # check
    mock_perform_snmp_mods.assert_called_once()
    expected_result = ("Successfully completed the Yunex SNMPSET configuration", 200)
    assert result == expected_result

@patch('api.src.rsufwdsnmpset.subprocess.run')
@patch('api.src.rsufwdsnmpset.perform_snmp_mods')
def test_config_msgfwd_yunex_no_tx(mock_perform_snmp_mods, mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'

    # call
    udp_port = 1234
    psid = 5678
    tx = False
    result = rsufwdsnmpset.config_msgfwd_yunex(rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, tx)

    # check
    mock_perform_snmp_mods.assert_called_once()
    expected_result = ("Successfully completed the Yunex SNMPSET configuration", 200)
    assert result == expected_result

@patch('api.src.rsufwdsnmpset.set_rsu_status')
def test_config_msgfwd(mock_set_rsu_status):
    manufacturer = 'test_manufacturer'
    udp_port = 1234
    psid = 1
    
    # call function
    rsufwdsnmpset.config_msgfwd(rsu_ip, manufacturer, snmp_creds, dest_ip, udp_port, rsu_index, psid)
    
    # check calls
    mock_set_rsu_status.assert_has_calls([
        call(rsu_ip, snmp_creds, operate=False),
        call().__ne__('success'),
        call(rsu_ip, snmp_creds, operate=True)
    ])

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_kapsch(mock_subprocess_run, mock_set_rsu_status):
    # mock subprocess.run
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'

    # mock set_rsu_status
    mock_set_rsu_status.return_value = 'success'

    # prepare args
    manufacturer = 'Kapsch'
    msg_type = 'test_msg_type'
    
    # call function
    rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check calls
    mock_set_rsu_status.assert_has_calls([
        call(rsu_ip, snmp_creds, operate=False),
        call(rsu_ip, snmp_creds, operate=True)
    ])
    mock_subprocess_run.assert_called_once()

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_kapsch_set_rsu_status_failure(mock_subprocess_run, mock_set_rsu_status):
    # mock subprocess.run
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'

    # mock set_rsu_status
    mock_set_rsu_status.return_value = 'failure'

    # prepare args
    manufacturer = 'Kapsch'
    msg_type = 'test_msg_type'

    # call function
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check calls
    mock_set_rsu_status.assert_has_calls([
        call(rsu_ip, snmp_creds, operate=False),
        call(rsu_ip, snmp_creds, operate=True)
    ])
    mock_subprocess_run.assert_not_called()
    assert result == ('failure', 500)

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_commsignia(mock_subprocess_run, mock_set_rsu_status):
    # mock subprocess.run
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'

    # mock set_rsu_status
    mock_set_rsu_status.return_value = 'success'

    # prepare args
    manufacturer = 'Commsignia'
    msg_type = 'test_msg_type'

    # call function
    rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check calls
    mock_set_rsu_status.assert_has_calls([
        call(rsu_ip, snmp_creds, operate=False),
        call(rsu_ip, snmp_creds, operate=True)
    ])
    mock_subprocess_run.assert_called_once()

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_yunex_bsm(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'
    mock_set_rsu_status.return_value = 'success'

    # call
    manufacturer = 'Yunex'
    msg_type = 'bsm'
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 1.3.6.1.4.1.1206.4.2.18.5.2.1.10.1 i 6 ', shell=True, capture_output=True, check=True)
    assert result == ("Successfully deleted the Yunex SNMPSET configuration", 200)

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_yunex_spat(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'
    mock_set_rsu_status.return_value = 'success'

    # call
    manufacturer = 'Yunex'
    msg_type = 'spat'
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 1.3.6.1.4.1.1206.4.2.18.20.2.1.9.1 i 6 ', shell=True, capture_output=True, check=True)
    assert result == ("Successfully deleted the Yunex SNMPSET configuration", 200)

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_yunex_map(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'
    mock_set_rsu_status.return_value = 'success'

    # call
    manufacturer = 'Yunex'
    msg_type = 'map'
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 1.3.6.1.4.1.1206.4.2.18.20.2.1.9.1 i 6 ', shell=True, capture_output=True, check=True)
    assert result == ("Successfully deleted the Yunex SNMPSET configuration", 200)

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_yunex_ssm(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'
    mock_set_rsu_status.return_value = 'success'

    # call
    manufacturer = 'Yunex'
    msg_type = 'ssm'
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 1.3.6.1.4.1.1206.4.2.18.20.2.1.9.1 i 6 ', shell=True, capture_output=True, check=True)
    assert result == ("Successfully deleted the Yunex SNMPSET configuration", 200)

@patch('api.src.rsufwdsnmpset.set_rsu_status')
@patch('api.src.rsufwdsnmpset.subprocess.run')
def test_config_del_yunex_srm(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test_output'
    mock_set_rsu_status.return_value = 'success'

    # call
    manufacturer = 'Yunex'
    msg_type = 'srm'
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)
    
    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with('snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 1.3.6.1.4.1.1206.4.2.18.5.2.1.10.1 i 6 ', shell=True, capture_output=True, check=True)
    assert result == ("Successfully deleted the Yunex SNMPSET configuration", 200)

@patch('api.src.rsufwdsnmpset.set_rsu_status')
def test_config_del_unsupported_manufacturer(mock_set_rsu_status):
    # prepare args
    manufacturer = 'test_manufacturer'
    msg_type = 'test_msg_type'

    # call function
    result = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)

    # check result
    expected_response = "Supported RSU manufacturers are currently only Commsignia, Kapsch and Yunex"
    expected_code = 501
    assert result == (expected_response, expected_code)

    # check calls
    mock_set_rsu_status.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'BSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '46800', rsu_index, '20')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'SPaT'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44910', rsu_index, '8002')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'MAP'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44920', rsu_index, 'E0000017', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'SSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44900', rsu_index, 'E0000015', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'SRM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44930', rsu_index, 'E0000016', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_unsupported_msg_type(mock_config_msgfwd_yunex, mock_config_msgfwd):
    manufacturer = 'Kapsch'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported message type is currently only BSM, SPaT, MAP, SSM and SRM", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'BSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '46800', rsu_index, '20')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'SPaT'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44910', rsu_index, '8002')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'MAP'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44920', rsu_index, 'E0000017', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'SSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44900', rsu_index, 'E0000015', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'SRM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44930', rsu_index, 'E0000016', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_unsupported_msg_type(mock_config_msfwd_yunex, mock_config_msgfwd):
    manufacturer = 'Commsignia'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported message type is currently only BSM, SPaT, MAP, SSM and SRM", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'BSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '46800', rsu_index, '20', False)
    mock_config_msgfwd.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'SPaT'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44910', rsu_index, '8002', True)
    mock_config_msgfwd.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'MAP'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44920', rsu_index, 'E0000017', True)
    mock_config_msgfwd.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'SSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44900', rsu_index, 'E0000015', True)
    mock_config_msgfwd.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'SRM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44930', rsu_index, 'E0000016', False)
    mock_config_msgfwd.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_unsupported_msg_type(mock_config_msfwd_yunex, mock_config_msgfwd):
    manufacturer = 'Yunex'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported message type is currently only BSM, SPaT, MAP, SSM and SRM", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.config_msgfwd')
@patch('api.src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_unsupported_manufacturer(mock_config_msfwd_yunex, mock_config_msgfwd):
    manufacturer = 'test_manufacturer'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported RSU manufacturers are currently only Commsignia, Kapsch and Yunex", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()

@patch('api.src.rsufwdsnmpset.SnmpsetSchema.validate')
@patch('api.src.rsufwdsnmpset.config_init')
def test_post(mock_config_init, mock_validate):
    # mock validate
    mock_validate.return_value = None

    # mock config_init
    mock_config_init.return_value = { 'status': 'success' }, 200

    # prepare request
    request = {
        'args': {
            'msg_type': 'init',
            'dest_ip': '192.168.0.20',
            'rsu_index': 1,
        },
        'rsu_ip': '192.168.0.20',
        'manufacturer': 'test_manufacturer',
        'snmp_creds': {'username': 'test_username', 'password': 'test_password'}
    }

    # call function
    result = rsufwdsnmpset.post(request)

    # check calls
    mock_validate.assert_called_once()
    mock_config_init.assert_called_once_with(request['rsu_ip'], request['manufacturer'], request['snmp_creds'], request['args']['dest_ip'], request['args']['msg_type'], request['args']['rsu_index'])

    # check result
    expected_code = 200
    expected_response = {'status': 'success'}
    expected_result = { "RsuFwdSnmpset": expected_response }, expected_code
    assert result == expected_result

@patch('api.src.rsufwdsnmpset.SnmpsetSchema.validate')
@patch('api.src.rsufwdsnmpset.config_init')
def test_post_error(mock_config_init, mock_validate):
    # mock validate
    mock_validate.return_value = "error"

    # prepare request
    request = {
        'args': {
            'msg_type': 'init',
            'rsu_index': 1,
        },
        'rsu_ip': '192.168.0.20',
        'manufacturer': 'test_manufacturer',
        'snmp_creds': {'username': 'test_username', 'password': 'test_password'},
    }

    # call function
    result = rsufwdsnmpset.post(request)

    # check calls
    mock_validate.assert_called_once_with(request['args'])
    mock_config_init.assert_not_called()

    # check result
    assert result == ("The provided args does not match required values: " + mock_validate.return_value, 400)

@patch('api.src.rsufwdsnmpset.SnmpsetDeleteSchema.validate')
@patch('api.src.rsufwdsnmpset.config_del')
def test_delete(mock_config_del, mock_validate):
    # mock validate
    mock_validate.return_value = None

    # mock config_del
    mock_config_del.return_value = { 'status': 'success' }, 200

    # prepare request
    request = {
        'args': {
            'msg_type': 'delete',
            'rsu_index': 1,
        },
        'rsu_ip': '192.168.0.20',
        'manufacturer': 'test_manufacturer',
        'snmp_creds': {'username': 'test_username', 'password': 'test_password'}
    }

    # call function
    result = rsufwdsnmpset.delete(request)

    # check calls
    mock_validate.assert_called_once()
    mock_config_del.assert_called_once_with(request['rsu_ip'], request['manufacturer'], request['snmp_creds'], request['args']['msg_type'], request['args']['rsu_index'])

    # check result
    expected_code = 200
    expected_response = {'status': 'success'}
    expected_result = { "RsuFwdSnmpset": expected_response }, expected_code
    assert result == expected_result

@patch('api.src.rsufwdsnmpset.SnmpsetDeleteSchema.validate')
@patch('api.src.rsufwdsnmpset.config_del')
def test_delete_error(mock_config_del, mock_validate):
    # mock validate
    mock_validate.return_value = 'error'

    # prepare request
    request = {
        'args': {
            'msg_type': 'delete',
            'rsu_index': 1,
        },
        'rsu_ip': '192.168.0.20',
        'manufacturer': 'test_manufacturer',
        'snmp_creds': {'username': 'test_username', 'password': 'test_password'}
    }

    # call function
    result = rsufwdsnmpset.delete(request)

    # check calls
    mock_validate.assert_called_once()
    mock_config_del.assert_not_called()

    # check result
    expected_result = ("The provided args does not match required values: " + mock_validate.return_value, 400)
    assert result == expected_result

# TODO: implement tests for exception/failure states