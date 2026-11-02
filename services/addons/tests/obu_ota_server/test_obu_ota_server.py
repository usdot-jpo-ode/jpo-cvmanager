import asyncio
import pytest
import tempfile
import threading
import time
import os
from httpx import ASGITransport, AsyncClient, BasicAuth
from fastapi import HTTPException, Request
from unittest.mock import patch, MagicMock, AsyncMock
from datetime import datetime

from addons.images.obu_ota_server import obu_ota_server
from addons.images.obu_ota_server.obu_ota_server import (
    get_firmware_list,
    get_firmware,
    get_manufacturer_id,
    parse_range_header,
    read_file,
    log_request,
    write_request_log,
    removed_old_logs,
    app,
)

FIRMWARE_NAME = "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig"
FIRMWARE_CONTENT = bytes(range(256)) * 4  # 1024 bytes


@pytest.fixture(autouse=True)
def clear_manufacturer_cache():
    obu_ota_server.manufacturer_ids.clear()
    yield
    obu_ota_server.manufacturer_ids.clear()


@pytest.fixture
def firmware_dir():
    with tempfile.TemporaryDirectory() as directory:
        with open(os.path.join(directory, FIRMWARE_NAME), "wb") as firmware:
            firmware.write(FIRMWARE_CONTENT)
        with patch.object(obu_ota_server, "FIRMWARE_DIR", directory):
            yield directory


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("glob.glob")
def test_get_firmware_list_local(mock_glob):
    mock_glob.return_value = ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]

    result = get_firmware_list()

    mock_glob.assert_called_once_with("/firmwares/*.tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "GCP")
@patch("obu_ota_server_environment.BLOB_STORAGE_PATH", "PATH")
@patch("common.gcs_utils.list_gcs_blobs")
def test_get_firmware_list_gcs(mock_list_gcs_blobs):
    mock_list_gcs_blobs.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]

    result = get_firmware_list()

    mock_list_gcs_blobs.assert_called_once_with("PATH", ".tar.sig")
    assert result == ["/firmwares/test1.tar.sig", "/firmwares/test2.tar.sig"]


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("os.path.exists")
@patch("common.gcs_utils.list_gcs_blobs")
def test_get_firmware_local_fail(mock_gcs_utils, mock_os_path_exists):
    mock_os_path_exists.return_value = False

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_path_exists.assert_called_once_with(local_file_path)
    mock_gcs_utils.assert_not_called()

    assert result is False


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("os.path.exists")
@patch("common.gcs_utils.list_gcs_blobs")
def test_get_firmware_local_success(mock_gcs_utils, mock_os_path_exists):
    mock_os_path_exists.return_value = True

    firmware_id = "test_firmware_id"
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_path_exists.assert_called_once_with(local_file_path)
    mock_gcs_utils.assert_not_called()

    assert result is True


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "GCP")
@patch("os.path.exists")
@patch("common.gcs_utils.download_gcp_blob")
def test_get_firmware_gcs_success(mock_download_gcp_blob, mock_os_path_exists):
    mock_os_path_exists.return_value = False
    mock_download_gcp_blob.return_value = True

    firmware_file_ext = ".tar.sig"
    firmware_id = "test_firmware_id" + firmware_file_ext
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_path_exists.assert_called_with(local_file_path)
    mock_download_gcp_blob.assert_called_once_with(
        firmware_id, local_file_path, firmware_file_ext
    )

    assert result is True


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "GCP")
@patch("os.path.exists")
@patch("common.gcs_utils.download_gcp_blob")
def test_get_firmware_gcs_failure(mock_download_gcp_blob, mock_os_path_exists):
    mock_os_path_exists.return_value = False
    mock_download_gcp_blob.return_value = False

    firmware_file_ext = ".tar.sig"
    firmware_id = "test_firmware_id" + firmware_file_ext
    local_file_path = "test_local_file_path"
    result = get_firmware(firmware_id, local_file_path)

    mock_os_path_exists.assert_called_with(local_file_path)
    mock_download_gcp_blob.assert_called_once_with(
        firmware_id, local_file_path, firmware_file_ext
    )

    assert result is False


@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "GCP")
@pytest.mark.asyncio
async def test_get_firmware_serializes_concurrent_gcs_downloads():
    calls = []
    active = 0
    max_active = 0
    counter_lock = threading.Lock()

    def slow_download(firmware_id, local_file_path, file_extension):
        nonlocal active, max_active
        with counter_lock:
            active += 1
            max_active = max(max_active, active)
            calls.append(firmware_id)
        # Simulate a download that is still writing when another request arrives
        time.sleep(0.2)
        with open(local_file_path, "wb") as firmware:
            firmware.write(FIRMWARE_CONTENT)
        with counter_lock:
            active -= 1
        return True

    with tempfile.TemporaryDirectory() as directory:
        local_file_path = os.path.join(directory, FIRMWARE_NAME)
        with patch("common.gcs_utils.download_gcp_blob", side_effect=slow_download):
            results = await asyncio.gather(
                asyncio.to_thread(get_firmware, FIRMWARE_NAME, local_file_path),
                asyncio.to_thread(get_firmware, FIRMWARE_NAME, local_file_path),
            )

        # The second request waits for the first download, then finds the complete file
        assert results == [True, True]
        assert max_active == 1
        assert calls == [FIRMWARE_NAME]
        assert os.path.getsize(local_file_path) == len(FIRMWARE_CONTENT)


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

    assert await read_file(temp_path, 0, 9) == b"Test data"
    assert await read_file(temp_path, 5, 4) == b"data"

    # Clean up the temporary file
    os.remove(temp_path)


@pytest.mark.asyncio
async def test_read_file_failure():
    with pytest.raises(HTTPException) as excinfo:
        await read_file("fake_path", 0, 9)
    assert str(excinfo.value) == "500: Error reading file"


@pytest.mark.anyio
async def test_read_root():
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "obu ota server healthcheck", "root_path": ""}


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
@pytest.mark.anyio
async def test_get_manifest(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = [
        "/firmwares/test1.tar.sig",
        "/firmwares/test2.tar.sig",
    ]
    mock_commsignia_manifest.return_value = {"json": "data"}
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )
    assert response.status_code == 200
    assert response.json() == {"json": "data"}


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@pytest.mark.anyio
async def test_get_manifest_requires_auth():
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "wrong")
        )
    assert response.status_code == 401


async def download(path: str, headers: dict | None = None):
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        return await ac.get(
            path, headers=headers or {}, auth=BasicAuth("username", "password")
        )


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("addons.images.obu_ota_server.obu_ota_server.log_request", new_callable=AsyncMock)
@pytest.mark.anyio
async def test_get_fw_full_file(mock_log_request, firmware_dir):
    response = await download(f"/firmwares/commsignia/{FIRMWARE_NAME}")

    assert response.status_code == 200
    assert response.content == FIRMWARE_CONTENT
    assert response.headers["Content-Length"] == "1024"
    assert response.headers["Accept-Ranges"] == "bytes"
    assert "Content-Range" not in response.headers
    mock_log_request.assert_called_once()
    assert mock_log_request.call_args[0][3] is False


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("addons.images.obu_ota_server.obu_ota_server.log_request", new_callable=AsyncMock)
@pytest.mark.anyio
async def test_get_fw_partial_range_is_inclusive(mock_log_request, firmware_dir):
    response = await download(
        f"/firmwares/commsignia/{FIRMWARE_NAME}", {"Range": "bytes=0-99"}
    )

    assert response.status_code == 206
    assert response.content == FIRMWARE_CONTENT[0:100]
    assert response.headers["Content-Length"] == "100"
    assert response.headers["Content-Range"] == "bytes 0-99/1024"


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("addons.images.obu_ota_server.obu_ota_server.log_request", new_callable=AsyncMock)
@pytest.mark.anyio
async def test_get_fw_open_ended_and_clamped_ranges(mock_log_request, firmware_dir):
    response = await download(
        f"/firmwares/commsignia/{FIRMWARE_NAME}", {"Range": "bytes=1000-"}
    )
    assert response.status_code == 206
    assert response.content == FIRMWARE_CONTENT[1000:]
    assert response.headers["Content-Range"] == "bytes 1000-1023/1024"

    response = await download(
        f"/firmwares/commsignia/{FIRMWARE_NAME}", {"Range": "bytes=1020-5000"}
    )
    assert response.status_code == 206
    assert response.content == FIRMWARE_CONTENT[1020:]
    assert response.headers["Content-Range"] == "bytes 1020-1023/1024"


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("addons.images.obu_ota_server.obu_ota_server.log_request", new_callable=AsyncMock)
@pytest.mark.anyio
async def test_get_fw_unsatisfiable_range(mock_log_request, firmware_dir):
    response = await download(
        f"/firmwares/commsignia/{FIRMWARE_NAME}", {"Range": "bytes=1024-"}
    )

    assert response.status_code == 416
    assert response.headers["Content-Range"] == "bytes */1024"
    mock_log_request.assert_called_once()
    assert mock_log_request.call_args[0][3] is True


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.BLOB_STORAGE_PROVIDER", "DOCKER")
@patch("addons.images.obu_ota_server.obu_ota_server.log_request", new_callable=AsyncMock)
@pytest.mark.anyio
async def test_get_fw_missing_file(mock_log_request, firmware_dir):
    response = await download(
        "/firmwares/commsignia/ob4-generic-ro-secureboot-y99.99.9-b000000.tar.sig"
    )

    assert response.status_code == 404
    assert mock_log_request.call_args[0][3] is True


@pytest.mark.parametrize(
    "firmware_id",
    [
        "..%2F..%2Fetc%2Fpasswd",
        "..%2Fob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig",
        "not-a-firmware.bin",
    ],
)
@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("addons.images.obu_ota_server.obu_ota_server.log_request", new_callable=AsyncMock)
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware")
@patch("addons.images.obu_ota_server.obu_ota_server.read_file")
@pytest.mark.anyio
async def test_get_fw_rejects_invalid_names_without_file_access(
    mock_read_file, mock_get_firmware, mock_log_request, firmware_id
):
    response = await download(f"/firmwares/commsignia/{firmware_id}")

    assert response.status_code == 404
    mock_get_firmware.assert_not_called()
    mock_read_file.assert_not_called()


@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
def test_get_manufacturer_id_is_parameterized_and_cached(mock_pgquery):
    mock_pgquery.query_db.return_value = [(7,)]

    assert get_manufacturer_id("Commsignia") == 7
    assert get_manufacturer_id("COMMSIGNIA") == 7

    mock_pgquery.query_db.assert_called_once()
    query, params = mock_pgquery.query_db.call_args[0]
    assert ":name" in query
    assert params == {"name": "commsignia"}


@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
def test_get_manufacturer_id_missing_is_not_cached(mock_pgquery):
    mock_pgquery.query_db.return_value = []

    assert get_manufacturer_id("Commsignia") is None
    assert get_manufacturer_id("Commsignia") is None
    assert mock_pgquery.query_db.call_count == 2


@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
@patch("addons.images.obu_ota_server.obu_ota_server.datetime")
@patch("addons.images.obu_ota_server.obu_ota_server.removed_old_logs")
def test_write_request_log_uses_bound_parameters(
    mock_removed_old_logs, mock_datetime, mock_pgquery
):
    fixed_datetime = datetime(2024, 7, 30, 0, 0, 0)
    mock_datetime.now.return_value = fixed_datetime
    mock_pgquery.query_db.return_value = [(3,)]
    malicious_serial = "x'); DROP TABLE obu_ota_requests;--"

    write_request_log(
        "Commsignia",
        malicious_serial,
        "y11.11.1-b11111",
        "127.0.0.1",
        FIRMWARE_NAME,
        True,
        "e" * 300,
    )

    query, params = mock_pgquery.write_db.call_args[0]
    # User-provided values are never part of the SQL text
    assert malicious_serial not in query
    assert "y11.11.1-b11111" not in query
    assert "CAST(:origin_ip AS inet)" in query
    assert ":error_status," in query
    assert "bit" not in query
    assert params == {
        "obu_sn": malicious_serial,
        "manufacturer": 3,
        "request_datetime": fixed_datetime,
        "origin_ip": "127.0.0.1",
        "obu_firmware_version": "y11.11.1-b11111",
        "requested_firmware_version": FIRMWARE_NAME,
        "error_status": True,
        "error_message": "e" * 128,
    }
    mock_removed_old_logs.assert_called_once_with(malicious_serial)


@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
@patch("addons.images.obu_ota_server.obu_ota_server.removed_old_logs")
def test_write_request_log_skips_unknown_manufacturer(
    mock_removed_old_logs, mock_pgquery
):
    mock_pgquery.query_db.return_value = []

    write_request_log("Unknown", "sn", "v", "127.0.0.1", FIRMWARE_NAME, False, "")

    mock_pgquery.write_db.assert_not_called()
    mock_removed_old_logs.assert_not_called()


@patch("addons.images.obu_ota_server.obu_ota_server.write_request_log")
@pytest.mark.asyncio
async def test_log_request_normalizes_request_values(mock_write_request_log):
    mock_request = MagicMock(spec=Request)
    mock_request.query_params = {"version": "v" * 200}
    mock_request.client.host = "not-an-ip"

    await log_request("Commsignia", mock_request, FIRMWARE_NAME, False, "")

    mock_write_request_log.assert_called_once_with(
        "Commsignia", "", "v" * 128, "0.0.0.0", FIRMWARE_NAME, False, ""
    )


@patch("addons.images.obu_ota_server.obu_ota_server.write_request_log")
@pytest.mark.asyncio
async def test_log_request_swallows_errors(mock_write_request_log):
    mock_write_request_log.side_effect = Exception("db down")
    mock_request = MagicMock(spec=Request)
    mock_request.query_params = {"serialnum": "sn", "version": "v"}
    mock_request.client.host = "10.0.0.1"

    # Logging failures must never fail the firmware download
    await log_request("Commsignia", mock_request, FIRMWARE_NAME, False, "")


@patch("obu_ota_server_environment.MAX_COUNT", 10)
@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
def test_removed_old_logs_is_single_parameterized_delete(mock_pgquery):
    serialnum = "x' OR '1'='1"
    removed_old_logs(serialnum)

    mock_pgquery.query_db.assert_not_called()
    query, params = mock_pgquery.write_db.call_args[0]
    assert serialnum not in query
    assert "OFFSET :max_count" in query
    assert "error_status = FALSE" in query
    assert params == {"obu_sn": serialnum, "max_count": 10}


@patch("addons.images.obu_ota_server.obu_ota_server.pgquery")
def test_removed_old_logs_swallows_errors(mock_pgquery):
    mock_pgquery.write_db.side_effect = Exception("db down")
    removed_old_logs("sn")


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.NGINX_ENCRYPTION", "plain")
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
@pytest.mark.anyio
async def test_fqdn_response_plain(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = []
    expected_hostname = "http://localhost"
    mock_commsignia_manifest.return_value = {"json": "data"}

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )

    assert response.status_code == 200
    mock_commsignia_manifest.assert_called_once_with(expected_hostname, [])


@patch("obu_ota_server_environment.OTA_USERNAME", "username")
@patch("obu_ota_server_environment.OTA_PASSWORD", "password")
@patch("obu_ota_server_environment.NGINX_ENCRYPTION", "ssl")
@patch("addons.images.obu_ota_server.obu_ota_server.get_firmware_list")
@patch("addons.images.obu_ota_server.obu_ota_server.commsignia_manifest.add_contents")
@pytest.mark.anyio
async def test_fqdn_response_ssl(mock_commsignia_manifest, mock_get_firmware_list):
    mock_get_firmware_list.return_value = []
    expected_hostname = "https://localhost"
    mock_commsignia_manifest.return_value = {"json": "data"}

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get(
            "/firmwares/commsignia", auth=BasicAuth("username", "password")
        )

    assert response.status_code == 200
    mock_commsignia_manifest.assert_called_once_with(expected_hostname, [])


if __name__ == "__main__":
    pytest.main()
