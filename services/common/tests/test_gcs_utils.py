from unittest.mock import patch, MagicMock
import os

from common import gcs_utils


@patch.dict(
    os.environ, {"GCP_PROJECT": "test-project", "BLOB_STORAGE_BUCKET": "test-bucket"}
)
@patch("common.gcs_utils.logging")
@patch("common.gcs_utils.storage.Client")
def test_download_gcp_blob(mock_storage_client, mock_logging):
    # mock
    mock_client = mock_storage_client.return_value
    mock_bucket = mock_client.get_bucket.return_value
    mock_blob = mock_bucket.blob.return_value

    # run
    gcs_utils.download_gcp_blob(
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


@patch.dict(
    os.environ, {"GCP_PROJECT": "test-project", "BLOB_STORAGE_BUCKET": "test-bucket"}
)
@patch("common.gcs_utils.storage.Client")
def test_list_gcs_blobs(mock_storage_client):
    # mock
    mock_client = mock_storage_client.return_value
    # mock_bucket = mock_client.get_bucket.return_value
    mock_blobs = []
    for filename in ["file1.txt", "file2.jpg", "file3.txt"]:
        mock_blob = MagicMock()
        mock_blob.name = filename
        mock_blobs.append(mock_blob)

    mock_client.list_blobs.return_value = mock_blobs

    # run
    result = gcs_utils.list_gcs_blobs(
        gcs_prefix="path/to/files/", file_extension=".txt"
    )

    # validate
    mock_storage_client.assert_called_with("test-project")
    mock_client.list_blobs.assert_called_with(
        "test-bucket", prefix="path/to/files/", delimiter="/"
    )
    assert result == ["/firmwares/file1.txt", "/firmwares/file3.txt"]


@patch.dict(
    os.environ, {"GCP_PROJECT": "test-project", "BLOB_STORAGE_BUCKET": "test-bucket"}
)
@patch("common.gcs_utils.storage.Client")
def test_list_gcs_blobs_empty(mock_storage_client):
    # mock
    mock_client = mock_storage_client.return_value
    # mock_bucket = mock_client.get_bucket.return_value
    mock_blobs = []

    mock_client.list_blobs.return_value = mock_blobs

    # run
    result = gcs_utils.list_gcs_blobs(
        gcs_prefix="path/to/files/", file_extension=".txt"
    )

    # validate
    mock_storage_client.assert_called_with("test-project")
    mock_client.list_blobs.assert_called_with(
        "test-bucket", prefix="path/to/files/", delimiter="/"
    )
    assert result == []
