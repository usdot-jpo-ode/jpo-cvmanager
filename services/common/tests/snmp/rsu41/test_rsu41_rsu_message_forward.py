from unittest.mock import call, patch, Mock
import common.snmp.rsu41.rsu_message_forward as rsu_message_forward
import subprocess


def raise_called_process_error(*args, **kwargs):
    error = subprocess.CalledProcessError(1, cmd=["any"], stderr=b"any\n")
    raise error


@patch("common.snmp.rsu41.rsu_message_forward.subprocess.run")
def test_set_rsu_status_operate(mock_run):
    # mock
    mock_run.return_value = Mock()
    mock_run.return_value.stdout = Mock()
    mock_run.return_value.stdout.decode.return_value = "test_output"
    rsu_ip = "192.168.0.20"
    snmp_creds = {
        "username": "test_username",
        "password": "test_password",
        "encrypt_pw": "test_password",
    }

    # call
    response = rsu_message_forward.set_rsu_status(rsu_ip, snmp_creds, True)

    # check
    mock_run.assert_called_once_with(
        "snmpset -v 3 -t 5 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 RSU-MIB:rsuMode.0 i 4",
        shell=True,
        capture_output=True,
        check=True,
    )
    expected_response = "success"
    assert response == expected_response


@patch("common.snmp.rsu41.rsu_message_forward.subprocess.run")
def test_set_rsu_status_standby(mock_run):
    # mock
    mock_run.return_value = Mock()
    mock_run.return_value.stdout = Mock()
    mock_run.return_value.stdout.decode.return_value = "test_output"
    rsu_ip = "192.168.0.20"
    snmp_creds = {
        "username": "test_username",
        "password": "test_password",
        "encrypt_pw": "test_password",
    }

    # call
    response = rsu_message_forward.set_rsu_status(rsu_ip, snmp_creds, False)

    # check
    mock_run.assert_called_once_with(
        "snmpset -v 3 -t 5 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 RSU-MIB:rsuMode.0 i 2",
        shell=True,
        capture_output=True,
        check=True,
    )
    expected_response = "success"
    assert response == expected_response


@patch("common.snmp.rsu41.rsu_message_forward.set_rsu_status")
def test_config_rsudsrcfwd(mock_set_rsu_status):
    manufacturer = "test_manufacturer"
    udp_port = 1234
    psid = 1
    rsu_index = 1
    rsu_ip = "192.168.0.20"
    dest_ip = "10.0.0.1"
    snmp_creds = {
        "username": "test_username",
        "password": "test_password",
        "encrypt_pw": "test_password",
    }

    # call function
    rsu_message_forward.set(
        rsu_ip, manufacturer, snmp_creds, dest_ip, udp_port, rsu_index, psid
    )

    # check calls
    mock_set_rsu_status.assert_has_calls(
        [
            call(rsu_ip, snmp_creds, operate=False),
            call().__ne__("success"),
            call(rsu_ip, snmp_creds, operate=True),
        ]
    )


@patch("common.snmp.rsu41.rsu_message_forward.set_rsu_status")
@patch("common.snmp.rsu41.rsu_message_forward.subprocess.run")
def test_config_del_rsu41(mock_subprocess_run, mock_set_rsu_status):
    # mock subprocess.run
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    rsu_ip = "192.168.0.20"
    rsu_index = 1
    snmp_creds = {
        "username": "test_username",
        "password": "test_password",
        "encrypt_pw": "test_password",
    }

    # mock set_rsu_status
    mock_set_rsu_status.return_value = "success"

    # prepare args
    msg_type = "test_msg_type"

    # call function
    rsu_message_forward.delete(rsu_ip, snmp_creds, rsu_index)

    # check calls
    mock_set_rsu_status.assert_has_calls(
        [
            call(rsu_ip, snmp_creds, operate=False),
            call(rsu_ip, snmp_creds, operate=True),
        ]
    )
    mock_subprocess_run.assert_called_once()


@patch("common.snmp.rsu41.rsu_message_forward.set_rsu_status", return_value="success")
@patch("common.snmp.rsu41.rsu_message_forward.perform_snmp_mods")
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
    response, code = rsu_message_forward.set(
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


@patch("common.snmp.rsu41.rsu_message_forward.set_rsu_status", return_value="success")
@patch("common.snmp.rsu41.rsu_message_forward.perform_snmp_mods")
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
    response, code = rsu_message_forward.set(
        rsu_ip, manufacturer, snmp_creds, dest_ip, "44920", index, psid, raw
    )

    # Check the result
    assert code == 200
    assert response == "Successfully completed the rsuDsrcFwd SNMPSET configuration"


def raise_called_process_error(*args, **kwargs):
    error = subprocess.CalledProcessError(1, cmd=["any"], stderr=b"any\n")
    raise error


@patch(
    "common.snmp.rsu41.rsu_message_forward.snmpcredential.get_authstring",
    return_value="auth_string",
)
@patch(
    "common.snmp.rsu41.rsu_message_forward.snmperrorcheck.check_error_type",
    return_value="error message",
)
@patch("subprocess.run", side_effect=raise_called_process_error)
def test_set_rsu_status_exception(mock_run, mock_check_error_type, mock_get_authstring):
    # Setup
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password"}
    operate = True

    # Call the function
    result = rsu_message_forward.set_rsu_status(rsu_ip, snmp_creds, operate)
    mock_check_error_type.assert_called_once_with("any")

    # Assert the function result
    assert result == "error message"


@patch("common.snmp.rsu41.rsu_message_forward.subprocess.run")
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


@patch("common.snmp.rsu41.rsu_message_forward.subprocess.run")
@patch(
    "common.snmp.rsu41.rsu_message_forward.snmpcredential.get_authstring",
    return_value="auth_string",
)
def test_rsu41_get(mock_get_authstring, mock_run):
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
    mock_run.assert_called_once()
    mock_get_authstring.assert_called_once_with(snmp_creds)

    # Verify the response
    assert response == ({"RsuFwdSnmpwalk": {}}, 200)


@patch("common.snmp.rsu41.rsu_message_forward.subprocess.run")
def test_rsu41_get_with_snmp_config(mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = (
        'iso.0.15628.4.1.7.1.2.1 = STRING: " "\n' * 15
    )

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
    expected_snmp_config = {"1": {"Message Type": "BSM"}}
    expected_output = ({"RsuFwdSnmpwalk": expected_snmp_config}, 200)
    assert output == expected_output
