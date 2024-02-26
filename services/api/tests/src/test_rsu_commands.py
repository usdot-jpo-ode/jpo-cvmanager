from unittest.mock import MagicMock, patch
import api.src.rsu_commands as rsu_commands

# shared arguments
rsu_ip = ["192.168.0.20"]
args = "test"
rsu_info = {
    "rsu_ip": rsu_ip,
    "manufacturer": "test",
    "snmp_username": "test",
    "snmp_password": "test",
    "snmp_encrypt_pw": None,
    "snmp_version": "test",
    "ssh_username": "test",
    "ssh_password": "test",
}
organization = "test"

### RSU_COMMANDS TESTS ###


def test_rsu_commands_snmpfilter_option_present():
    expected_value = {
        "roles": ["operator", "admin"],
        "ssh_required": True,
        "snmp_required": False,
    }

    assert rsu_commands.command_data["snmpfilter"]["roles"] == expected_value["roles"]
    assert (
        rsu_commands.command_data["snmpfilter"]["ssh_required"]
        == expected_value["ssh_required"]
    )
    assert (
        rsu_commands.command_data["snmpfilter"]["snmp_required"]
        == expected_value["snmp_required"]
    )


@patch("api.src.rsu_commands.rsufwdsnmpwalk.get")
def test_execute_command_rsufwdsnmpwalk(mock_rsufwdsnmpwalk_get):
    # mock
    mock_rsufwdsnmpwalk_get.return_value = "mocked rsufwdsnmpwalk.get"
    rsu_commands.command_data["rsufwdsnmpwalk"]["function"] = mock_rsufwdsnmpwalk_get

    # call
    command = "rsufwdsnmpwalk"
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_rsufwdsnmpwalk_get.assert_called_once()
    expected_result = "mocked rsufwdsnmpwalk.get"
    assert result == expected_result


@patch("api.src.rsu_commands.rsufwdsnmpset.post")
def test_execute_command_rsufwdsnmpset(mock_rsufwdsnmpset_post):
    # mock
    mock_rsufwdsnmpset_post.return_value = "mocked rsufwdsnmpset.post"
    rsu_commands.command_data["rsufwdsnmpset"]["function"] = mock_rsufwdsnmpset_post

    # call
    command = "rsufwdsnmpset"
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_rsufwdsnmpset_post.assert_called_once()
    expected_result = "mocked rsufwdsnmpset.post"
    assert result == expected_result


@patch("api.src.rsu_commands.rsufwdsnmpset.delete")
def test_execute_command_rsufwdsnmpset_del(mock_rsufwdsnmpset_delete):
    # mock
    mock_rsufwdsnmpset_delete.return_value = "mocked rsufwdsnmpset.delete"
    rsu_commands.command_data["rsufwdsnmpset-del"][
        "function"
    ] = mock_rsufwdsnmpset_delete

    # call
    command = "rsufwdsnmpset-del"
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_rsufwdsnmpset_delete.assert_called_once()
    expected_result = "mocked rsufwdsnmpset.delete"
    assert result == expected_result


@patch("api.src.rsu_commands.ssh_commands.reboot")
def test_execute_command_reboot(mock_ssh_commands_reboot):
    # mock
    mock_ssh_commands_reboot.return_value = "mocked ssh_commands.reboot"
    rsu_commands.command_data["reboot"]["function"] = mock_ssh_commands_reboot

    # call
    command = "reboot"
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_ssh_commands_reboot.assert_called_once()
    expected_result = "mocked ssh_commands.reboot"
    assert result == expected_result


@patch("api.src.rsu_commands.ssh_commands.snmpfilter")
def test_execute_command_snmpfilter(mock_ssh_commands_snmpfilter):
    # mock
    mock_ssh_commands_snmpfilter.return_value = "mocked ssh_commands.snmpfilter"
    rsu_commands.command_data["snmpfilter"]["function"] = mock_ssh_commands_snmpfilter

    # call
    command = "snmpfilter"
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_ssh_commands_snmpfilter.assert_called_once()
    expected_result = "mocked ssh_commands.snmpfilter"
    assert result == expected_result


# test queries for RSU manufacturer, SSH credentials, and SNMP credentials


@patch("api.src.rsu_commands.pgquery.query_db")
def test_fetch_rsu_info(mock_query_db):
    # mock
    mock_query_db.return_value = [
        (
            {
                "manufacturer_name": "mocked manufacturer_name",
                "ssh_username": "mocked ssh_username",
                "ssh_password": "mocked ssh_password",
                "snmp_username": "mocked snmp_username",
                "snmp_password": "mocked snmp_password",
                "snmp_encrypt_pw": "mocked snmp_encrypt_pw",
                "snmp_version": "mocked snmp_version",
            },
        ),
    ]

    # call
    result = rsu_commands.fetch_rsu_info(rsu_ip, organization)

    # check
    mock_query_db.assert_called_once()
    expected_result = {
        "manufacturer": "mocked manufacturer_name",
        "ssh_username": "mocked ssh_username",
        "ssh_password": "mocked ssh_password",
        "snmp_username": "mocked snmp_username",
        "snmp_password": "mocked snmp_password",
        "snmp_encrypt_pw": "mocked snmp_encrypt_pw",
        "snmp_version": "mocked snmp_version",
    }
    assert result == expected_result


@patch("api.src.rsu_commands.execute_command")
def test_perform_command_unknown_command(mock_execute_command):
    # call
    command = "unknown-command"
    role = "rsu"
    result = rsu_commands.perform_command(command, organization, role, rsu_ip, args)

    # check
    expected_result = ("Command unknown: unknown-command", 400)
    assert result == expected_result
    mock_execute_command.assert_not_called()


@patch("api.src.rsu_commands.fetch_rsu_info")
@patch("api.src.rsu_commands.execute_command")
def test_perform_command_incomplete_rsu_data(mock_execute_command, mock_fetch_rsu_info):
    # mock
    mock_fetch_rsu_info.return_value = None

    # call
    command = "reboot"
    role = "admin"
    result = rsu_commands.perform_command(command, organization, role, rsu_ip, args)

    # check
    expected_result = (
        "Provided RSU IP does not have complete RSU data for organization: test::192.168.0.20",
        500,
    )
    assert result == expected_result
    mock_execute_command.assert_not_called()


@patch("api.src.rsu_commands.fetch_rsu_info")
@patch("api.src.rsu_commands.execute_command")
def test_perform_command_unauthorized_role(mock_execute_command, mock_fetch_rsu_info):
    # mock
    mock_fetch_rsu_info.return_value = "mocked fetch_rsu_info"

    # call
    command = "reboot"
    role = "rsu"
    result = rsu_commands.perform_command(command, organization, role, rsu_ip, args)

    # check
    expected_result = ("Unauthorized role to run reboot", 401)
    assert result == expected_result
    mock_execute_command.assert_not_called()


# TODO: test RsuCommandRequest class
