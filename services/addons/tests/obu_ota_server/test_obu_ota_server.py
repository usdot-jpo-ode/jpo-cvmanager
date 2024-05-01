import pytest
import os
import glob
from unittest.mock import patch
from addons.images.obu_ota_server.obu_ota_server import get_firmware_list


@patch("os.getenv")
@patch("glob.glob")
def test_get_firmware_list_local(mock_glob, mock_getenv):
    # Arrange
    mock_getenv.return_value = "LOCAL"
    mock_glob.return_value = ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]

    # Act
    result = get_firmware_list()

    # Assert
    mock_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_glob.assert_called_once_with("/firmwares/*.tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("os.getenv")
@patch("services.common.gcs_utils.list_gcs_blobs")
def test_get_firmware_list_gcs(mock_list_gcs_blobs, mock_getenv):
    # Arrange
    mock_getenv.return_value = "GCP"
    mock_list_gcs_blobs.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]

    # Act
    result = get_firmware_list()

    # Assert
    mock_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_list_gcs_blobs.assert_called_once_with("firmwares", ".tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


if __name__ == "__main__":
    pytest.main()
