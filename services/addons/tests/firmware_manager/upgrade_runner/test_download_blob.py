from unittest.mock import MagicMock, patch
import os

from addons.images.firmware_manager.upgrade_runner import download_blob


@patch("addons.images.firmware_manager.upgrade_runner.download_blob.logging")
def test_download_docker_blob(mock_logging):
    # prepare
    os.system = MagicMock()
    blob_name = "test.tar"
    destination_file_name = "/home/test/"

    # run
    download_blob.download_docker_blob(blob_name, destination_file_name)

    # validate
    os.system.assert_called_with(
        f"cp /mnt/blob_storage/{blob_name} {destination_file_name}"
    )
    mock_logging.info.assert_called_with(
        f"Copied storage object {blob_name} from directory /mnt/blob_storage to local file {destination_file_name}."
    )


@patch("common.util.logging")
def test_download_docker_blob_unsupported_file_type(mock_logging):
    # prepare
    os.system = MagicMock()
    blob_name = "test.blob"
    destination_file_name = "/home/test/"

    # run
    result = download_blob.download_docker_blob(blob_name, destination_file_name)

    # validate
    mock_logging.error.assert_called_with(
        f'Unsupported file type for storage object {blob_name}. Only ".tar" files are supported.'
    )
    assert result == False
