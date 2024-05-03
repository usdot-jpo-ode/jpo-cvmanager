import asyncio
import pytest
import aiofiles
import json
import tempfile
import os

from httpx import AsyncClient
from fastapi import HTTPException, Request, Response

from unittest.mock import MagicMock, AsyncMock, patch
from addons.images.obu_ota_server.obu_ota_server import (
    get_firmware_list,
    get_firmware,
    parse_range_header,
    read_file,
    app,
)


@patch("os.getenv")
@patch("glob.glob")
def test_get_firmware_list_local(mock_glob, mock_getenv):
    mock_getenv.return_value = "LOCAL"
    mock_glob.return_value = ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]

    result = get_firmware_list()

    mock_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_glob.assert_called_once_with("/firmwares/*.tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("os.getenv")
@patch("services.common.gcs_utils.list_gcs_blobs")
def test_get_firmware_list_gcs(mock_list_gcs_blobs, mock_getenv):
    mock_getenv.return_value = "GCP"
    mock_list_gcs_blobs.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]

    result = get_firmware_list()

    mock_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_list_gcs_blobs.assert_called_once_with("firmwares", ".tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("os.getenv")
@patch("os.path.exists")
@patch("services.common.gcs_utils.list_gcs_blobs")
def test_get_firmware_local_fail(mock_gcs_utils, mock_os_path_exists, mock_os_getenv):
    mock_os_getenv.return_value = "LOCAL"
    mock_os_path_exists.return_value = False

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_os_path_exists.assert_called_once_with(local_file_path)
    mock_gcs_utils.assert_not_called()

    assert result == False


@patch("os.getenv")
@patch("os.path.exists")
@patch("services.common.gcs_utils.list_gcs_blobs")
def test_get_firmware_local_success(
    mock_gcs_utils, mock_os_path_exists, mock_os_getenv
):
    mock_os_getenv.return_value = "LOCAL"
    mock_os_path_exists.return_value = True

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_os_path_exists.assert_called_once_with(local_file_path)
    mock_gcs_utils.assert_not_called()

    assert result == True


@patch("os.getenv")
@patch("os.path.exists")
@patch("services.common.gcs_utils.download_gcp_blob")
def test_get_firmware_gcs_success(
    mock_download_gcp_blob, mock_os_path_exists, mock_os_getenv
):
    mock_os_getenv.return_value = "GCP"
    mock_os_path_exists.return_value = False
    mock_download_gcp_blob.return_value = True

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_os_path_exists.assert_called_with(local_file_path)
    mock_download_gcp_blob.assert_called_once_with(firmware_id, local_file_path)

    assert result == True


@patch("os.getenv")
@patch("os.path.exists")
@patch("services.common.gcs_utils.download_gcp_blob")
def test_get_firmware_gcs_failure(
    mock_download_gcp_blob, mock_os_path_exists, mock_os_getenv
):
    mock_os_getenv.return_value = "GCP"
    mock_os_path_exists.return_value = False
    mock_download_gcp_blob.return_value = False

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_with("BLOB_STORAGE_PROVIDER", "LOCAL")
    mock_os_path_exists.assert_called_with(local_file_path)
    mock_download_gcp_blob.assert_called_once_with(firmware_id, local_file_path)

    assert result == False


def test_parse_range_header_valid():
    assert parse_range_header(None) == (0, None)
    assert parse_range_header("bytes=0-499") == (0, 499)
    assert parse_range_header("bytes=500-") == (500, None)


def test_parse_range_header_invalid():
    with pytest.raises(HTTPException) as excinfo:
        parse_range_header("invalid range header")
    assert str(excinfo.value) == "400: Invalid Range header"

    with pytest.raises(HTTPException) as excinfo:
        parse_range_header("bytes=0-abc")
    assert str(excinfo.value) == "400: Invalid Range header"

    with pytest.raises(HTTPException) as excinfo:
        parse_range_header("bytes=-500")
    assert str(excinfo.value) == "400: Invalid Range header"

    with pytest.raises(HTTPException) as excinfo:
        parse_range_header("bytes=500-499")
    assert (
        str(excinfo.value)
        == "400: Invalid Range header: start cannot be greater than end"
    )


@pytest.mark.asyncio
async def test_read_file():
    with tempfile.NamedTemporaryFile(delete=False) as temp:
        temp.write(b"Test data")
        temp_path = temp.name

    data, file_size = await read_file(temp_path, 0, 9)

    assert data == b"Test data"
    assert file_size == 9

    # Clean up the temporary file
    os.remove(temp_path)


@pytest.mark.asyncio
async def test_read_file_failure():
    with pytest.raises(HTTPException) as excinfo:
        data, file_size = await read_file("fake_path", 0, 9)
    assert str(excinfo.value) == "500: Error reading file"


@pytest.mark.anyio
async def test_read_root():
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "obu ota server root path", "root_path": ""}


@pytest.mark.anyio
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsginia_manifest.add_contents")
async def test_get_manifest(mock_commsginia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]
    mock_commsginia_manifest.return_value = {"json": "data"}
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get("/firmwares")
    assert response.status_code == 200
    assert response.json() == {"json": "data"}


if __name__ == "__main__":
    pytest.main()
