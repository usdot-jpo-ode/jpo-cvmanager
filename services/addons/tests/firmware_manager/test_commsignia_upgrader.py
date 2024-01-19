from unittest.mock import call, patch, MagicMock
from paramiko import WarningPolicy

from addons.images.firmware_manager.commsignia_upgrader import CommsigniaUpgrader

test_upgrade_info = {
  "ipv4_address": "8.8.8.8",
  "manufacturer": "test-manufacturer",
  "model": "test-model",
  "ssh_username": "test-user",
  "ssh_password": "test-psw",
  "target_firmware_id": 4,
  "target_firmware_version": "1.0.0",
  "install_package": "firmware_package.tar"
}

def test_commsignia_upgrader_init():
  test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
  assert test_commsignia_upgrader.install_package == "firmware_package.tar"
  assert test_commsignia_upgrader.root_path == "/home/8.8.8.8"
  assert test_commsignia_upgrader.blob_name == "test-manufacturer/test-model/1.0.0/firmware_package.tar"
  assert test_commsignia_upgrader.local_file_name == "/home/8.8.8.8/firmware_package.tar"
  assert test_commsignia_upgrader.rsu_ip == "8.8.8.8"
  assert test_commsignia_upgrader.ssh_username == "test-user"
  assert test_commsignia_upgrader.ssh_password == "test-psw"

@patch('addons.images.firmware_manager.commsignia_upgrader.SCPClient')
@patch('addons.images.firmware_manager.commsignia_upgrader.SSHClient')
def test_commsignia_upgrader_upgrade_success(mock_sshclient, mock_scpclient):
  # Mock SSH Client and successful firmware upgrade return value
  sshclient_obj = mock_sshclient.return_value
  _stdout = MagicMock()
  sshclient_obj.exec_command.return_value = MagicMock(), _stdout, MagicMock()
  _stdout.read.return_value.decode.return_value = "ALL OK"

  # Mock SCP Client
  scpclient_obj = mock_scpclient.return_value

  test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
  test_commsignia_upgrader.download_blob = MagicMock()
  test_commsignia_upgrader.cleanup = MagicMock()
  notify = MagicMock()
  test_commsignia_upgrader.notify_firmware_manager = notify

  test_commsignia_upgrader.upgrade()

  # Assert initial SSH connection
  sshclient_obj.set_missing_host_key_policy.assert_called_with(WarningPolicy)
  sshclient_obj.connect.assert_called_with(
    "8.8.8.8", 
    username="test-user", 
    password="test-psw", 
    look_for_keys=False, 
    allow_agent=False
    )

  # Assert SCP file transfer
  mock_scpclient.assert_called_with(sshclient_obj.get_transport())
  scpclient_obj.put.assert_called_with(
    "/home/8.8.8.8/firmware_package.tar",
    remote_path="/tmp/"
  )
  scpclient_obj.close.assert_called_with()

  # Assert SSH firmware upgrade run
  sshclient_obj.exec_command.assert_has_calls(
    [
      call("signedUpgrade.sh /tmp/firmware_package.tar"), 
      call("reboot")
    ]
  )
  sshclient_obj.close.assert_called_with()

  # Assert notified success value
  notify.assert_called_with(success=True)

@patch('addons.images.firmware_manager.commsignia_upgrader.SCPClient')
@patch('addons.images.firmware_manager.commsignia_upgrader.SSHClient')
def test_commsignia_upgrader_upgrade_fail(mock_sshclient, mock_scpclient):
  # Mock SSH Client and failed firmware upgrade return value
  sshclient_obj = mock_sshclient.return_value
  _stdout = MagicMock()
  sshclient_obj.exec_command.return_value = MagicMock(), _stdout, MagicMock()
  _stdout.read.return_value.decode.return_value = "NOT OK TEST"

  # Mock SCP Client
  scpclient_obj = mock_scpclient.return_value

  test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
  test_commsignia_upgrader.download_blob = MagicMock()
  test_commsignia_upgrader.cleanup = MagicMock()
  notify = MagicMock()
  test_commsignia_upgrader.notify_firmware_manager = notify

  test_commsignia_upgrader.upgrade()

  # Assert initial SSH connection
  sshclient_obj.set_missing_host_key_policy.assert_called_with(WarningPolicy)
  sshclient_obj.connect.assert_called_with(
    "8.8.8.8", 
    username="test-user", 
    password="test-psw", 
    look_for_keys=False, 
    allow_agent=False
    )

  # Assert SCP file transfer
  mock_scpclient.assert_called_with(sshclient_obj.get_transport())
  scpclient_obj.put.assert_called_with(
    "/home/8.8.8.8/firmware_package.tar",
    remote_path="/tmp/"
  )
  scpclient_obj.close.assert_called_with()

  # Assert SSH firmware upgrade run
  sshclient_obj.exec_command.assert_has_calls(
    [
      call("signedUpgrade.sh /tmp/firmware_package.tar")
    ]
  )
  sshclient_obj.close.assert_called_with()

  # Assert notified success value
  notify.assert_called_with(success=False)

@patch('addons.images.firmware_manager.commsignia_upgrader.logging')
@patch('addons.images.firmware_manager.commsignia_upgrader.SCPClient')
@patch('addons.images.firmware_manager.commsignia_upgrader.SSHClient')
def test_commsignia_upgrader_upgrade_exception(mock_sshclient, mock_scpclient, mock_logging):
  # Mock SSH Client and failed firmware upgrade return value
  sshclient_obj = mock_sshclient.return_value
  sshclient_obj.connect.side_effect = Exception("Exception occurred during upgrade")

  test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
  test_commsignia_upgrader.download_blob = MagicMock()
  cleanup = MagicMock()
  notify = MagicMock()
  test_commsignia_upgrader.cleanup = cleanup
  test_commsignia_upgrader.notify_firmware_manager = notify

  test_commsignia_upgrader.upgrade()

  # Assert initial SSH connection
  sshclient_obj.set_missing_host_key_policy.assert_called_with(WarningPolicy)
  sshclient_obj.connect.assert_called_with(
    "8.8.8.8", 
    username="test-user", 
    password="test-psw", 
    look_for_keys=False, 
    allow_agent=False
    )

  # Assert SCP file transfer doesn't occur
  mock_scpclient.assert_not_called()

  # Assert SSH firmware upgrade run doesn't occur
  sshclient_obj.exec_command.assert_not_called()

  # Assert exception was cleaned up and firmware manager was notified of upgrade failure
  mock_logging.error.assert_called_with("Failed to perform firmware upgrade: Exception occurred during upgrade")
  cleanup.assert_called_with()
  notify.assert_called_with(success=False)