from unittest.mock import MagicMock, patch
import src.rsu_commands as rsu_commands
import tests.data.rsu_commands_data as data
from flask import Flask,request,json

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


@patch('src.rsu_commands.execute_command')
def test_fetch_index_rsufwdsnmpwalk_add(mock_execute_command):
    #mock
    # mock_execute_command.return_value = 
    mock_rsu_ip = '192.168.1.1'
    mock_rsu_info = {'manufacturer': 'Yunex'}
    mock_command = 'add'
    mock_message_type = 'BSM'
    mock_data = {
                'RsuFwdSnmpwalk': {
                    'rsuReceivedMsgTable': {
                        '0': {
                            'Message Type': 'BSM',
                            'IP': '192.168.1.2'
                        }
                    },
                    'rsuXmitMsgFwdingTable': {}
                }
            }
    # set the return value of the mocked function
    mock_execute_command.return_value = (mock_data, 200)
    # Action
    result = rsu_commands.fetch_index(mock_command, mock_rsu_ip, mock_rsu_info, mock_message_type)
    # Assert
    mock_execute_command.assert_called_once_with('rsufwdsnmpwalk', mock_rsu_ip, {}, mock_rsu_info)

@patch('src.rsu_commands.execute_command')
def test_fetch_index_rsufwdsnmpwalk_other(mock_execute_command):
    #mock
    # mock_execute_command.return_value = 
    mock_rsu_ip = '192.168.1.1'
    mock_rsu_info = {'manufacturer': 'Yunex'}
    mock_command = 'add'
    mock_message_type = 'OTHER'
    mock_data = {
                'RsuFwdSnmpwalk': {
                    'rsuReceivedMsgTable': {
                        '0': {
                            'Message Type': 'BSM',
                            'IP': '192.168.1.2'
                        }
                    },
                    'rsuXmitMsgFwdingTable': {}
                }
            }
    # set the return value of the mocked function
    mock_execute_command.return_value = (mock_data, 200)
    # Action
    result = rsu_commands.fetch_index(mock_command, mock_rsu_ip, mock_rsu_info, mock_message_type)
    # Assert
    mock_execute_command.assert_called_once_with('rsufwdsnmpwalk', mock_rsu_ip, {}, mock_rsu_info)


@patch('src.rsu_commands.execute_command')
def test_fetch_index_rsufwdsnmpwalk_del(mock_execute_command):
    mock_rsu_ip = '192.168.1.1'
    mock_rsu_info = {'manufacturer': 'Yunex'}
    mock_command = 'del'
    mock_message_type = 'BSM'
    mock_target_ip = '192.168.1.2'
    mock_data = {
        'RsuFwdSnmpwalk': {
            'rsuReceivedMsgTable': {
                '0': {
                    'Message Type': 'BSM',
                    'IP': '192.168.1.2'
                },
                '1': {
                    'Message Type': 'BSM',
                    'IP': '192.168.1.2'
                }
            },
            'rsuXmitMsgFwdingTable': {}
        }
    }
    mock_execute_command.return_value = (mock_data, 200)
    # Action
    result = rsu_commands.fetch_index(mock_command, mock_rsu_ip, mock_rsu_info, mock_message_type,mock_target_ip)
    # Assert
    # assert result == 1, "Expected result to be 1"
    mock_execute_command.assert_called_once_with('rsufwdsnmpwalk', mock_rsu_ip, {}, mock_rsu_info)

@patch('src.rsu_commands.execute_command')
def test_fetch_index_manufacturer_noyunix(mock_execute_command):
    mock_execute_command.return_value = ({'RsuFwdSnmpwalk':{}}, 200)
    rsu_info = {"manufacturer": "NotYunex"}
    # Call the fetch_index function with the test parameters
    index = rsu_commands.fetch_index('command', 'rsu_ip', rsu_info)
    assert index == -1


@patch('src.rsu_commands.execute_command')
def test_fetch_index_manufacturer_test_index(mock_execute_command):
    #checks index = int(entry)  if code ==200 and manufacturer is not Yunex
    mock_execute_command.return_value = ({'RsuFwdSnmpwalk': {'0': {}}}, 200)
    rsu_info = {"manufacturer": "NotYunex"}
    index = rsu_commands.fetch_index('add', 'rsu_ip', rsu_info)
    assert index == 1


@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.fetch_index')
@patch('src.rsu_commands.execute_command')
def test_perform_command_rsufwdsnmpset(mock_execute_command, mock_fetch_index, mock_fetch_rsu_info):
    mock_command = 'rsufwdsnmpset'
    mock_organization = 'TestOrganization'
    mock_role = 'operator'
    mock_rsu_ip = ['192.168.1.1']
    mock_args = {'msg_type': 'TestMsgType'}
    mock_rsu_info = {'manufacturer': 'TestManufacturer'}
    
    # set the return values of the mocked functions
    mock_fetch_rsu_info.return_value = mock_rsu_info
    mock_fetch_index.return_value = 0
    mock_execute_command.return_value = ('Test data', 200)
    # Action
    result = rsu_commands.perform_command(mock_command, mock_organization, mock_role, mock_rsu_ip, mock_args)
    expected_result = {mock_rsu_ip[0]: {'code': 200, 'data': 'Test data'}}
    assert result[0] == expected_result, "Expected result to match expected dictionary"
    assert result[1] == 200, "Expected HTTP status code to be 200"
    mock_fetch_rsu_info.assert_called_once_with(mock_rsu_ip[0], mock_organization)
    mock_fetch_index.assert_called_once_with('add', mock_rsu_ip[0], mock_rsu_info, mock_args['msg_type'])
    mock_execute_command.assert_called_once_with(mock_command, mock_rsu_ip[0], mock_args, mock_rsu_info)

@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.fetch_index')
@patch('src.rsu_commands.execute_command')
def test_perform_command_rsufwdsnmpset_del(mock_execute_command, mock_fetch_index, mock_fetch_rsu_info):
    command = 'rsufwdsnmpset-del'
    organization = 'test_organization'
    role = 'admin'
    rsu_ip = ['192.168.1.1', '192.168.1.2']
    dest_ip ='192.168.1.3'
    args = {'msg_type': 'test_message', 'dest_ip':dest_ip}

    # Mock necessary functions
    mock_fetch_rsu_info.return_value = {'rsu_info': 'some_info'}
    mock_fetch_index.return_value = 1
    mock_execute_command.return_value = ('response_data', 200)

    # Define expected outputs
    expected_output = {
    '192.168.1.1': {'code': 200, 'data': 'response_data'},
    '192.168.1.2': {'code': 200, 'data': 'response_data'}
    }

    # Perform the command
    result_dict, result_status = rsu_commands.perform_command(command, organization, role, rsu_ip, args)
    
    assert result_dict == expected_output
    assert result_status == 200
    mock_fetch_rsu_info.assert_any_call(rsu_ip[0], organization)
    mock_fetch_rsu_info.assert_any_call(rsu_ip[1], organization)
    mock_fetch_index.assert_any_call('del', rsu_ip[0], mock_fetch_rsu_info.return_value, args['msg_type'], dest_ip)
    mock_fetch_index.assert_any_call('del', rsu_ip[1], mock_fetch_rsu_info.return_value, args['msg_type'], dest_ip)
    mock_execute_command.assert_any_call(command, rsu_ip[0], args, mock_fetch_rsu_info.return_value)
    mock_execute_command.assert_any_call(command, rsu_ip[1], args, mock_fetch_rsu_info.return_value)

@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.fetch_index')
@patch('src.rsu_commands.execute_command')
def test_perform_command_with_no_rsu_info_rsufwdsnmpset(mock_execute_command, mock_fetch_index, mock_fetch_rsu_info):
# Define test inputs
    command = 'rsufwdsnmpset'
    organization = 'test_organization'
    role = 'admin'
    rsu_ip = ['192.168.1.1', '192.168.1.2']
    args = {'msg_type': 'test_message'}

    # Mock fetch_rsu_info to return None, simulating no information available for the RSU IP
    mock_fetch_rsu_info.return_value = None

    # Perform the command
    result, status_code = rsu_commands.perform_command(command, organization, role, rsu_ip, args)

    # Define expected outputs
    expected_output = {
        '192.168.1.1': {'code': 400, 'data': f"Provided RSU IP does not have complete RSU data for organization: {organization}::192.168.1.1"},
        '192.168.1.2': {'code': 400, 'data': f"Provided RSU IP does not have complete RSU data for organization: {organization}::192.168.1.2"}
    }
    assert result == expected_output
    assert status_code == 200
    mock_fetch_rsu_info.assert_any_call(rsu_ip[0], organization)
    mock_fetch_rsu_info.assert_any_call(rsu_ip[1], organization)


@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.fetch_index')
@patch('src.rsu_commands.execute_command')
def test_perform_command_rsufwdsnmpset_rsuisnotnone(mock_execute_command, mock_fetch_index, mock_fetch_rsu_info):
    #checks invalidindex for RSU
    mock_fetch_rsu_info.return_value = {"manufacturer": "Yunex"}
    mock_fetch_index.return_value = -1
    # mock_execute_command.return_value = ({"result": "Success"}, 200)
    return_dict, code = rsu_commands.perform_command('rsufwdsnmpset', 'org1', 'admin', ['rsu_ip'], {'msg_type': 'type'})
    # Validate the response
    assert code == 200
    assert return_dict['rsu_ip']['code'] == 400
    assert return_dict['rsu_ip']['data']== "Invalid index for RSU: rsu_ip"


@patch('src.rsu_commands.rsu_update.get_os_update_info')
@patch('src.rsu_commands.fetch_rsu_info')
def test_perform_command_checkforupdates_infonone(mock_fetch_rsu_info, mock_get_os_update_info):
    #checks for checkforupdates and if RSU == None - cant update its OS version, error code 500
    mock_fetch_rsu_info.return_value = {"manufacturer": "Yunex"}
    mock_get_os_update_info.return_value = None

    # Call the perform_command function with the 'osupdate' command
    response, code = rsu_commands.perform_command('osupdate', 'org1', 'admin', ['rsu_ip'], {})

    # Validate the response
    assert code == 500
    assert response == "RSU ['rsu_ip'] cannot update its OS version"


# @patch('src.rsu_update.get_firmware_update_info')
# @patch('src.rsu_commands.rsu_update.get_os_update_info')
# @patch('src.rsu_commands.fetch_rsu_info')
# def test_perform_command_fwupdates_infonone(mock_fetch_rsu_info, mock_get_os_update_info,mock_get_firmware_update_info):
#     #checks for fwUpdate and if RSU == None - cannot update its firmware version, error code 500
#     mock_fetch_rsu_info.return_value = {"manufacturer": "Yunex"}
#     mock_get_os_update_info.return_value = None
#     mock_get_firmware_update_info.return_value = None

#     # Call the perform_command function with the 'fwupdate' command
#     response, code = rsu_commands.perform_command('fwupdate', 'org1', 'admin', ['rsu_ip'], {})

#     # Validate the response
#     assert code == 500
#     assert response == "RSU ['rsu_ip'] cannot update its firmware version"





@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.fetch_index')
@patch('src.rsu_commands.execute_command')
def test_perform_command_with_no_rsu_info_rsufwdsnmpset_del(mock_execute_command, mock_fetch_index, mock_fetch_rsu_info):
    command = 'rsufwdsnmpset-del'
    organization = 'test_organization'
    role = 'admin'
    rsu_ip = ['192.168.1.1', '192.168.1.2']
    args = {'msg_type': 'test_message', 'dest_ip': '192.168.1.3'}

    # Mock fetch_rsu_info to return None, simulating no information available for the RSU IP
    mock_fetch_rsu_info.return_value = None

    # Perform the command
    result, status_code = rsu_commands.perform_command(command, organization, role, rsu_ip, args)

    # Define expected outputs
    expected_output = {
        '192.168.1.1': {'code': 400, 'data': f"Provided RSU IP does not have complete RSU data for organization: {organization}::192.168.1.1"},
        '192.168.1.2': {'code': 400, 'data': f"Provided RSU IP does not have complete RSU data for organization: {organization}::192.168.1.2"}
    }

    # Verify the result
    assert result == expected_output
    assert status_code == 200
    mock_fetch_rsu_info.assert_any_call(rsu_ip[0], organization)
    mock_fetch_rsu_info.assert_any_call(rsu_ip[1], organization)


  



@patch('src.rsu_commands.fetch_rsu_info')
@patch('src.rsu_commands.fetch_index')
@patch('src.rsu_commands.execute_command')
def test_perform_command_with_no_rsu_info_rsufwdsnmpset_del_invaidindex(mock_execute_command, mock_fetch_index, mock_fetch_rsu_info):
    command = 'rsufwdsnmpset-del'
    organization = 'test_organization'
    role = 'admin'
    rsu_ip = ['192.168.1.1', '192.168.1.2']
    args = {'msg_type': 'test_message', 'dest_ip': '192.168.1.3'}
    mock_fetch_index.return_value = -1
    result, status_code = rsu_commands.perform_command(command, organization, role, rsu_ip, args)
    expected_result = {
        '192.168.1.1': {'code': 400, 'data': 'Delete index invalid for RSU: 192.168.1.1'},
        '192.168.1.2': {'code': 400, 'data': 'Delete index invalid for RSU: 192.168.1.2'}
    }
    expected_status_code = 200
    assert result['192.168.1.1']['data'] == expected_result['192.168.1.1']['data']
    assert expected_status_code == status_code
    

def test_options_RsuCommandRequestGet():
    options_headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization,Organization',
    'Access-Control-Allow-Methods': 'GET,POST',
    'Access-Control-Max-Age': '3600'
    }
    request = rsu_commands.RsuCommandRequest()
    empty, responsecode, headers = request.options()
    assert empty == ''
    assert responsecode == 204
    assert headers == options_headers

@patch('src.rsu_commands.perform_command')
def test_get_RsuCommandRequestGet(mockData):
    req = MagicMock()
    req.environ = data.request_environ_good
    req.json = data.request_json_good
    
    mockData.return_value = {"some data"},200

    with patch("src.rsu_commands.request", req):
        info = rsu_commands.RsuCommandRequest()
        (body, code, headers)= info.get()
        mockData.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {"some data"}


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
