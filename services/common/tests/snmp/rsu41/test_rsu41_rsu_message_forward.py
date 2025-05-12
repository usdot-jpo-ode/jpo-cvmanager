from unittest.mock import patch, Mock
import common.snmp.rsu41.rsu_message_forward as rsu_message_forward
import subprocess


def raise_called_process_error(*args, **kwargs):
    error = subprocess.CalledProcessError(1, cmd=["any"], stderr=b"any\n")
    raise error


@patch("common.rsufwdsnmpset.set_rsu_status", return_value="success")
@patch("common.rsufwdsnmpset.perform_snmp_mods")
def test_config_rsudsrcfwd_raw_false(mock_perform_snmp_mods, mock_set_rsu_status):
    # Set up test data
    rsu_ip = "192.168.1.1"
    manufacturer = "Commsignia"
    snmp_creds = {"username": "username", "password": "password", "encrypt_pw": None}
    dest_ip = "192.168.1.2"
    index = 1
    psid = "20"
    raw = False

    # Call the function
    response, code = rsufwdsnmpset.config_rsudsrcfwd(
        rsu_ip, manufacturer, snmp_creds, dest_ip, "44920", index, psid, raw
    )

    # Check the result
    assert code == 200, "Unexpected code"
    assert (
        response == "Successfully completed the rsuDsrcFwd SNMPSET configuration"
    ), "Unexpected response"

    # # # Check that set_rsu_status and perform_snmp_mods were called correctly
    mock_set_rsu_status.assert_any_call(rsu_ip, snmp_creds, operate=False)
    mock_set_rsu_status.assert_any_call(rsu_ip, snmp_creds, operate=True)
    mock_perform_snmp_mods.assert_called_once()


@patch("common.rsufwdsnmpset.set_rsu_status", return_value="success")
@patch("common.rsufwdsnmpset.perform_snmp_mods")
def test_config_rsudsrcfwd_raw_true(mock_perform_snmp_mods, mock_set_rsu_status):
    # Set up test data
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password", "encrypt_pw": None}
    dest_ip = "192.168.1.2"
    index = 1
    psid = "20"
    manufacturer = "Commsignia"
    raw = True
    # Call the function
    response, code = rsufwdsnmpset.config_rsudsrcfwd(
        rsu_ip, manufacturer, snmp_creds, dest_ip, "44920", index, psid, raw
    )

    # Check the result
    assert code == 200
    assert response == "Successfully completed the rsuDsrcFwd SNMPSET configuration"


def raise_called_process_error(*args, **kwargs):
    error = subprocess.CalledProcessError(1, cmd=["any"], stderr=b"any\n")
    raise error


@patch("common.rsufwdsnmpset.snmpcredential.get_authstring", return_value="auth_string")
@patch(
    "common.rsufwdsnmpset.snmperrorcheck.check_error_type",
    return_value="error message",
)
@patch("subprocess.run", side_effect=raise_called_process_error)
def test_set_rsu_status_exception(mock_run, mock_check_error_type, mock_get_authstring):
    # Setup
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password"}
    operate = True

    # Call the function
    result = rsufwdsnmpset.set_rsu_status(rsu_ip, snmp_creds, operate)
    mock_check_error_type.assert_called_once_with("any")

    # Assert the function result
    assert result == "error message"


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
@patch(
    "common.snmp.rsu41.rsu_message_forward.snmperrorcheck.check_error_type",
    return_value="error message",
)
def test_rsu41_rsu_message_forward_delete_exception(mock_check_error_type, mock_run):
    # Setup
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password"}
    rsu_index = 1

    # mock subprocess.run to raise CalledProcessError
    mock_error = Mock()
    mock_error.stderr.decode.return_value = "any\n"
    mock_run.return_value = ["hello"]
    mock_run.side_effect = subprocess.CalledProcessError(
        1, cmd=["any"], stderr=b"\n error line"
    )

    # Call the function
    response, code = rsu_message_forward.delete(rsu_ip, snmp_creds, rsu_index)

    # Assert that check_error_type was called with the last line of the error output
    # mock_check_error_type.assert_called_once_with("any")

    # Assert the function result
    assert code == 500
    assert response == "error message"
