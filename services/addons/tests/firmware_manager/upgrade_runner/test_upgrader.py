from unittest.mock import MagicMock, patch
import os
import pytest

from addons.images.firmware_manager.upgrade_runner import upgrader
from addons.images.firmware_manager.upgrade_runner.upgrader import (
    StorageProviderNotSupportedException,
)


# Test class for testing the abstract class
class TestUpgrader(upgrader.UpgraderAbstractClass):
    # Prevent Pytest from trying to scan class since it begins with "Test"
    __test__ = False

    def __init__(self, upgrade_info):
        super().__init__(upgrade_info, "")

    def upgrade(self):
        super().upgrade()


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


def test_upgrader_init():
    test_upgrader = TestUpgrader(test_upgrade_info)
    assert test_upgrader.install_package == "firmware_package.tar"
    assert test_upgrader.root_path == "/home/8.8.8.8"
    assert (
        test_upgrader.blob_name
        == "test-manufacturer/test-model/1.0.0/firmware_package.tar"
    )
    assert test_upgrader.local_file_name == "/home/8.8.8.8/firmware_package.tar"
    assert test_upgrader.rsu_ip == "8.8.8.8"
    assert test_upgrader.ssh_username == "test-user"
    assert test_upgrader.ssh_password == "test-psw"


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.shutil")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.Path")
def test_cleanup_exists(mock_Path, mock_shutil):
    mock_path_obj = mock_Path.return_value
    mock_path_obj.exists.return_value = True
    mock_path_obj.is_dir.return_value = True
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.cleanup()

    mock_Path.assert_called_with("/home/8.8.8.8")
    mock_shutil.rmtree.assert_called_with(mock_path_obj)


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.shutil")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.Path")
def test_cleanup_not_exist(mock_Path, mock_shutil):
    mock_path_obj = mock_Path.return_value
    mock_path_obj.exists.return_value = False
    mock_path_obj.is_dir.return_value = False
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.cleanup()

    mock_Path.assert_called_with("/home/8.8.8.8")
    mock_shutil.rmtree.assert_not_called()


@patch.dict(os.environ, {"BLOB_STORAGE_PROVIDER": "GCP"})
@patch("common.gcs_utils.download_gcp_blob")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.Path")
def test_download_blob_gcp(mock_Path, mock_download_gcp_blob):
    mock_path_obj = mock_Path.return_value
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.download_blob()

    mock_path_obj.mkdir.assert_called_with(exist_ok=True)
    mock_download_gcp_blob.assert_called_with(
        "test-manufacturer/test-model/1.0.0/firmware_package.tar",
        "/home/8.8.8.8/firmware_package.tar",
        "",
    )


@patch.dict(os.environ, {"BLOB_STORAGE_PROVIDER": "DOCKER"})
@patch(
    "addons.images.firmware_manager.upgrade_runner.upgrader.download_blob.download_docker_blob"
)
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.Path")
def test_download_blob_docker(mock_Path, mock_download_docker_blob):
    mock_path_obj = mock_Path.return_value
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.download_blob()

    mock_path_obj.mkdir.assert_called_with(exist_ok=True)
    mock_download_docker_blob.assert_called_with(
        "test-manufacturer/test-model/1.0.0/firmware_package.tar",
        "/home/8.8.8.8/firmware_package.tar",
    )


@patch.dict(os.environ, {"BLOB_STORAGE_PROVIDER": "Test"})
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.logging")
@patch("common.gcs_utils.download_gcp_blob")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.Path")
def test_download_blob_not_supported(mock_Path, mock_download_gcp_blob, mock_logging):
    mock_path_obj = mock_Path.return_value
    test_upgrader = TestUpgrader(test_upgrade_info)

    with pytest.raises(StorageProviderNotSupportedException):
        test_upgrader.download_blob()

        mock_path_obj.mkdir.assert_called_with(exist_ok=True)
        mock_download_gcp_blob.assert_not_called()
        mock_logging.error.assert_called_with("Unsupported blob storage provider")


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.requests")
def test_notify_firmware_manager_success(mock_requests, mock_logging):
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.notify_firmware_manager(success=True)

    expected_url = "http://127.0.0.1:8080/firmware_upgrade_completed"
    expected_body = {"rsu_ip": "8.8.8.8", "status": "success"}
    mock_logging.info.assert_called_with(
        "Firmware upgrade script completed for 8.8.8.8 with status: success"
    )
    mock_logging.error.assert_not_called()
    mock_requests.post.assert_called_with(expected_url, json=expected_body)


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.requests")
def test_notify_firmware_manager_fail(mock_requests, mock_logging):
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.notify_firmware_manager(success=False)

    expected_url = "http://127.0.0.1:8080/firmware_upgrade_completed"
    expected_body = {"rsu_ip": "8.8.8.8", "status": "fail"}
    mock_logging.info.assert_called_with(
        "Firmware upgrade script completed for 8.8.8.8 with status: fail"
    )
    mock_logging.error.assert_not_called()
    mock_requests.post.assert_called_with(expected_url, json=expected_body)


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.logging")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.requests")
def test_notify_firmware_manager_exception(mock_requests, mock_logging):
    mock_requests.post.side_effect = Exception("Exception occurred during upgrade")
    test_upgrader = TestUpgrader(test_upgrade_info)

    test_upgrader.notify_firmware_manager(success=True)

    mock_logging.error.assert_called_with(
        "Failed to connect to the Firmware Manager API for '8.8.8.8': Exception occurred during upgrade"
    )


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.subprocess")
def test_upgrader_wait_until_online_success(mock_subprocess, mock_time):
    run_response_obj = MagicMock()
    run_response_obj.returncode = 0
    mock_subprocess.run.return_value = run_response_obj

    test_upgrader = TestUpgrader(test_upgrade_info)
    code = test_upgrader.wait_until_online()

    assert code == 0
    assert mock_time.sleep.call_count == 1


@patch("addons.images.firmware_manager.upgrade_runner.upgrader.time")
@patch("addons.images.firmware_manager.upgrade_runner.upgrader.subprocess")
def test_upgrader_wait_until_online_timeout(mock_subprocess, mock_time):
    run_response_obj = MagicMock()
    run_response_obj.returncode = 1
    mock_subprocess.run.return_value = run_response_obj

    test_upgrader = TestUpgrader(test_upgrade_info)
    code = test_upgrader.wait_until_online()

    assert code == -1
    assert mock_time.sleep.call_count == 180
