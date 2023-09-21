import datetime
from unittest.mock import MagicMock, call, patch, Mock, create_autospec
from src import rsufwdsnmpset
import subprocess


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

@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.subprocess.run')
@patch('src.rsufwdsnmpset.perform_snmp_mods')
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

@patch('src.rsufwdsnmpset.subprocess.run')
@patch('src.rsufwdsnmpset.perform_snmp_mods')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
@patch('src.rsufwdsnmpset.subprocess.run')
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

@patch('src.rsufwdsnmpset.set_rsu_status')
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

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'BSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '46800', rsu_index, '20')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'SPaT'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44910', rsu_index, '8002')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'MAP'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44920', rsu_index, 'E0000017', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'SSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44900', rsu_index, 'E0000015', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Kapsch'
    msg_type = 'SRM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44930', rsu_index, 'E0000016', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_kapsch_unsupported_msg_type(mock_config_msgfwd_yunex, mock_config_msgfwd):
    manufacturer = 'Kapsch'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported message type is currently only BSM, SPaT, MAP, SSM and SRM", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'BSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '46800', rsu_index, '20')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'SPaT'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44910', rsu_index, '8002')
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'MAP'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44920', rsu_index, 'E0000017', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'SSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44900', rsu_index, 'E0000015', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = 'success'
    manufacturer = 'Commsignia'
    msg_type = 'SRM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(rsu_ip, manufacturer, snmp_creds, dest_ip, '44930', rsu_index, 'E0000016', raw=True)
    mock_config_msgfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_commsignia_unsupported_msg_type(mock_config_msfwd_yunex, mock_config_msgfwd):
    manufacturer = 'Commsignia'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported message type is currently only BSM, SPaT, MAP, SSM and SRM", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'BSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '46800', rsu_index, '20', False)
    mock_config_msgfwd.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'SPaT'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44910', rsu_index, '8002', True)
    mock_config_msgfwd.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'MAP'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44920', rsu_index, 'E0000017', True)
    mock_config_msgfwd.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'SSM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44900', rsu_index, 'E0000015', True)
    mock_config_msgfwd.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd_yunex.return_value = 'success'
    manufacturer = 'Yunex'
    msg_type = 'SRM'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = 'success'
    assert result == expected_result
    mock_config_msgfwd_yunex.assert_called_once_with(rsu_ip, snmp_creds, dest_ip, '44930', rsu_index, 'E0000016', False)
    mock_config_msgfwd.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_yunex_unsupported_msg_type(mock_config_msfwd_yunex, mock_config_msgfwd):
    manufacturer = 'Yunex'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported message type is currently only BSM, SPaT, MAP, SSM and SRM", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.config_msgfwd')
@patch('src.rsufwdsnmpset.config_msgfwd_yunex')
def test_config_init_unsupported_manufacturer(mock_config_msfwd_yunex, mock_config_msgfwd):
    manufacturer = 'test_manufacturer'
    msg_type = 'test_msg_type'
    result = rsufwdsnmpset.config_init(rsu_ip, manufacturer, snmp_creds, dest_ip, msg_type, rsu_index)
    expected_result = "Supported RSU manufacturers are currently only Commsignia, Kapsch and Yunex", 501
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()

@patch('src.rsufwdsnmpset.SnmpsetSchema.validate')
@patch('src.rsufwdsnmpset.config_init')
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

@patch('src.rsufwdsnmpset.SnmpsetSchema.validate')
@patch('src.rsufwdsnmpset.config_init')
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

@patch('src.rsufwdsnmpset.SnmpsetDeleteSchema.validate')
@patch('src.rsufwdsnmpset.config_del')
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

@patch('src.rsufwdsnmpset.SnmpsetDeleteSchema.validate')
@patch('src.rsufwdsnmpset.config_del')
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

@patch('src.rsufwdsnmpset.set_rsu_status', return_value='success')
@patch('src.rsufwdsnmpset.perform_snmp_mods')
def test_config_msgfwd_raw_false( mock_perform_snmp_mods, mock_set_rsu_status):
    # Set up test data
    rsu_ip = '192.168.1.1'
    manufacturer = 'Kapsch'
    snmp_creds = {'username': 'username', 'password': 'password'}
    dest_ip = '192.168.1.2'
    index = 1
    psid = '20'
    raw = False

    # Call the function
    response, code = rsufwdsnmpset.config_msgfwd(rsu_ip, manufacturer, snmp_creds, dest_ip, '44920', index, psid, raw)

    # Check the result
    assert code == 200, "Unexpected code"
    assert response == "Successfully completed the rsuDsrcFwd SNMPSET configuration", "Unexpected response"

    # # # Check that set_rsu_status and perform_snmp_mods were called correctly
    mock_set_rsu_status.assert_any_call(rsu_ip, snmp_creds, operate=False)
    mock_set_rsu_status.assert_any_call(rsu_ip, snmp_creds, operate=True)
    mock_perform_snmp_mods.assert_called_once()

@patch('src.rsufwdsnmpset.set_rsu_status', return_value='success')
@patch('src.rsufwdsnmpset.perform_snmp_mods')
def test_config_msgfwd_raw_true( mock_perform_snmp_mods, mock_set_rsu_status):
    # Set up test data
    rsu_ip = '192.168.1.1'
    snmp_creds = {'username': 'username', 'password': 'password'}
    dest_ip = '192.168.1.2'
    index = 1 
    psid = '20'
    manufacturer = 'Kapsch'
    raw = True
    # Call the function
    response, code = rsufwdsnmpset.config_msgfwd(rsu_ip, manufacturer, snmp_creds, dest_ip, '44920', index, psid, raw)

    # Check the result
    assert code == 200
    assert response == "Successfully completed the rsuDsrcFwd SNMPSET configuration"


def raise_called_process_error(*args, **kwargs):
    error = subprocess.CalledProcessError(1, cmd=['any'], stderr=b'any\n')
    raise error

@patch('src.rsufwdsnmpset.snmpcredential.get_authstring', return_value='auth_string')
@patch('src.rsufwdsnmpset.snmperrorcheck.check_error_type', return_value='error message')
@patch('subprocess.run', side_effect=raise_called_process_error)
def test_set_rsu_status_exception(mock_run, mock_check_error_type, mock_get_authstring):
    # Setup
    rsu_ip = '192.168.1.1'
    snmp_creds = {'username': 'username', 'password': 'password'}
    operate = True
    
    # Call the function
    result = rsufwdsnmpset.set_rsu_status(rsu_ip, snmp_creds, operate)
    mock_check_error_type.assert_called_once_with('any')
    
    # Assert the function result
    assert result == 'error message'

@patch('src.rsufwdsnmpset.snmpcredential.get_authstring', return_value='auth_string')
@patch('src.rsufwdsnmpset.snmperrorcheck.check_error_type', return_value='error message')
@patch('src.rsufwdsnmpset.perform_snmp_mods', side_effect=raise_called_process_error)
def test_config_msgfwd_yunex_exception(mock_perform_snmp_mods, mock_check_error_type, mock_get_authstring):
    # Setup
    rsu_ip = '192.168.1.1'
    snmp_creds = {'username': 'username', 'password': 'password'}
    dest_ip = '192.168.1.2'
    udp_port = '44920'
    rsu_index = 1 
    psid = '20'
    tx = True

    # Call the function
    response, code = rsufwdsnmpset.config_msgfwd_yunex(rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, tx)
    mock_check_error_type.assert_called_once_with('any')
    # Assert the function result
    assert code == 500
    assert response == 'error message'

@patch('src.rsufwdsnmpset.snmpcredential.get_authstring', return_value='auth_string')
@patch('src.rsufwdsnmpset.snmperrorcheck.check_error_type', return_value='error message')
@patch('src.rsufwdsnmpset.perform_snmp_mods', side_effect=raise_called_process_error)
@patch('src.rsufwdsnmpset.set_rsu_status', return_value='success')
def test_config_msgfwd_exception(mock_set_rsu_status, mock_perform_snmp_mods, mock_check_error_type, mock_get_authstring):
    # Setup
    rsu_ip = '192.168.1.1'
    manufacturer = 'manufacturer'
    snmp_creds = {'username': 'username', 'password': 'password'}
    dest_ip = '192.168.1.2'
    udp_port = '44920'
    rsu_index = 1 
    psid = '20'
    raw = False

    # Call the function
    response, code = rsufwdsnmpset.config_msgfwd(rsu_ip, manufacturer, snmp_creds, dest_ip, udp_port, rsu_index, psid, raw)

    # Assert that check_error_type was called with the last line of the error output
    mock_check_error_type.assert_called_once_with('any')

    # Assert the function result
    assert code == 500
    assert response == 'error message'

@patch('src.rsufwdsnmpset.snmpcredential.get_authstring', return_value='auth_string')
@patch('src.rsufwdsnmpset.snmperrorcheck.check_error_type', return_value='error message')
@patch('src.rsufwdsnmpset.subprocess.run', side_effect=raise_called_process_error)
@patch('src.rsufwdsnmpset.set_rsu_status', return_value='success')
def test_config_del_exception(mock_set_rsu_status, mock_run, mock_check_error_type, mock_get_authstring):
    # Setup
    rsu_ip = '192.168.1.1'
    manufacturer = 'Commsignia'
    snmp_creds = {'username': 'username', 'password': 'password'}
    msg_type = 'bsm'
    rsu_index = 1 

    # Call the function
    response, code = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)

    # Assert that check_error_type was called with the last line of the error output
    mock_check_error_type.assert_called_once_with('any')

    # Assert the function result
    assert code == 500
    assert response == 'error message'

@patch('src.rsufwdsnmpset.set_rsu_status', return_value='success')
@patch('src.rsufwdsnmpset.subprocess.run')
@patch('src.rsufwdsnmpset.snmperrorcheck.check_error_type', return_value='test error')
def test_config_del_yunex_manufacturer_exception(mock_check_error_type, mock_run, mock_set_rsu_status):
    # Setup
    rsu_ip = '192.168.1.1'
    manufacturer = 'Yunex'  
    snmp_creds = {'username': 'username', 'password': 'password'}
    msg_type = 'bsm'  # This can be any of the following: ['bsm', 'spat', 'map', 'ssm', 'srm']
    rsu_index = 1 

    # mock subprocess.run to raise CalledProcessError
    mock_error = Mock()
    mock_error.stderr.decode.return_value = 'any\n'
    mock_run.return_value = ['hello']
    mock_run.side_effect = subprocess.CalledProcessError(1, cmd=['any'], stderr=b'\n error line')

    # Call the function
    response, code = rsufwdsnmpset.config_del(rsu_ip, manufacturer, snmp_creds, msg_type, rsu_index)

    # Assert the function result
    expected_response = 'test error'
    expected_code = 500
    assert code == expected_code
    assert response == expected_response


# TODO: implement tests for exception/failure states