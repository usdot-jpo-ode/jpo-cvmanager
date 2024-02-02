from unittest.mock import MagicMock, patch
import os

from addons.images.firmware_manager import download_blob


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
        blob_name="test.blob", destination_file_name="/home/test/"
    )

    # validate
    mock_storage_client.assert_called_with("test-project")
    mock_client.get_bucket.assert_called_with("test-bucket")
    mock_bucket.blob.assert_called_with("test.blob")
    mock_blob.download_to_filename.assert_called_with("/home/test/")
    mock_logging.info.assert_called_with(
        "Downloaded storage object test.blob from bucket test-bucket to local file /home/test/."
    )


@patch("addons.images.firmware_manager.download_blob.logging")
def test_download_docker_blob(mock_logging):
    # prepare
    os.system = MagicMock()
    blob_name = "test.blob"
    destination_file_name = "/home/test/"

    # run
    download_blob.download_docker_blob(blob_name, destination_file_name)

    # validate
    os.system.assert_called_with(f"cp /mnt/blob_storage/{blob_name} {destination_file_name}")
    mock_logging.info.assert_called_with(
        f"Copied storage object {blob_name} from directory /mnt/blob_storage to local file {destination_file_name}."
    )

