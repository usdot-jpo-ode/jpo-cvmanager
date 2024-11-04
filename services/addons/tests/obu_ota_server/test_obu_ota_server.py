import pytest
import tempfile
import os
from httpx import AsyncClient, BasicAuth
from fastapi import HTTPException, Request
from unittest.mock import patch, MagicMock
from datetime import datetime

from addons.images.obu_ota_server.obu_ota_server import (
    get_firmware_list,
    get_firmware,
    parse_range_header,
    read_file,
    log_request,
    removed_old_logs,
    app,
)


@patch("os.getenv")
@patch("glob.glob")
def test_get_firmware_list_local(mock_glob, mock_getenv):
    mock_getenv.return_value = "DOCKER"
    mock_glob.return_value = ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]

    result = get_firmware_list()

    mock_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "DOCKER")
    mock_glob.assert_called_once_with("/firmwares/*.tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("os.getenv")
@patch("common.gcs_utils.list_gcs_blobs")
def test_get_firmware_list_gcs(mock_list_gcs_blobs, mock_getenv):
    mock_getenv.return_value = "GCP"
    mock_list_gcs_blobs.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]

    result = get_firmware_list()

    # mock_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "DOCKER")
    mock_list_gcs_blobs.assert_called_once_with("GCP", ".tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("os.getenv")
@patch("os.path.exists")
@patch("common.gcs_utils.list_gcs_blobs")
def test_get_firmware_local_fail(mock_gcs_utils, mock_os_path_exists, mock_os_getenv):
    mock_os_getenv.return_value = "DOCKER"
    mock_os_path_exists.return_value = False

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "DOCKER")
    mock_os_path_exists.assert_called_once_with(local_file_path)
    mock_gcs_utils.assert_not_called()

    assert result == False


@patch("os.getenv")
@patch("os.path.exists")
@patch("common.gcs_utils.list_gcs_blobs")
def test_get_firmware_local_success(
    mock_gcs_utils, mock_os_path_exists, mock_os_getenv
):
    mock_os_getenv.return_value = "DOCKER"
    mock_os_path_exists.return_value = True

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_once_with("BLOB_STORAGE_PROVIDER", "DOCKER")
    mock_os_path_exists.assert_called_once_with(local_file_path)
    mock_gcs_utils.assert_not_called()

    assert result == True


@patch("os.getenv")
@patch("os.path.exists")
@patch("common.gcs_utils.download_gcp_blob")
def test_get_firmware_gcs_success(
    mock_download_gcp_blob, mock_os_path_exists, mock_os_getenv
):
    mock_os_getenv.return_value = "GCP"
    mock_os_path_exists.return_value = False
    mock_download_gcp_blob.return_value = True

    firmware_file_ext = ".tar.sig"
    firmware_id = "test_firmware_id" + firmware_file_ext
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_with("BLOB_STORAGE_PROVIDER", "DOCKER")
    mock_os_path_exists.assert_called_with(local_file_path)
    mock_download_gcp_blob.assert_called_once_with(
        firmware_id, local_file_path, firmware_file_ext
    )

    assert result == True


@patch("os.getenv")
@patch("os.path.exists")
@patch("common.gcs_utils.download_gcp_blob")
def test_get_firmware_gcs_failure(
    mock_download_gcp_blob, mock_os_path_exists, mock_os_getenv
):
    mock_os_getenv.return_value = "GCP"
    mock_os_path_exists.return_value = False
    mock_download_gcp_blob.return_value = False

    firmware_file_ext = ".tar.sig"
    firmware_id = "test_firmware_id" + firmware_file_ext
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_getenv.assert_called_with("BLOB_STORAGE_PROVIDER", "DOCKER")
    mock_os_path_exists.assert_called_with(local_file_path)
    mock_download_gcp_blob.assert_called_once_with(
        firmware_id, local_file_path, firmware_file_ext
    )

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

    data, file_size, end = await read_file(temp_path, 0, 9)

    assert data == b"Test data"
    assert file_size == 9
    assert end == 9

    # Clean up the temporary file
    os.remove(temp_path)


@pytest.mark.asyncio
async def test_read_file_failure():
    with pytest.raises(HTTPException) as excinfo:
        data, file_size, end = await read_file("fake_path", 0, 9)
    assert str(excinfo.value) == "500: Error reading file"


@pytest.mark.asyncio
async def test_read_file_no_end_range():
    with tempfile.NamedTemporaryFile(delete=False) as temp:
        temp.write(b"Test data")
        temp_path = temp.name

    data, file_size, end = await read_file(temp_path, 0, None)

    assert data == b"Test data"
    assert file_size == 9
    assert end == 9

    # Clean up the temporary file
    os.remove(temp_path)


@patch.dict("os.environ", {"OTA_USERNAME": "username", "OTA_PASSWORD": "password"})
@pytest.mark.anyio
async def test_read_root():
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "obu ota server healthcheck", "root_path": ""}


@patch.dict("os.environ", {"OTA_USERNAME": "username", "OTA_PASSWORD": "password"})
@pytest.mark.anyio
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
async def test_get_manifest(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]
    mock_commsignia_manifest.return_value = {"json": "data"}
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )
    assert response.status_code == 200
    assert response.json() == {"json": "data"}


@patch.dict("os.environ", {"OTA_USERNAME": "username", "OTA_PASSWORD": "password"})
@pytest.mark.anyio
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware")
@patch("addons.images.obu_ota_server.obu_ota_server.parse_range_header")
@patch("addons.images.obu_ota_server.obu_ota_server.read_file")
async def test_get_fw(mock_read_file, mock_parse_range_header, mock_get_firmware):
    mock_get_firmware.return_value = True
    mock_parse_range_header.return_value = 0, 100
    mock_read_file.return_value = b"Test data", 100, 100
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get(
            "/firmwares/commsignia/test_firmware_id",
            auth=BasicAuth("username", "password"),
        )
    assert response.status_code == 200
    assert response.content == b"Test data"
    assert response.headers["Content-Length"] == "100"


@pytest.mark.asyncio
@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
@patch("addons.images.obu_ota_server.obu_ota_server.datetime")
@patch("addons.images.obu_ota_server.obu_ota_server.removed_old_logs")
async def test_log_request(mock_removed_old_logs, mock_datetime, mock_pgquery):
    fixed_datetime = datetime(2024, 7, 30, 0, 0, 0)
    mock_datetime.now.return_value = fixed_datetime
    mock_datetime.strftime = datetime.strftime
    # Create a mock request object
    mock_request = MagicMock(spec=Request)
    mock_request.query_params = {
        "serialnum": "111111111111",
        "version": "y11.11.1-b11111",
    }
    mock_request.client.host = "127.0.0.1"

    # Call the log_request function
    manufacturer = 1
    firmware_id = "test_firmware"
    error_status = 0
    error_message = ""

    await log_request(
        manufacturer, mock_request, firmware_id, error_status, error_message
    )

    # Verify the query passed to write_db
    expected_query = (
        f"INSERT INTO public.obu_ota_requests (obu_sn, manufacturer, request_datetime, origin_ip, obu_firmware_version, requested_firmware_version, error_status, error_message) VALUES"
        f"('{mock_request.query_params['serialnum']}', {manufacturer}, '{fixed_datetime.strftime('%Y-%m-%d %H:%M:%S')}', '{mock_request.client.host}', '{mock_request.query_params['version']}', '{firmware_id}', B'{error_status}', '{error_message}')"
    ).replace(" ", "")

    actual_query = mock_pgquery.write_db.call_args[0][0].replace(" ", "")

    assert expected_query == actual_query

    mock_removed_old_logs.assert_called_once_with(
        mock_request.query_params["serialnum"]
    )


@patch.dict("os.environ", {"MAX_COUNT": "10"})
@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
def test_removed_old_logs_no_removal(mock_pgquery):
    mock_pgquery.query_db.side_effect = [
        [(5,)],  # success_count
    ]

    serialnum = "test_serialnum"
    removed_old_logs(serialnum)

    mock_pgquery.query_db.assert_called_once_with(
        f"SELECT COUNT(*) FROM public.obu_ota_requests WHERE obu_sn = '{serialnum}' AND error_status = B'0'"
    )
    mock_pgquery.write_db.assert_not_called()


@patch.dict("os.environ", {"MAX_COUNT": "5"})
@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
def test_removed_old_logs_with_removal(mock_pgquery):
    mock_pgquery.query_db.side_effect = [
        [(10,)],  # success_count
        [(1,), (2,), (3,), (4,), (5,)],  # oldest_entries
    ]

    serialnum = "test_serialnum"
    removed_old_logs(serialnum)

    assert mock_pgquery.query_db.call_count == 2
    mock_pgquery.query_db.assert_any_call(
        f"SELECT COUNT(*) FROM public.obu_ota_requests WHERE obu_sn = '{serialnum}' AND error_status = B'0'"
    )
    mock_pgquery.query_db.assert_any_call(
        f"SELECT request_id FROM public.obu_ota_requests WHERE obu_sn = '{serialnum}' AND error_status = B'0' ORDER BY request_datetime ASC LIMIT 5"
    )
    mock_pgquery.write_db.assert_called_once_with(
        "DELETE FROM public.obu_ota_requests WHERE request_id IN (1,2,3,4,5)"
    )


@patch.dict("os.environ", {"OTA_USERNAME": "username", "OTA_PASSWORD": "password"})
@pytest.mark.anyio
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
async def test_get_manifest(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]
    mock_commsignia_manifest.return_value = {"json": "data"}
    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )
    assert response.status_code == 200
    assert response.json() == {"json": "data"}


@patch.dict(
    "os.environ",
    {
        "OTA_USERNAME": "username",
        "OTA_PASSWORD": "password",
        "NGINX_ENCRYPTION": "plain",
    },
)
@pytest.mark.anyio
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
async def test_fqdn_response_plain(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = []
    expected_hostname = "http://localhost"
    mock_commsignia_manifest.return_value = {"json": "data"}

    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )

    assert response.status_code == 200
    mock_commsignia_manifest.assert_called_once_with(expected_hostname, [])


@patch.dict(
    "os.environ",
    {
        "OTA_USERNAME": "username",
        "OTA_PASSWORD": "password",
        "NGINX_ENCRYPTION": "SSL",
    },
)
@pytest.mark.anyio
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
async def test_fqdn_response_ssl(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = []
    expected_hostname = "https://localhost"
    mock_commsignia_manifest.return_value = {"json": "data"}

    async with AsyncClient(app=app, base_url="http://test") as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )

    assert response.status_code == 200
    mock_commsignia_manifest.assert_called_once_with(expected_hostname, [])


if __name__ == "__main__":
    pytest.main()
