from unittest.mock import call, MagicMock, patch

import api.src.ssh_commands as ssh_commands

# shared variables
mock_reboot_request = {
    "rsu_ip": "192.168.0.20",
    "creds": {"username": "username", "password": "password"},
}

mock_snmp_filter_request = {
    "rsu_ip": "8.8.8.8",
    "manufacturer": "Commsignia",
    "creds": {"username": "username", "password": "password"},
}


# ### REBOOT TESTS ###
@patch("api.src.ssh_commands.Connection")
def test_reboot(mock_connection):
    # mock
    mock_connection.return_value.__enter__.return_value = MagicMock()

    # run test
    result = ssh_commands.reboot(mock_reboot_request)

    # verify
    mock_connection.assert_called_once_with(
        "192.168.0.20",
        user="username",
        config=ssh_commands.Config(overrides={"sudo": {"password": "password"}}),
        connect_kwargs={"password": "password"},
    )
    mock_connection.return_value.__enter__.return_value.sudo.assert_called_once_with(
        "reboot"
    )
    assert result == ("succeeded", 200)


@patch("api.src.ssh_commands.Connection")
def test_reboot_exception(mock_connection):
    # mock
    mock_connection.return_value.__enter__.return_value = MagicMock()
    mock_connection.return_value.__enter__.return_value.sudo.side_effect = Exception(
        "test"
    )

    # run test
    result = ssh_commands.reboot(mock_reboot_request)

    # verify
    mock_connection.assert_called_once_with(
        "192.168.0.20",
        user="username",
        config=ssh_commands.Config(overrides={"sudo": {"password": "password"}}),
        connect_kwargs={"password": "password"},
    )
    mock_connection.return_value.__enter__.return_value.sudo.assert_called_once_with(
        "reboot"
    )
    assert result == ("failed", 500)


# ## SNMPFILTER TESTS ###
def test_snmpfilter_not_commsignia():
    # run test
    resp = ssh_commands.snmpfilter({"manufacturer": "test"})

    # verify
    assert resp == ("Target RSU is not of type Commsignia", 400)


@patch("api.src.ssh_commands.Connection.run")
def test_snmp_filter_resp(mock_conn_run):
    # mock
    mock_conn_run.return_value = MagicMock()
    mock_conn_run.return_value.stdout = "test1\ntest2\n"

    # run test
    resp = ssh_commands.snmpfilter(mock_snmp_filter_request)

    # verify
    assert resp == ("filter applied successfully", 200)


@patch("api.src.ssh_commands.Connection.run")
def test_snmp_filter_run_no_reboot(mock_conn_run):
    # run test
    resp = ssh_commands.snmpfilter(mock_snmp_filter_request)

    # verify
    assert resp == ("filter applied successfully", 200)
    calls = [
        call(
            "grep -rwl /rwdata/etc/data_logger_ftw -e '\"value\":3758096407'",
            warn=True,
            hide=True,
        ),
        call(
            "grep -rwl /rwdata/etc/data_logger_ftw -e '\"value\":32770'",
            warn=True,
            hide=True,
        ),
    ]
    mock_conn_run.assert_has_calls(calls, any_order=True)


@patch("api.src.ssh_commands.Connection.run")
def test_snmp_filter_run_with_reboot(mock_conn_run):
    # mock
    mock_conn_run.return_value = MagicMock()
    mock_conn_run.return_value.stdout = "test1\ntest2\n"

    # run test
    resp = ssh_commands.snmpfilter(mock_snmp_filter_request)

    # verify
    expected_response = ("filter applied successfully", 200)
    assert resp == expected_response
    calls = [
        call(
            "grep -rwl /rwdata/etc/data_logger_ftw -e '\"value\":3758096407'",
            warn=True,
            hide=True,
        ),
        call(
            "grep -rwl /rwdata/etc/data_logger_ftw -e '\"value\":32770'",
            warn=True,
            hide=True,
        ),
        call('sed -i \'s/"direction":"both"/"direction":"out"/g\' test1', hide=True),
        call('sed -i \'s/"direction":"both"/"direction":"out"/g\' test2', hide=True),
        call("reboot"),
    ]
    mock_conn_run.assert_has_calls(calls, any_order=True)


@patch("api.src.ssh_commands.Connection.run")
@patch("api.src.ssh_commands.logging")
def test_snmpfilter_error(mock_logging, mock_conn_run):
    # mock
    mock_conn_run.side_effect = Exception("mocked error")

    # run test
    resp = ssh_commands.snmpfilter(mock_snmp_filter_request)

    # verify
    mock_logging.error.assert_called_once_with("Encountered an error: mocked error")
    assert resp == ("filter failed to be applied", 500)
