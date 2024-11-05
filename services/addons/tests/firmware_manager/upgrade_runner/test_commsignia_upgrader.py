from unittest.mock import call, patch, MagicMock
from paramiko import WarningPolicy

from addons.images.firmware_manager.upgrade_runner.commsignia_upgrader import (
    CommsigniaUpgrader,
)

test_upgrade_info = {
    "ipv4_address": "8.8.8.8",
    "manufacturer": "test-manufacturer",
    "model": "test-model",
    "ssh_username": "test-user",
    "ssh_password": "test-psw",
    "target_firmware_id": 4,
    "target_firmware_version": "1.0.0",
    "install_package": "firmware_package.tar",
}


def test_commsignia_upgrader_init():
    test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
    assert test_commsignia_upgrader.install_package == "firmware_package.tar"
    assert test_commsignia_upgrader.root_path == "/home/8.8.8.8"
    assert (
        test_commsignia_upgrader.blob_name
        == "test-manufacturer/test-model/1.0.0/firmware_package.tar"
    )
    assert (
        test_commsignia_upgrader.local_file_name == "/home/8.8.8.8/firmware_package.tar"
    )
    assert test_commsignia_upgrader.rsu_ip == "8.8.8.8"
    assert test_commsignia_upgrader.ssh_username == "test-user"
    assert test_commsignia_upgrader.ssh_password == "test-psw"


@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SCPClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SSHClient")
def test_commsignia_upgrader_upgrade_success_no_post_update(
    mock_sshclient, mock_scpclient, mock_logging
):
    # Mock SSH Client and successful firmware upgrade return value
    sshclient_obj = mock_sshclient.return_value
    _stdout = MagicMock()
    sshclient_obj.exec_command.return_value = MagicMock(), _stdout, MagicMock()
    _stdout.read.return_value.decode.return_value = "ALL OK"

    # Mock SCP Client
    scpclient_obj = mock_scpclient.return_value

    test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
    test_commsignia_upgrader.check_online = MagicMock(return_value=True)
    test_commsignia_upgrader.download_blob = MagicMock(return_value=False)
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
        allow_agent=False,
    )

    # Assert SCP file transfer
    mock_scpclient.assert_called_with(sshclient_obj.get_transport())
    scpclient_obj.put.assert_called_with(
        "/home/8.8.8.8/firmware_package.tar", remote_path="/tmp/"
    )
    scpclient_obj.close.assert_called_with()

    # Assert SSH firmware upgrade run
    sshclient_obj.exec_command.assert_has_calls(
        [call("signedUpgrade.sh /tmp/firmware_package.tar"), call("reboot")]
    )
    sshclient_obj.close.assert_called_with()

    # Assert notified success value
    notify.assert_called_with(success=True)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Making SSH connection with 8.8.8.8..."),
            call("Copying installation package to 8.8.8.8..."),
            call("Running firmware upgrade for 8.8.8.8..."),
            call("ALL OK"),
        ]
    )
    mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SCPClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SSHClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.time")
def test_commsignia_upgrader_upgrade_success_post_update(
    mock_time, mock_sshclient, mock_scpclient, mock_logging
):
    # Mock SSH Client and successful firmware upgrade return value
    sshclient_obj = mock_sshclient.return_value
    _stdout = MagicMock()
    sshclient_obj.exec_command.return_value = MagicMock(), _stdout, MagicMock()
    _stdout.read.return_value.decode.return_value = "ALL OK"

    # Mock SCP Client
    scpclient_obj = mock_scpclient.return_value

    test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
    test_commsignia_upgrader.check_online = MagicMock(return_value=True)
    test_commsignia_upgrader.download_blob = MagicMock(return_value=True)
    test_commsignia_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_commsignia_upgrader.notify_firmware_manager = notify
    test_commsignia_upgrader.wait_until_online = MagicMock(return_value=0)

    # Mock time.sleep to avoid waiting during test
    mock_time.sleep = MagicMock(return_value=None)

    test_commsignia_upgrader.upgrade()

    # Assert initial SSH connection
    sshclient_obj.set_missing_host_key_policy.assert_called_with(WarningPolicy)
    sshclient_obj.connect.assert_called_with(
        "8.8.8.8",
        username="test-user",
        password="test-psw",
        look_for_keys=False,
        allow_agent=False,
    )

    # Assert SCP file transfer
    mock_scpclient.assert_called_with(sshclient_obj.get_transport())
    scpclient_obj.put.assert_called_with(
        "/home/8.8.8.8/post_upgrade.sh", remote_path="/tmp/"
    )
    scpclient_obj.close.assert_called_with()

    # Assert SSH firmware upgrade run
    sshclient_obj.exec_command.assert_has_calls(
        [
            call("signedUpgrade.sh /tmp/firmware_package.tar"),
            call("reboot"),
            call("chmod +x /tmp/post_upgrade.sh"),
            call("/tmp/post_upgrade.sh"),
        ]
    )
    sshclient_obj.close.assert_called_with()

    # Assert notified success value
    notify.assert_called_with(success=True)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Making SSH connection with 8.8.8.8..."),
            call("Copying installation package to 8.8.8.8..."),
            call("Running firmware upgrade for 8.8.8.8..."),
            call("ALL OK"),
            call("Making SSH connection with 8.8.8.8..."),
            call("Copying post upgrade script to 8.8.8.8..."),
            call("Running post upgrade script for 8.8.8.8..."),
            call("ALL OK"),
            call("Post upgrade script executed successfully for rsu: 8.8.8.8."),
        ]
    )
    mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SCPClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SSHClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.logging")
def test_commsignia_upgrader_upgrade_post_update_fail(
    mock_logging, mock_time, mock_sshclient, mock_scpclient
):
    # Mock SSH Client and successful firmware upgrade return value
    sshclient_obj = mock_sshclient.return_value
    _stdout = MagicMock()
    sshclient_obj.exec_command.return_value = MagicMock(), _stdout, MagicMock()
    _stdout.read.return_value.decode = MagicMock(side_effect=["ALL OK", "NOT OK TEST"])

    # Mock SCP Client
    scpclient_obj = mock_scpclient.return_value

    test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
    test_commsignia_upgrader.check_online = MagicMock(return_value=True)
    test_commsignia_upgrader.download_blob = MagicMock(return_value=True)
    test_commsignia_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_commsignia_upgrader.notify_firmware_manager = notify
    test_commsignia_upgrader.wait_until_online = MagicMock(return_value=0)

    # Mock time.sleep to avoid waiting during test
    mock_time.sleep = MagicMock(return_value=None)

    # Mock logging.error to check for expected error message
    mock_logging.error = MagicMock()

    test_commsignia_upgrader.upgrade()

    # Assert initial SSH connection
    sshclient_obj.set_missing_host_key_policy.assert_called_with(WarningPolicy)
    sshclient_obj.connect.assert_called_with(
        "8.8.8.8",
        username="test-user",
        password="test-psw",
        look_for_keys=False,
        allow_agent=False,
    )

    # Assert SCP file transfer
    mock_scpclient.assert_called_with(sshclient_obj.get_transport())
    scpclient_obj.put.assert_called_with(
        "/home/8.8.8.8/post_upgrade.sh", remote_path="/tmp/"
    )
    scpclient_obj.close.assert_called_with()

    # Assert SSH firmware upgrade run
    sshclient_obj.exec_command.assert_has_calls(
        [
            call("signedUpgrade.sh /tmp/firmware_package.tar"),
            call("reboot"),
            call("chmod +x /tmp/post_upgrade.sh"),
            call("/tmp/post_upgrade.sh"),
        ]
    )
    sshclient_obj.close.assert_called_with()

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Making SSH connection with 8.8.8.8..."),
            call("Copying installation package to 8.8.8.8..."),
            call("Running firmware upgrade for 8.8.8.8..."),
            call("ALL OK"),
            call("Making SSH connection with 8.8.8.8..."),
            call("Copying post upgrade script to 8.8.8.8..."),
            call("Running post upgrade script for 8.8.8.8..."),
            call("NOT OK TEST"),
        ]
    )
    mock_logging.error.assert_called_with(
        "Failed to execute post upgrade script for rsu 8.8.8.8: NOT OK TEST"
    )

    # Assert notified success value
    notify.assert_called_with(success=True)


@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SCPClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SSHClient")
def test_commsignia_upgrader_upgrade_fail(mock_sshclient, mock_scpclient, mock_logging):
    # Mock SSH Client and failed firmware upgrade return value
    sshclient_obj = mock_sshclient.return_value
    _stdout = MagicMock()
    sshclient_obj.exec_command.return_value = MagicMock(), _stdout, MagicMock()
    _stdout.read.return_value.decode.return_value = "NOT OK TEST"

    # Mock SCP Client
    scpclient_obj = mock_scpclient.return_value

    test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
    test_commsignia_upgrader.check_online = MagicMock(return_value=True)
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
        allow_agent=False,
    )

    # Assert SCP file transfer
    mock_scpclient.assert_called_with(sshclient_obj.get_transport())
    scpclient_obj.put.assert_called_with(
        "/home/8.8.8.8/firmware_package.tar", remote_path="/tmp/"
    )
    scpclient_obj.close.assert_called_with()

    # Assert SSH firmware upgrade run
    sshclient_obj.exec_command.assert_has_calls(
        [call("signedUpgrade.sh /tmp/firmware_package.tar")]
    )
    sshclient_obj.close.assert_called_with()

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Making SSH connection with 8.8.8.8..."),
            call("Copying installation package to 8.8.8.8..."),
            call("Running firmware upgrade for 8.8.8.8..."),
            call("NOT OK TEST"),
        ]
    )
    mock_logging.error.assert_not_called()

    # Assert notified success value
    notify.assert_called_with(success=False)


@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SCPClient")
@patch("addons.images.firmware_manager.upgrade_runner.commsignia_upgrader.SSHClient")
def test_commsignia_upgrader_upgrade_exception(
    mock_sshclient, mock_scpclient, mock_logging
):
    # Mock SSH Client and failed firmware upgrade return value
    sshclient_obj = mock_sshclient.return_value
    sshclient_obj.connect.side_effect = Exception("Exception occurred during upgrade")

    test_commsignia_upgrader = CommsigniaUpgrader(test_upgrade_info)
    test_commsignia_upgrader.check_online = MagicMock(return_value=True)
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
        allow_agent=False,
    )

    # Assert SCP file transfer doesn't occur
    mock_scpclient.assert_not_called()

    # Assert SSH firmware upgrade run doesn't occur
    sshclient_obj.exec_command.assert_not_called()

    # Assert logging
    mock_logging.info.assert_called_with("Making SSH connection with 8.8.8.8...")
    mock_logging.error.assert_called_with(
        "Failed to perform firmware upgrade for 8.8.8.8: Exception occurred during upgrade"
    )

    # Assert exception was cleaned up and firmware manager was notified of upgrade failure
    cleanup.assert_called_with()
    notify.assert_called_with(success=False)
