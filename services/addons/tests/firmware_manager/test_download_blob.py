from unittest.mock import MagicMock, patch
import os
import pytest

from addons.images.firmware_manager import download_blob
from addons.images.firmware_manager.download_blob import UnsupportedFileTypeException


@patch.dict(
    os.environ, {"GCP_PROJECT": "test-project", "BLOB_STORAGE_BUCKET": "test-bucket"}
)
@patch("addons.images.firmware_manager.download_blob.logging")
@patch("addons.images.firmware_manager.download_blob.storage.Client")
def test_download_gcp_blob(mock_storage_client, mock_logging):
    # mock
    mock_client = mock_storage_client.return_value
    mock_bucket = mock_client.get_bucket.return_value
    mock_blob = mock_bucket.blob.return_value

    # run
    download_blob.download_gcp_blob(
        blob_name="test.tar", destination_file_name="/home/test/"
    )

    # validate
    mock_storage_client.assert_called_with("test-project")
    mock_client.get_bucket.assert_called_with("test-bucket")
    mock_bucket.blob.assert_called_with("test.tar")
    mock_blob.download_to_filename.assert_called_with("/home/test/")
    mock_logging.info.assert_called_with(
        "Downloaded storage object test.tar from bucket test-bucket to local file /home/test/."
    )

@patch.dict(
    os.environ, {"GCP_PROJECT": "test-project", "BLOB_STORAGE_BUCKET": "test-bucket"}
)
def test_download_gcp_blob_unsupported_file_type():
    # prepare
    blob_name = "test.blob"
    destination_file_name = "/home/test/"

    # run
    with pytest.raises(UnsupportedFileTypeException):
        download_blob.download_gcp_blob(blob_name, destination_file_name)

        # validate
        os.system.assert_not_called()
        mock_logging.error.assert_called_with(
            f"Unsupported file type for storage object {blob_name}. Only .tar files are supported."
        )


@patch("addons.images.firmware_manager.download_blob.logging")
def test_download_docker_blob(mock_logging):
    # prepare
    os.system = MagicMock()
    blob_name = "test.tar"
    destination_file_name = "/home/test/"

    # run
    download_blob.download_docker_blob(blob_name, destination_file_name)

    # validate
    os.system.assert_called_with(f"cp /mnt/blob_storage/{blob_name} {destination_file_name}")
    mock_logging.info.assert_called_with(
        f"Copied storage object {blob_name} from directory /mnt/blob_storage to local file {destination_file_name}."
    )


@patch("addons.images.firmware_manager.download_blob.logging")
def test_download_docker_blob_unsupported_file_type(mock_logging):
    # prepare
    os.system = MagicMock()
    blob_name = "test.blob"
    destination_file_name = "/home/test/"

    # run
    with pytest.raises(UnsupportedFileTypeException):
        download_blob.download_docker_blob(blob_name, destination_file_name)

        # validate
        os.system.assert_not_called()
        mock_logging.error.assert_called_with(
            f"Unsupported file type for storage object {blob_name}. Only .tar files are supported."
        )