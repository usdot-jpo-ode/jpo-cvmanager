import datetime
from unittest.mock import MagicMock, call, patch, Mock, create_autospec
import common.snmp.ntcip1218.rsu_message_forward as rsu_message_forward
import subprocess


# static values
rsu_ip = "192.168.0.20"
snmp_creds = {
    "username": "test_username",
    "password": "test_password",
    "encrypt_pw": None,
}
dest_ip = "192.168.0.10"
rsu_index = 1


def test_hex_datetime():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    expected = "07e401010101"
    assert rsu_message_forward.hex_datetime(dt) == expected


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
def test_perform_snmp_mods(subprocess_run):
    # mock
    subprocess_run.return_value = Mock()
    subprocess_run.return_value.stdout = Mock()
    subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    snmp_mods = [
        {"oid": "test_oid1", "value": "test_value1"},
        {"oid": "test_oid2", "value": "test_value2"},
    ]
    response = rsu_message_forward.perform_snmp_mods(snmp_mods)

    # check
    subprocess_run.assert_has_calls(
        [
            call(snmp_mods[0], shell=True, capture_output=True, check=True),
            call().stdout.decode("utf-8"),
            call(snmp_mods[1], shell=True, capture_output=True, check=True),
            call().stdout.decode("utf-8"),
        ]
    )
    expected_response = None
    assert response == expected_response


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_message_forward.perform_snmp_mods")
def test_set(mock_perform_snmp_mods, mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    udp_port = 1234
    psid = 5678
    tx = True
    security = 1
    result = rsu_message_forward.set(
        rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, security, tx
    )

    # check
    mock_perform_snmp_mods.assert_called_once()
    expected_result = (
        "Successfully completed the NTCIP-1218 SNMPSET configuration",
        200,
    )
    assert result == expected_result


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_message_forward.perform_snmp_mods")
def test_set_no_tx(mock_perform_snmp_mods, mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    udp_port = 1234
    psid = 5678
    tx = False
    security = 0
    result = rsu_message_forward.set(
        rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, security, tx
    )

    # check
    mock_perform_snmp_mods.assert_called_once()
    expected_result = (
        "Successfully completed the NTCIP-1218 SNMPSET configuration",
        200,
    )
    assert result == expected_result


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
def test_config_del_ntcip1218_bsm(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    msg_type = "bsm"
    result = rsu_message_forward.delete(rsu_ip, snmp_creds, msg_type, rsu_index)

    # check
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -t 5 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuReceivedMsgStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
def test_config_del_ntcip1218_spat(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    msg_type = "spat"
    result = rsu_message_forward.delete(rsu_ip, snmp_creds, msg_type, rsu_index)

    # check
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -t 5 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuXmitMsgFwdingStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
def test_config_del_ntcip1218_srm(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    snmp_version = "1218"
    msg_type = "srm"
    result = rsu_message_forward.delete(rsu_ip, snmp_creds, msg_type, rsu_index)

    # check
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -t 5 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuReceivedMsgStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
@patch(
    "common.snmp.ntcip1218.rsu_message_forward.snmperrorcheck.check_error_type",
    return_value="test error",
)
def test_ntcip1218_rsu_message_forward_delete_exception(
    mock_check_error_type, mock_run
):
    # Setup
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password", "encrypt_pw": None}
    msg_type = (
        "bsm"  # This can be any of the following: ['bsm', 'spat', 'map', 'ssm', 'srm']
    )
    rsu_index = 1

    # mock subprocess.run to raise CalledProcessError
    mock_error = Mock()
    mock_error.stderr.decode.return_value = "any\n"
    mock_run.return_value = ["hello"]
    mock_run.side_effect = subprocess.CalledProcessError(
        1, cmd=["any"], stderr=b"\n error line"
    )

    # Call the function
    response, code = rsu_message_forward.delete(rsu_ip, snmp_creds, msg_type, rsu_index)

    # Assert the function result
    expected_response = "test error"
    expected_code = 500
    assert code == expected_code
    assert response == expected_response


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
@patch(
    "common.snmp.ntcip1218.rsu_message_forward.snmpcredential.get_authstring",
    return_value="auth_string",
)
def test_ntcip1218_rsu_message_forward_get(mock_get_authstring, mock_run):
    # Prepare input
    rsu_ip = "192.168.0.20"
    snmp_creds = {
        "ip": "10.0.0.1",
        "username": "public",
        "password": "password",
        "encrypt_pw": "password",
    }

    # Mock subprocess.run to simulate a successful SNMP GET operation
    mock_run.return_value = Mock(stdout=b"SNMP GET successful")

    # Call the function
    response = rsu_message_forward.get(rsu_ip, snmp_creds)

    # Verify subprocess.run was called with the expected arguments
    expected_calls = [
        call(
            "snmpwalk -v 3 -t 5 auth_string 192.168.0.20 NTCIP1218-v01:rsuReceivedMsgTable",
            shell=True,
            capture_output=True,
            check=True,
        ),
        call(
            "snmpwalk -v 3 -t 5 auth_string 192.168.0.20 NTCIP1218-v01:rsuXmitMsgFwdingTable",
            shell=True,
            capture_output=True,
            check=True,
        ),
    ]
    mock_run.assert_has_calls(expected_calls, any_order=False)
    assert mock_get_authstring.call_count == 2

    # Verify the response
    assert response == (
        {"RsuFwdSnmpwalk": {"rsuReceivedMsgTable": {}, "rsuXmitMsgFwdingTable": {}}},
        200,
    )


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
def test_ntcip1218_rsu_message_forward_get_with_snmp_config(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.side_effect = [
        "NTCIP1218-v01::rsuReceivedMsgPsid.1 = STRING: 20000000\n" * 15,
        "NTCIP1218-v01::rsuXmitMsgFwdingPsid.1 = STRING: e0000017\n" * 15,
    ]

    # prepare input
    rsu_ip = "192.168.0.20"
    snmp_creds = {
        "ip": "10.0.0.1",
        "username": "public",
        "password": "password",
        "encrypt_pw": "password",
    }

    # call function
    output = rsu_message_forward.get(rsu_ip, snmp_creds)

    # verify
    expected_snmp_config = {
        "rsuReceivedMsgTable": {"1": {"Message Type": "BSM"}},
        "rsuXmitMsgFwdingTable": {"1": {"Message Type": "MAP"}},
    }
    expected_output = ({"RsuFwdSnmpwalk": expected_snmp_config}, 200)
    assert output == expected_output
