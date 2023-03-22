from unittest.mock import Mock, patch
from src import rsufwdsnmpwalk

source_ip = '192.168.0.10'
rsu_ip = '192.168.0.20'
snmp_creds = {'ip': source_ip, 'username': 'public', 'password': 'public'}

def test_message_type():
    # test BSM PSIDs
    val = '\" \"'
    assert(rsufwdsnmpwalk.message_type(val) == 'BSM')
    val = '00 00 00 20'
    assert(rsufwdsnmpwalk.message_type(val) == 'BSM')

    # test SPAT PSIDs
    val = '00 00 80 02'
    assert(rsufwdsnmpwalk.message_type(val) == 'SPaT')
    val = '80 02'
    assert(rsufwdsnmpwalk.message_type(val) == 'SPaT')

    # test MAP PSID
    val = 'E0 00 00 17'
    assert(rsufwdsnmpwalk.message_type(val) == 'MAP')

    # test SSM PSID
    val = 'E0 00 00 15'
    assert(rsufwdsnmpwalk.message_type(val) == 'SSM')

    # test SRM PSID
    val = 'E0 00 00 16'
    assert(rsufwdsnmpwalk.message_type(val) == 'SRM')

    # test other PSID
    val = '00 00 00 00'
    assert(rsufwdsnmpwalk.message_type(val) == 'Other')
 
def test_ip():
    val = 'c0 a8 00 0a'
    assert(rsufwdsnmpwalk.ip(val) == '192.168.0.10')

def test_yunex_ip():
    val = '\"10.0.0.1\"'
    assert(rsufwdsnmpwalk.yunex_ip(val) == '10.0.0.1')

def test_protocol():
    val = '1'
    assert(rsufwdsnmpwalk.protocol(val) == 'TCP')
    val = '2'
    assert(rsufwdsnmpwalk.protocol(val) == 'UDP')
    val = '14'
    assert(rsufwdsnmpwalk.protocol(val) == 'Other')

def test_fwdon():
    val = '1'
    assert(rsufwdsnmpwalk.fwdon(val) == 'On')
    val = '0'
    assert(rsufwdsnmpwalk.fwdon(val) == 'Off')

def test_active():
    val = '1'
    assert(rsufwdsnmpwalk.active(val) == 'Enabled')
    val = '4'
    assert(rsufwdsnmpwalk.active(val) == 'Enabled')
    val = '17'
    assert(rsufwdsnmpwalk.active(val) == 'Disabled')

def test_toint():
    mystr = '123'
    assert(rsufwdsnmpwalk.toint(mystr) == 123)

def test_startend():
    # prepare hex input
    hex_input = '05 06 07 08 09 10'

    # call function
    output = rsufwdsnmpwalk.startend(hex_input)

    # verify
    expected_output = '1286-07-08 09:16'
    assert(output == expected_output)

@patch('src.rsufwdsnmpwalk.subprocess.run')
def test_snmpwalk_rsudsrcfwd_no_snmp_config(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test'

    # prepare input
    snmp_creds = {'ip': '192.168.0.10', 'username': 'public', 'password': 'public'}
    rsu_ip = '192.168.0.20'

    # call function
    output = rsufwdsnmpwalk.snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip)

    # verify
    expected_snmp_config = {}
    expected_output = ({ "RsuFwdSnmpwalk": expected_snmp_config }, 200)
    assert(output == expected_output)

@patch('src.rsufwdsnmpwalk.subprocess.run')
def test_snmpwalk_rsudsrcfwd_with_snmp_config(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'iso.0.15628.4.1.7.1.2.1 = STRING: " "\n' * 15

    # prepare input
    snmp_creds = {'ip': '192.168.0.10', 'username': 'public', 'password': 'public'}
    rsu_ip = '192.168.0.20'

    # call function
    output = rsufwdsnmpwalk.snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip)

    # verify
    expected_snmp_config = {'1': {'Message Type': 'BSM'}}
    expected_output = ({'RsuFwdSnmpwalk': expected_snmp_config}, 200)
    assert(output == expected_output)

def test_snmpwalk_rsudsrcfwd_exception():
    # prepare input
    snmp_creds = {'ip': '192.168.0.10', 'username': 'public', 'password': 'public'}
    rsu_ip = '192.168.0.20'

    # call function
    output = rsufwdsnmpwalk.snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip)

    # verify
    expected_possible_outputs = [
        ({'RsuFwdSnmpwalk': 'operable program or batch file.\r'}, 500), # windows
        ({'RsuFwdSnmpwalk': '/bin/sh: 1: snmpwalk: not found'}, 500), # linux
        ({'RsuFwdSnmpwalk': 'Error generating a key (Ku) from the supplied authentication pass phrase. '}, 500) # snmp error
    ]
    assert(output[1] == 500)
    assert(output in expected_possible_outputs)

@patch('src.rsufwdsnmpwalk.subprocess.run')
def test_snmpwalk_yunex(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = 'test'
    
    # prepare input
    source_ip = '192.168.0.10'
    snmp_creds = {'ip': source_ip, 'username': 'public', 'password': 'public'}
    rsu_ip = '192.168.0.20'

    # call function
    output = rsufwdsnmpwalk.snmpwalk_yunex(snmp_creds, rsu_ip)

    # verify
    expected_snmp_results = {'rsuReceivedMsgTable': {}, 'rsuXmitMsgFwdingTable': {}}
    expected_output = ({'RsuFwdSnmpwalk': expected_snmp_results}, 200)
    assert(output == expected_output)

def test_snmpwalk_yunex_exception():
    # prepare input
    source_ip = '192.168.0.10'
    snmp_creds = {'ip': source_ip, 'username': 'public', 'password': 'public'}
    rsu_ip = '192.168.0.20'

    # call function
    output = rsufwdsnmpwalk.snmpwalk_yunex(snmp_creds, rsu_ip)

    # verify
    expected_possible_outputs = [
        ({'RsuFwdSnmpwalk': 'operable program or batch file.\r'}, 500), # windows
        ({'RsuFwdSnmpwalk': '/bin/sh: 1: snmpwalk: not found'}, 500), # linux
        ({'RsuFwdSnmpwalk': 'Error generating a key (Ku) from the supplied authentication pass phrase. '}, 500) # snmp error
    ]
    assert(output[1] == 500)
    assert(output in expected_possible_outputs)

@patch('src.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd')
@patch('src.rsufwdsnmpwalk.snmpwalk_yunex')
def test_get_kapsch(mock_snmpwalk_yunex, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {'snmp_creds': snmp_creds, 'rsu_ip': rsu_ip, 'manufacturer': 'Kapsch'}

    # call function
    rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_called_once_with(snmp_creds, rsu_ip)
    mock_snmpwalk_yunex.assert_not_called()

@patch('src.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd')
@patch('src.rsufwdsnmpwalk.snmpwalk_yunex')
def test_get_commsignia(mock_snmpwalk_yunex, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {'snmp_creds': snmp_creds, 'rsu_ip': rsu_ip, 'manufacturer': 'Commsignia'}

    # call function
    rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_called_once_with(snmp_creds, rsu_ip)
    mock_snmpwalk_yunex.assert_not_called()

@patch('src.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd')
@patch('src.rsufwdsnmpwalk.snmpwalk_yunex')
def test_get_yunex(mock_snmpwalk_yunex, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {'snmp_creds': snmp_creds, 'rsu_ip': rsu_ip, 'manufacturer': 'Yunex'}

    # call function
    rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_not_called()
    mock_snmpwalk_yunex.assert_called_once_with(snmp_creds, rsu_ip)

@patch('src.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd')
@patch('src.rsufwdsnmpwalk.snmpwalk_yunex')
def test_get_exception(mock_snmpwalk_yunex, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {'snmp_creds': snmp_creds, 'rsu_ip': rsu_ip, 'manufacturer': 'Unknown'}

    # call function
    output = rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_not_called()
    mock_snmpwalk_yunex.assert_not_called()
    expected_output = ("Supported RSU manufacturers are currently only Commsignia, Kapsch and Yunex", 501)
    assert(output == expected_output)