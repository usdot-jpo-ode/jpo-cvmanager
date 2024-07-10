from unittest.mock import Mock, patch
from common import rsufwdsnmpwalk

source_ip = "192.168.0.10"
rsu_ip = "192.168.0.20"
snmp_creds = {"ip": source_ip, "username": "public", "password": "public"}


@patch("common.rsufwdsnmpwalk.subprocess.run")
def test_snmpwalk_rsudsrcfwd_no_snmp_config(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test"

    # prepare input
    snmp_creds = {"ip": "192.168.0.10", "username": "public", "password": "public", "encrypt_pw": None}
    rsu_ip = "192.168.0.20"

    # call function
    output = rsufwdsnmpwalk.snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip)

    # verify
    expected_snmp_config = {}
    expected_output = ({"RsuFwdSnmpwalk": expected_snmp_config}, 200)
    assert output == expected_output


@patch("common.rsufwdsnmpwalk.subprocess.run")
def test_snmpwalk_rsudsrcfwd_with_snmp_config(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = (
        'iso.0.15628.4.1.7.1.2.1 = STRING: " "\n' * 15
    )

    # prepare input
    snmp_creds = {"ip": "192.168.0.10", "username": "public", "password": "public", "encrypt_pw": None}
    rsu_ip = "192.168.0.20"

    # call function
    output = rsufwdsnmpwalk.snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip)

    # verify
    expected_snmp_config = {"1": {"Message Type": "BSM"}}
    expected_output = ({"RsuFwdSnmpwalk": expected_snmp_config}, 200)
    assert output == expected_output


def test_snmpwalk_rsudsrcfwd_exception():
    # prepare input
    snmp_creds = {"ip": "192.168.0.10", "username": "public", "password": "public", "encrypt_pw": None}
    rsu_ip = "192.168.0.20"

    # call function
    output = rsufwdsnmpwalk.snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip)

    # verify
    expected_possible_outputs = [
        ({"RsuFwdSnmpwalk": "operable program or batch file.\r"}, 500),  # windows
        ({"RsuFwdSnmpwalk": "/bin/sh: 1: snmpwalk: not found"}, 500),  # linux
        (
            {
                "RsuFwdSnmpwalk": "Error generating a key (Ku) from the supplied authentication pass phrase. "
            },
            500,
        ),  # snmp error
    ]
    assert output[1] == 500
    assert output in expected_possible_outputs


@patch("common.rsufwdsnmpwalk.subprocess.run")
def test_snmpwalk_txrxmsg(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test"

    # prepare input
    source_ip = "192.168.0.10"
    snmp_creds = {"ip": source_ip, "username": "public", "password": "public", "encrypt_pw": None}
    rsu_ip = "192.168.0.20"

    # call function
    output = rsufwdsnmpwalk.snmpwalk_txrxmsg(snmp_creds, rsu_ip)

    # verify
    expected_snmp_results = {"rsuReceivedMsgTable": {}, "rsuXmitMsgFwdingTable": {}}
    expected_output = ({"RsuFwdSnmpwalk": expected_snmp_results}, 200)
    assert output == expected_output


def test_snmpwalk_txrxmsg_exception():
    # prepare input
    source_ip = "192.168.0.10"
    snmp_creds = {"ip": source_ip, "username": "public", "password": "public", "encrypt_pw": None}
    rsu_ip = "192.168.0.20"

    # call function
    output = rsufwdsnmpwalk.snmpwalk_txrxmsg(snmp_creds, rsu_ip)

    # verify
    expected_possible_outputs = [
        ({"RsuFwdSnmpwalk": "operable program or batch file.\r"}, 500),  # windows
        ({"RsuFwdSnmpwalk": "/bin/sh: 1: snmpwalk: not found"}, 500),  # linux
        (
            {
                "RsuFwdSnmpwalk": "Error generating a key (Ku) from the supplied authentication pass phrase. "
            },
            500,
        ),  # snmp error
    ]
    assert output[1] == 500
    assert output in expected_possible_outputs


@patch("common.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd")
@patch("common.rsufwdsnmpwalk.snmpwalk_txrxmsg")
def test_get_rsu41(mock_snmpwalk_txrxmsg, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {
        "snmp_creds": snmp_creds,
        "rsu_ip": rsu_ip,
        "manufacturer": "Commsignia",
        "snmp_version": "41",
    }

    # call function
    rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_called_once_with(snmp_creds, rsu_ip)
    mock_snmpwalk_txrxmsg.assert_not_called()


@patch("common.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd")
@patch("common.rsufwdsnmpwalk.snmpwalk_txrxmsg")
def test_get_ntcip1218(mock_snmpwalk_txrxmsg, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {
        "snmp_creds": snmp_creds,
        "rsu_ip": rsu_ip,
        "manufacturer": "Yunex",
        "snmp_version": "1218",
    }

    # call function
    rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_not_called()
    mock_snmpwalk_txrxmsg.assert_called_once_with(snmp_creds, rsu_ip)


@patch("common.rsufwdsnmpwalk.snmpwalk_rsudsrcfwd")
@patch("common.rsufwdsnmpwalk.snmpwalk_txrxmsg")
def test_get_exception(mock_snmpwalk_txrxmsg, mock_snmpwalk_rsudsrcfwd):
    # prepare input
    request = {
        "snmp_creds": snmp_creds,
        "rsu_ip": rsu_ip,
        "manufacturer": "Unknown",
        "snmp_version": "Unknown",
    }

    # call function
    output = rsufwdsnmpwalk.get(request)

    # verify
    mock_snmpwalk_rsudsrcfwd.assert_not_called()
    mock_snmpwalk_txrxmsg.assert_not_called()
    expected_output = (
        "Supported SNMP versions are currently only RSU 4.1 and NTCIP 1218",
        501,
    )
    assert output == expected_output
