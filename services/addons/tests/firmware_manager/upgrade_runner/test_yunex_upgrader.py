from unittest.mock import call, patch, MagicMock, mock_open

from addons.images.firmware_manager.upgrade_runner.yunex_upgrader import YunexUpgrader

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

test_upgrade_info_json = {
    "core": "core-file-name",
    "sdk": "sdk-file-name",
    "provision": "provision-file-name",
}


def test_yunex_upgrader_init():
    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    assert test_yunex_upgrader.install_package == "firmware_package.tar"
    assert test_yunex_upgrader.root_path == "/home/8.8.8.8"
    assert (
        test_yunex_upgrader.blob_name
        == "test-manufacturer/test-model/1.0.0/firmware_package.tar"
    )
    assert test_yunex_upgrader.local_file_name == "/home/8.8.8.8/firmware_package.tar"
    assert test_yunex_upgrader.rsu_ip == "8.8.8.8"
    assert test_yunex_upgrader.ssh_username == "test-user"
    assert test_yunex_upgrader.ssh_password == "test-psw"


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.subprocess")
def test_yunex_upgrader_run_xfer_upgrade_success(mock_subprocess, mock_logging):
    run_response_obj = MagicMock()
    run_response_obj.returncode = 0
    stdout_obj = MagicMock()
    stdout_obj.decode.return_value = '\ntest\ntest\nTEXT: {"success":{"upload":"Processing OK. Rebooting now ..."}}\ntest\n'
    run_response_obj.stdout = stdout_obj
    run_response_obj.stderr = MagicMock()
    mock_subprocess.run.return_value = run_response_obj

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    code = test_yunex_upgrader.run_xfer_upgrade("core-file-name")

    assert code == 0

    # Assert logging
    mock_logging.info.assert_not_called()
    mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.subprocess")
def test_yunex_upgrader_run_xfer_upgrade_fail_code(mock_subprocess, mock_logging):
    run_response_obj = MagicMock()
    run_response_obj.returncode = 2
    run_response_obj.stdout = MagicMock()
    run_response_obj.stderr = MagicMock()
    run_response_obj.stderr.decode.return_value = "test-error"
    mock_subprocess.run.return_value = run_response_obj

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    code = test_yunex_upgrader.run_xfer_upgrade("core-file-name")

    assert code == -1

    # Assert logging
    mock_logging.info.assert_not_called()
    mock_logging.error.assert_called_with(
        "Firmware not successful for 8.8.8.8: test-error"
    )


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.subprocess")
def test_yunex_upgrader_run_xfer_upgrade_fail_output(mock_subprocess, mock_logging):
    run_response_obj = MagicMock()
    run_response_obj.returncode = 0
    stdout_obj = MagicMock()
    stdout_obj.decode.return_value = "\ntest\ntest\ntest\n"
    run_response_obj.stdout = stdout_obj
    run_response_obj.stderr = MagicMock()
    run_response_obj.stderr.decode.return_value = "test-error"
    mock_subprocess.run.return_value = run_response_obj

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    code = test_yunex_upgrader.run_xfer_upgrade("core-file-name")

    assert code == -1

    # Assert logging
    mock_logging.info.assert_not_called()
    mock_logging.error.assert_called_with(
        "Firmware not successful for 8.8.8.8: test-error"
    )


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.json")
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch(
    "addons.images.firmware_manager.upgrade_runner.yunex_upgrader.tarfile.open",
    return_value=MagicMock(),
)
def test_yunex_upgrader_upgrade_success(
    mock_tarfile_open, mock_open, mock_json, mock_time, mock_logging
):
    taropen_obj = mock_tarfile_open.return_value.__enter__.return_value
    mock_json.load.return_value = test_upgrade_info_json

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    test_yunex_upgrader.check_online = MagicMock(return_value=True)
    test_yunex_upgrader.download_blob = MagicMock()
    test_yunex_upgrader.run_xfer_upgrade = MagicMock(return_value=0)
    test_yunex_upgrader.wait_until_online = MagicMock(return_value=0)
    test_yunex_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_yunex_upgrader.notify_firmware_manager = notify

    test_yunex_upgrader.upgrade()

    # Assert notified success value
    mock_tarfile_open.assert_called_with("/home/8.8.8.8/firmware_package.tar", "r")
    taropen_obj.extractall.assert_called_with("/home/8.8.8.8")

    mock_open.assert_called_with("/home/8.8.8.8/upgrade_info.json")
    mock_json.load.assert_called_with(mock_open.return_value)

    test_yunex_upgrader.run_xfer_upgrade.assert_has_calls(
        [
            call("/home/8.8.8.8/core-file-name"),
            call("/home/8.8.8.8/sdk-file-name"),
            call("/home/8.8.8.8/provision-file-name"),
        ]
    )
    assert test_yunex_upgrader.wait_until_online.call_count == 2
    assert mock_time.sleep.call_count == 2

    # Assert notified success value
    notify.assert_called_with(success=True)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Unpacking TAR file prior to upgrading 8.8.8.8..."),
            call("Running Core firmware upgrade for 8.8.8.8..."),
            call("Running SDK firmware upgrade for 8.8.8.8..."),
            call("Running application provisioning for 8.8.8.8..."),
        ]
    )
    mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.json")
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch(
    "addons.images.firmware_manager.upgrade_runner.yunex_upgrader.tarfile.open",
    return_value=MagicMock(),
)
def test_yunex_upgrader_core_upgrade_fail(
    mock_tarfile_open, mock_open, mock_json, mock_time, mock_logging
):
    taropen_obj = mock_tarfile_open.return_value.__enter__.return_value
    mock_json.load.return_value = test_upgrade_info_json

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    test_yunex_upgrader.check_online = MagicMock(return_value=True)
    test_yunex_upgrader.download_blob = MagicMock()
    test_yunex_upgrader.run_xfer_upgrade = MagicMock(return_value=-1)
    test_yunex_upgrader.wait_until_online = MagicMock(return_value=0)
    test_yunex_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_yunex_upgrader.notify_firmware_manager = notify

    test_yunex_upgrader.upgrade()

    # Assert notified success value
    mock_tarfile_open.assert_called_with("/home/8.8.8.8/firmware_package.tar", "r")
    taropen_obj.extractall.assert_called_with("/home/8.8.8.8")

    mock_open.assert_called_with("/home/8.8.8.8/upgrade_info.json")
    mock_json.load.assert_called_with(mock_open.return_value)

    test_yunex_upgrader.run_xfer_upgrade.assert_called_with(
        "/home/8.8.8.8/core-file-name"
    )
    assert test_yunex_upgrader.wait_until_online.call_count == 0
    assert mock_time.sleep.call_count == 0

    # Assert notified success value
    notify.assert_called_with(success=False)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Unpacking TAR file prior to upgrading 8.8.8.8..."),
            call("Running Core firmware upgrade for 8.8.8.8..."),
        ]
    )
    mock_logging.error.assert_called_with(
        "Failed to perform firmware upgrade for 8.8.8.8: Yunex RSU Core upgrade failed"
    )


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.json")
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch(
    "addons.images.firmware_manager.upgrade_runner.yunex_upgrader.tarfile.open",
    return_value=MagicMock(),
)
def test_yunex_upgrader_core_ping_fail(
    mock_tarfile_open, mock_open, mock_json, mock_time, mock_logging
):
    taropen_obj = mock_tarfile_open.return_value.__enter__.return_value
    mock_json.load.return_value = test_upgrade_info_json

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    test_yunex_upgrader.check_online = MagicMock(return_value=True)
    test_yunex_upgrader.download_blob = MagicMock()
    test_yunex_upgrader.run_xfer_upgrade = MagicMock(return_value=0)
    test_yunex_upgrader.wait_until_online = MagicMock(return_value=-1)
    test_yunex_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_yunex_upgrader.notify_firmware_manager = notify

    test_yunex_upgrader.upgrade()

    # Assert notified success value
    mock_tarfile_open.assert_called_with("/home/8.8.8.8/firmware_package.tar", "r")
    taropen_obj.extractall.assert_called_with("/home/8.8.8.8")

    mock_open.assert_called_with("/home/8.8.8.8/upgrade_info.json")
    mock_json.load.assert_called_with(mock_open.return_value)

    test_yunex_upgrader.run_xfer_upgrade.assert_called_with(
        "/home/8.8.8.8/core-file-name"
    )
    assert test_yunex_upgrader.wait_until_online.call_count == 1
    assert mock_time.sleep.call_count == 0

    # Assert notified success value
    notify.assert_called_with(success=False)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Unpacking TAR file prior to upgrading 8.8.8.8..."),
            call("Running Core firmware upgrade for 8.8.8.8..."),
        ]
    )
    mock_logging.error.assert_called_with(
        "Failed to perform firmware upgrade for 8.8.8.8: RSU offline for too long after Core upgrade"
    )


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.json")
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch(
    "addons.images.firmware_manager.upgrade_runner.yunex_upgrader.tarfile.open",
    return_value=MagicMock(),
)
def test_yunex_upgrader_sdk_upgrade_fail(
    mock_tarfile_open, mock_open, mock_json, mock_time, mock_logging
):
    taropen_obj = mock_tarfile_open.return_value.__enter__.return_value
    mock_json.load.return_value = test_upgrade_info_json

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    test_yunex_upgrader.check_online = MagicMock(return_value=True)
    test_yunex_upgrader.download_blob = MagicMock()
    test_yunex_upgrader.run_xfer_upgrade = MagicMock(side_effect=[0, -1])
    test_yunex_upgrader.wait_until_online = MagicMock(return_value=0)
    test_yunex_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_yunex_upgrader.notify_firmware_manager = notify

    test_yunex_upgrader.upgrade()

    # Assert notified success value
    mock_tarfile_open.assert_called_with("/home/8.8.8.8/firmware_package.tar", "r")
    taropen_obj.extractall.assert_called_with("/home/8.8.8.8")

    mock_open.assert_called_with("/home/8.8.8.8/upgrade_info.json")
    mock_json.load.assert_called_with(mock_open.return_value)

    test_yunex_upgrader.run_xfer_upgrade.assert_has_calls(
        [call("/home/8.8.8.8/core-file-name"), call("/home/8.8.8.8/sdk-file-name")]
    )
    assert test_yunex_upgrader.wait_until_online.call_count == 1
    assert mock_time.sleep.call_count == 1

    # Assert notified success value
    notify.assert_called_with(success=False)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Unpacking TAR file prior to upgrading 8.8.8.8..."),
            call("Running Core firmware upgrade for 8.8.8.8..."),
            call("Running SDK firmware upgrade for 8.8.8.8..."),
        ]
    )
    mock_logging.error.assert_called_with(
        "Failed to perform firmware upgrade for 8.8.8.8: Yunex RSU SDK upgrade failed"
    )


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.json")
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch(
    "addons.images.firmware_manager.upgrade_runner.yunex_upgrader.tarfile.open",
    return_value=MagicMock(),
)
def test_yunex_upgrader_sdk_ping_fail(
    mock_tarfile_open, mock_open, mock_json, mock_time, mock_logging
):
    taropen_obj = mock_tarfile_open.return_value.__enter__.return_value
    mock_json.load.return_value = test_upgrade_info_json

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    test_yunex_upgrader.check_online = MagicMock(return_value=True)
    test_yunex_upgrader.download_blob = MagicMock()
    test_yunex_upgrader.run_xfer_upgrade = MagicMock(return_value=0)
    test_yunex_upgrader.wait_until_online = MagicMock(side_effect=[0, -1])
    test_yunex_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_yunex_upgrader.notify_firmware_manager = notify

    test_yunex_upgrader.upgrade()

    # Assert notified success value
    mock_tarfile_open.assert_called_with("/home/8.8.8.8/firmware_package.tar", "r")
    taropen_obj.extractall.assert_called_with("/home/8.8.8.8")

    mock_open.assert_called_with("/home/8.8.8.8/upgrade_info.json")
    mock_json.load.assert_called_with(mock_open.return_value)

    test_yunex_upgrader.run_xfer_upgrade.assert_has_calls(
        [call("/home/8.8.8.8/core-file-name"), call("/home/8.8.8.8/sdk-file-name")]
    )
    assert test_yunex_upgrader.wait_until_online.call_count == 2
    assert mock_time.sleep.call_count == 1

    # Assert notified success value
    notify.assert_called_with(success=False)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Unpacking TAR file prior to upgrading 8.8.8.8..."),
            call("Running Core firmware upgrade for 8.8.8.8..."),
            call("Running SDK firmware upgrade for 8.8.8.8..."),
        ]
    )
    mock_logging.error.assert_called_with(
        "Failed to perform firmware upgrade for 8.8.8.8: RSU offline for too long after SDK upgrade"
    )


@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.yunex_upgrader.json")
@patch("builtins.open", new_callable=mock_open, read_data="data")
@patch(
    "addons.images.firmware_manager.upgrade_runner.yunex_upgrader.tarfile.open",
    return_value=MagicMock(),
)
def test_yunex_upgrader_provision_upgrade_fail(
    mock_tarfile_open, mock_open, mock_json, mock_time, mock_logging
):
    taropen_obj = mock_tarfile_open.return_value.__enter__.return_value
    mock_json.load.return_value = test_upgrade_info_json

    test_yunex_upgrader = YunexUpgrader(test_upgrade_info)
    test_yunex_upgrader.check_online = MagicMock(return_value=True)
    test_yunex_upgrader.download_blob = MagicMock()
    test_yunex_upgrader.run_xfer_upgrade = MagicMock(side_effect=[0, 0, -1])
    test_yunex_upgrader.wait_until_online = MagicMock(return_value=0)
    test_yunex_upgrader.cleanup = MagicMock()
    notify = MagicMock()
    test_yunex_upgrader.notify_firmware_manager = notify

    test_yunex_upgrader.upgrade()

    # Assert notified success value
    mock_tarfile_open.assert_called_with("/home/8.8.8.8/firmware_package.tar", "r")
    taropen_obj.extractall.assert_called_with("/home/8.8.8.8")

    mock_open.assert_called_with("/home/8.8.8.8/upgrade_info.json")
    mock_json.load.assert_called_with(mock_open.return_value)

    test_yunex_upgrader.run_xfer_upgrade.assert_has_calls(
        [
            call("/home/8.8.8.8/core-file-name"),
            call("/home/8.8.8.8/sdk-file-name"),
            call("/home/8.8.8.8/provision-file-name"),
        ]
    )
    assert test_yunex_upgrader.wait_until_online.call_count == 2
    assert mock_time.sleep.call_count == 2

    # Assert notified success value
    notify.assert_called_with(success=False)

    # Assert logging
    mock_logging.info.assert_has_calls(
        [
            call("Unpacking TAR file prior to upgrading 8.8.8.8..."),
            call("Running Core firmware upgrade for 8.8.8.8..."),
            call("Running SDK firmware upgrade for 8.8.8.8..."),
            call("Running application provisioning for 8.8.8.8..."),
        ]
    )
    mock_logging.error.assert_called_with(
        "Failed to perform firmware upgrade for 8.8.8.8: Yunex RSU application provisioning upgrade failed"
    )
