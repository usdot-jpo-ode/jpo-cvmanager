from unittest.mock import MagicMock, patch
import src.rsu_commands as rsu_commands

# shared arguments
rsu_ip = '192.168.0.20'
args = 'test'
rsu_info = {
    'rsu_ip': rsu_ip,
    'manufacturer': 'test',
    'snmp_username': 'test',
    'snmp_password': 'test',
    'ssh_username': 'test',
    'ssh_password': 'test'
}
organization = 'test'

### RSU_COMMANDS TESTS ###


def test_rsu_commands_snmpfilter_option_present():
    expected_value = {
        'roles': ['operator', 'admin'],
        'ssh_required': True,
        'snmp_required': False
    }

    assert rsu_commands.command_data['snmpfilter']['roles'] == expected_value['roles']
    assert rsu_commands.command_data['snmpfilter']['ssh_required'] == expected_value['ssh_required']
    assert rsu_commands.command_data['snmpfilter']['snmp_required'] == expected_value['snmp_required']


@patch('src.rsu_commands.rsufwdsnmpwalk.get')
def test_execute_command_rsufwdsnmpwalk(mock_rsufwdsnmpwalk_get):
    # mock
    mock_rsufwdsnmpwalk_get.return_value = 'mocked rsufwdsnmpwalk.get'
    rsu_commands.command_data['rsufwdsnmpwalk']['function'] = mock_rsufwdsnmpwalk_get

    # call
    command = 'rsufwdsnmpwalk'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_rsufwdsnmpwalk_get.assert_called_once()
    expected_result = 'mocked rsufwdsnmpwalk.get'
    assert result == expected_result


@patch('src.rsu_commands.rsufwdsnmpset.post')
def test_execute_command_rsufwdsnmpset(mock_rsufwdsnmpset_post):
    # mock
    mock_rsufwdsnmpset_post.return_value = 'mocked rsufwdsnmpset.post'
    rsu_commands.command_data['rsufwdsnmpset']['function'] = mock_rsufwdsnmpset_post

    # call
    command = 'rsufwdsnmpset'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_rsufwdsnmpset_post.assert_called_once()
    expected_result = 'mocked rsufwdsnmpset.post'
    assert result == expected_result


@patch('src.rsu_commands.rsufwdsnmpset.delete')
def test_execute_command_rsufwdsnmpset_del(mock_rsufwdsnmpset_delete):
    # mock
    mock_rsufwdsnmpset_delete.return_value = 'mocked rsufwdsnmpset.delete'
    rsu_commands.command_data['rsufwdsnmpset-del']['function'] = mock_rsufwdsnmpset_delete

    # call
    command = 'rsufwdsnmpset-del'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_rsufwdsnmpset_delete.assert_called_once()
    expected_result = 'mocked rsufwdsnmpset.delete'
    assert result == expected_result


@patch('src.rsu_commands.ssh_commands.reboot')
def test_execute_command_reboot(mock_ssh_commands_reboot):
    # mock
    mock_ssh_commands_reboot.return_value = 'mocked ssh_commands.reboot'
    rsu_commands.command_data['reboot']['function'] = mock_ssh_commands_reboot

    # call
    command = 'reboot'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_ssh_commands_reboot.assert_called_once()
    expected_result = 'mocked ssh_commands.reboot'
    assert result == expected_result


@patch('src.rsu_commands.ssh_commands.snmpfilter')
def test_execute_command_snmpfilter(mock_ssh_commands_snmpfilter):
    # mock
    mock_ssh_commands_snmpfilter.return_value = 'mocked ssh_commands.snmpfilter'
    rsu_commands.command_data['snmpfilter']['function'] = mock_ssh_commands_snmpfilter

    # call
    command = 'snmpfilter'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_ssh_commands_snmpfilter.assert_called_once()
    expected_result = 'mocked ssh_commands.snmpfilter'
    assert result == expected_result


def test_execute_command_checkforupdates():
    # mock
    mock_command_function = MagicMock(return_value='mocked checkforupdates')
    rsu_commands.command_data['checkforupdates']['function'] = mock_command_function

    # call
    command = 'checkforupdates'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_command_function.assert_called_once()
    expected_result = 'mocked checkforupdates'
    assert result == expected_result

# *****************************
# rsufwdsnmpset.delete
@patch('src.rsufwdsnmpset.delete')
def test_execute_command_osupdate(mock_ssh_commands_delete):
    # mock
    mock_ssh_commands_delete.return_value = 'mocked rsufwdsnmpset.mock_ssh_commands_delete'
    mock_ssh_commands_delete.delete['rsufwdsnmpset-del']['function'] = mock_ssh_commands_delete

    # call
    # command = 'rsufwdsnmpset-del'
    # result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # # check
    # mock_ssh_commands_delete.assert_called_once()
    # expected_result = 'mocked ssh_commands.osupdate'
    # assert result == expected_result






@patch('src.rsu_commands.ssh_commands.osupdate')
def test_execute_command_osupdate(mock_ssh_commands_osupdate):
    # mock
    mock_ssh_commands_osupdate.return_value = 'mocked ssh_commands.osupdate'
    rsu_commands.command_data['osupdate']['function'] = mock_ssh_commands_osupdate

    # call
    command = 'osupdate'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_ssh_commands_osupdate.assert_called_once()
    expected_result = 'mocked ssh_commands.osupdate'
    assert result == expected_result


@patch('src.rsu_commands.ssh_commands.fwupdate')
def test_execute_command_fwupdate(mock_ssh_commands_fwupdate):
    # mock
    mock_ssh_commands_fwupdate.return_value = 'mocked ssh_commands.fwupdate'
    rsu_commands.command_data['fwupdate']['function'] = mock_ssh_commands_fwupdate

    # call
    command = 'fwupdate'
    result = rsu_commands.execute_command(command, rsu_ip, args, rsu_info)

    # check
    mock_ssh_commands_fwupdate.assert_called_once()
    expected_result = 'mocked ssh_commands.fwupdate'
    assert result == expected_result

# test queries for RSU manufacturer, SSH credentials, and SNMP credentials


@patch('src.rsu_commands.pgquery.query_db')
def test_fetch_rsu_info(mock_query_db):
    # mock
    mock_query_db.return_value = [
        {
            "manufacturer_name": "mocked manufacturer_name",
            "ssh_username": "mocked ssh_username",
            "ssh_password": "mocked ssh_password",
            "snmp_username": "mocked snmp_username",
            "snmp_password": "mocked snmp_password"
        }
    ]
    rsu_commands.pgquery.query_db = mock_query_db

    # call
    result = rsu_commands.fetch_rsu_info(rsu_ip, organization)

    # check
    mock_query_db.assert_called_once()
    expected_result = {
        "manufacturer": "mocked manufacturer_name",
        "ssh_username": "mocked ssh_username",
        "ssh_password": "mocked ssh_password",
        "snmp_username": "mocked snmp_username",
        "snmp_password": "mocked snmp_password"
    }
    assert result == expected_result


@patch('src.rsu_commands.execute_command')
@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.rsu_update.check_for_updates')
def test_perform_command_checkforupdates(mock_check_for_updates, mock_fetch_rsu_info, mock_execute_command):
    # mock
    mock_fetch_rsu_info.return_value = {
        "manufacturer": "mocked manufacturer_name",
        "ssh_username": "mocked ssh_username",
        "ssh_password": "mocked ssh_password",
        "snmp_username": "mocked snmp_username",
        "snmp_password": "mocked snmp_password"
    }
    mock_execute_command.return_value = 'mocked execute_command'
    mock_check_for_updates.return_value = 'mocked update_rsu'

    # call
    command = 'checkforupdates'
    role = 'admin'
    result = rsu_commands.perform_command(
        command, organization, role, rsu_ip, args)

    # check
    mock_fetch_rsu_info.assert_called_once()
    mock_execute_command.assert_not_called()
    mock_check_for_updates.assert_called_once()
    expected_result = ('mocked update_rsu', 200)
    assert result == expected_result


@patch('src.rsu_commands.execute_command')
@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.rsu_update.check_for_updates')
@patch('src.rsu_commands.rsu_update.get_os_update_info')
def test_perform_command_osupdate(mock_get_os_update_info, mock_check_for_updates, mock_fetch_rsu_info, mock_execute_command):
    # mock
    mock_fetch_rsu_info.return_value = {
        "manufacturer": "mocked manufacturer_name",
        "ssh_username": "mocked ssh_username",
        "ssh_password": "mocked ssh_password",
        "snmp_username": "mocked snmp_username",
        "snmp_password": "mocked snmp_password"
    }
    mock_execute_command.return_value = 'mocked execute_command'
    mock_check_for_updates.return_value = 'mocked update_rsu'
    mock_get_os_update_info.return_value = {
        "update_available": True,
        "update_type": "os",
        "update_version": "mocked update_version"
    }

    # call
    command = 'osupdate'
    role = 'admin'
    result = rsu_commands.perform_command(
        command, organization, role, rsu_ip, args)

    # check
    mock_fetch_rsu_info.assert_called_once()
    mock_execute_command.assert_called_once()
    mock_check_for_updates.assert_not_called()
    mock_get_os_update_info.assert_called_once()
    expected_result = 'mocked execute_command'
    assert result == expected_result


@patch('src.rsu_commands.execute_command')
@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.rsu_update.check_for_updates')
@patch('src.rsu_commands.rsu_update.get_os_update_info')
@patch('src.rsu_commands.rsu_update.get_firmware_update_info')
def test_perform_command_fwupdate(mock_get_firmware_update_info, mock_get_os_update_info, mock_check_for_updates, mock_fetch_rsu_info, mock_execute_command):
    # mock
    mock_fetch_rsu_info.return_value = {
        "manufacturer": "mocked manufacturer_name",
        "ssh_username": "mocked ssh_username",
        "ssh_password": "mocked ssh_password",
        "snmp_username": "mocked snmp_username",
        "snmp_password": "mocked snmp_password"
    }
    mock_execute_command.return_value = 'mocked execute_command'
    mock_check_for_updates.return_value = 'mocked update_rsu'
    mock_get_os_update_info.return_value = {
        "update_available": True,
        "update_type": "os",
        "update_version": "mocked update_version"
    }
    mock_get_firmware_update_info.return_value = {
        "update_available": True,
        "update_type": "firmware",
        "update_version": "mocked update_version"
    }

    # call
    command = 'fwupdate'
    role = 'admin'
    result = rsu_commands.perform_command(
        command, organization, role, rsu_ip, args)

    # check
    mock_fetch_rsu_info.assert_called_once()
    mock_execute_command.assert_called_once()
    mock_check_for_updates.assert_not_called()
    mock_get_os_update_info.assert_not_called()
    mock_get_firmware_update_info.assert_called_once()
    expected_result = 'mocked execute_command'
    assert result == expected_result


@patch('src.rsu_commands.execute_command')
def test_perform_command_unknown_command(mock_execute_command):
    # call
    command = 'unknown-command'
    role = 'rsu'
    result = rsu_commands.perform_command(
        command, organization, role, rsu_ip, args)

    # check
    expected_result = ('Command unknown: unknown-command', 400)
    assert result == expected_result
    mock_execute_command.assert_not_called()


@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.execute_command')
def test_perform_command_incomplete_rsu_data(mock_execute_command, mock_fetch_rsu_info):
    # mock
    mock_fetch_rsu_info.return_value = None

    # call
    command = 'osupdate'
    role = 'admin'
    result = rsu_commands.perform_command(
        command, organization, role, rsu_ip, args)

    # check
    expected_result = (
        'Provided RSU IP does not have complete RSU data for organization: test::192.168.0.20', 500)
    assert result == expected_result
    mock_execute_command.assert_not_called()


@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.execute_command')
def test_perform_command_unauthorized_role(mock_execute_command, mock_fetch_rsu_info):
    # mock
    mock_fetch_rsu_info.return_value = 'mocked fetch_rsu_info'

    # call
    command = 'osupdate'
    role = 'rsu'
    result = rsu_commands.perform_command(
        command, organization, role, rsu_ip, args)

    # check
    expected_result = ('Unauthorized role to run osupdate', 401)
    assert result == expected_result
    mock_execute_command.assert_not_called()

# TODO: test RsuCommandRequest class
