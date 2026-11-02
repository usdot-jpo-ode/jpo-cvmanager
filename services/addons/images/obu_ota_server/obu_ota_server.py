from typing import Any
from fastapi import FastAPI, Request, Response, HTTPException, Depends
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from common import gcs_utils, pgquery
import commsignia_manifest
import glob
import ipaddress
import os
import aiofiles
import logging
import threading
from datetime import datetime, timezone
import asyncio
import obu_ota_server_environment

app = FastAPI()
security = HTTPBasic()

FIRMWARE_DIR = "/firmwares"
commsignia_file_ext = ".tar.sig"
commsignia_manufacturer_name = "Commsignia"

# obu_ota_requests text columns are varchar(128)
MAX_LOG_FIELD_LENGTH = 128

# Manufacturer ids never change once created, so successful lookups are cached
manufacturer_ids: dict[str, int] = {}

# Holds references to fire-and-forget logging tasks so they are not garbage collected
background_tasks: set[asyncio.Task] = set()

# Serializes firmware storage access. GCS downloads write directly to the firmware
# directory, so a concurrent request could otherwise serve a partially written file.
storage_lock = threading.Lock()


def authenticate_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    correct_username = obu_ota_server_environment.OTA_USERNAME
    correct_password = obu_ota_server_environment.OTA_PASSWORD
    if (
        credentials.username != correct_username
        or credentials.password != correct_password
    ):
        raise HTTPException(
            status_code=401,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Basic"},
        )
    return credentials.username


@app.get("/")
async def read_root(request: Request):
    return {
        "message": "obu ota server healthcheck",
        "root_path": request.scope.get("root_path"),
    }


def get_firmware_list() -> list:
    blob_storage_provider = obu_ota_server_environment.BLOB_STORAGE_PROVIDER
    files = []
    file_extension = commsignia_file_ext
    with storage_lock:
        if blob_storage_provider.upper() == "DOCKER":
            files = glob.glob(f"{FIRMWARE_DIR}/*{file_extension}")
        elif blob_storage_provider.upper() == "GCP":
            blob_storage_path = obu_ota_server_environment.BLOB_STORAGE_PATH
            files = gcs_utils.list_gcs_blobs(blob_storage_path, file_extension)
    return files


def get_host_name() -> str:
    host_name = obu_ota_server_environment.SERVER_HOST
    tls_enabled = obu_ota_server_environment.NGINX_ENCRYPTION
    if tls_enabled.lower() == "ssl":
        host_name = "https://" + host_name
    else:
        host_name = "http://" + host_name
    return host_name


@app.get("/firmwares/commsignia", dependencies=[Depends(authenticate_user)])
async def get_manifest(request: Request) -> dict[str, Any]:
    try:
        files = await asyncio.to_thread(get_firmware_list)
        logging.debug(f"get_manifest :: Files: {files}")
        host_name = get_host_name()
        response_manifest = commsignia_manifest.add_contents(host_name, files)
        return response_manifest
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def get_firmware(firmware_id: str, local_file_path: str) -> bool:
    try:
        blob_storage_provider = obu_ota_server_environment.BLOB_STORAGE_PROVIDER
        with storage_lock:
            # checks if firmware exists locally
            if not os.path.exists(local_file_path):
                # If configured to only use local storage, return False as firmware does not exist
                if blob_storage_provider.upper() == "DOCKER":
                    return False
                # If configured to use GCP storage, download the firmware from GCP
                elif blob_storage_provider.upper() == "GCP":
                    # Download blob will attempt to download the firmware and return True if successful
                    return gcs_utils.download_gcp_blob(
                        firmware_id, local_file_path, commsignia_file_ext
                    )
            return True
    except Exception:
        logging.exception("get_firmware: Error getting firmware")
        raise HTTPException(status_code=500, detail="Error getting firmware")


def parse_range_header(range_header: str) -> tuple[int, int | None]:
    """Returns the start offset and the inclusive end offset (None for open-ended ranges)."""
    start, end = 0, None
    try:
        if range_header:
            start_end = range_header.partition("=")[2].split("-")
            start = int(start_end[0])
            end = int(start_end[1]) if start_end[1] else None
    except (ValueError, IndexError):
        raise HTTPException(status_code=400, detail="Invalid Range header")
    if end is not None and start > end:
        raise HTTPException(
            status_code=400,
            detail="Invalid Range header: start cannot be greater than end",
        )
    return start, end


async def read_file(file_path: str, start: int, length: int) -> bytes:
    try:
        async with aiofiles.open(file_path, mode="rb") as file:
            await file.seek(start)
            return await file.read(length)
    except Exception as e:
        logging.error(f"read_file: Error reading file: {e}")
        raise HTTPException(status_code=500, detail="Error reading file")


def get_manufacturer_id(manufacturer_name: str) -> int | None:
    key = manufacturer_name.lower()
    if key in manufacturer_ids:
        return manufacturer_ids[key]
    rows = pgquery.query_db(
        "SELECT manufacturer_id FROM public.manufacturers WHERE lower(name) = :name ORDER BY manufacturer_id LIMIT 1",
        {"name": key},
    )
    if not rows:
        return None
    manufacturer_ids[key] = rows[0][0]
    return manufacturer_ids[key]


def truncate(value: str | None) -> str:
    return (value or "")[:MAX_LOG_FIELD_LENGTH]


def normalize_ip(host: str | None) -> str:
    try:
        return str(ipaddress.ip_address(host))
    except ValueError:
        return "0.0.0.0"


def removed_old_logs(serialnum: str):
    try:
        # Keep only the newest MAX_COUNT successful requests for this OBU
        pgquery.write_db(
            "DELETE FROM public.obu_ota_requests WHERE request_id IN ("
            "SELECT request_id FROM public.obu_ota_requests "
            "WHERE obu_sn = :obu_sn AND error_status = FALSE "
            "ORDER BY request_datetime DESC, request_id DESC OFFSET :max_count)",
            {"obu_sn": serialnum, "max_count": obu_ota_server_environment.MAX_COUNT},
        )
    except Exception:
        logging.exception("removed_old_logs: Error removing old entries")


def write_request_log(
    manufacturer_name: str,
    serialnum: str,
    version: str,
    origin_ip: str,
    firmware_id: str,
    error_status: bool,
    error_message: str,
):
    manufacturer_id = get_manufacturer_id(manufacturer_name)
    if manufacturer_id is None:
        logging.error(
            f"write_request_log: Manufacturer '{manufacturer_name}' does not exist, skipping request log"
        )
        return

    pgquery.write_db(
        "INSERT INTO public.obu_ota_requests (obu_sn, manufacturer, request_datetime, origin_ip, "
        "obu_firmware_version, requested_firmware_version, error_status, error_message) VALUES "
        "(:obu_sn, :manufacturer, :request_datetime, CAST(:origin_ip AS inet), :obu_firmware_version, "
        ":requested_firmware_version, :error_status, :error_message)",
        {
            "obu_sn": serialnum,
            "manufacturer": manufacturer_id,
            "request_datetime": datetime.now(timezone.utc).replace(tzinfo=None),
            "origin_ip": origin_ip,
            "obu_firmware_version": version,
            "requested_firmware_version": truncate(firmware_id),
            "error_status": bool(error_status),
            "error_message": truncate(error_message),
        },
    )
    removed_old_logs(serialnum)


async def log_request(
    manufacturer_name: str,
    request: Request,
    firmware_id: str,
    error_status: bool,
    error_message: str,
):
    try:
        query_params = request.query_params
        serialnum = truncate(query_params.get("serialnum"))
        version = truncate(query_params.get("version"))
        origin_ip = normalize_ip(request.client.host if request.client else None)

        await asyncio.to_thread(
            write_request_log,
            manufacturer_name,
            serialnum,
            version,
            origin_ip,
            firmware_id,
            error_status,
            error_message,
        )
    except Exception:
        logging.exception("log_request: Error logging request")


def schedule_log_request(*args):
    task = asyncio.create_task(log_request(*args))
    background_tasks.add(task)
    task.add_done_callback(background_tasks.discard)


@app.get(
    "/firmwares/commsignia/{firmware_id}",
    dependencies=[Depends(authenticate_user)],
    response_class=Response,
    responses={
        200: {"description": "The complete firmware file"},
        206: {"description": "The requested byte range of the firmware file"},
        400: {"description": "Invalid Range header"},
        404: {"description": "Firmware not found"},
        416: {"description": "Requested range starts beyond the end of the file"},
        500: {"description": "Firmware could not be read"},
    },
)
async def get_fw(request: Request, firmware_id: str):
    try:
        # Only names that follow the manifest convention can be served. This also
        # prevents the requested id from escaping the firmware directory.
        if not commsignia_manifest.is_valid_firmware_name(firmware_id):
            raise HTTPException(status_code=404, detail="Firmware not found")

        file_path = os.path.join(FIRMWARE_DIR, firmware_id)

        # Checks if firmware exists locally or downloads it from GCS
        if not await asyncio.to_thread(get_firmware, firmware_id, file_path):
            raise HTTPException(status_code=404, detail="Firmware not found")

        file_size = await asyncio.to_thread(os.path.getsize, file_path)
        range_header = request.headers.get("Range")
        headers = {"Accept-Ranges": "bytes"}

        if range_header:
            start, end = parse_range_header(range_header)
            if start >= file_size:
                raise HTTPException(
                    status_code=416,
                    detail="Requested range not satisfiable",
                    headers={"Content-Range": f"bytes */{file_size}"},
                )
            end = file_size - 1 if end is None else min(end, file_size - 1)
            data = await read_file(file_path, start, end - start + 1)
            status_code = 206
            headers["Content-Range"] = f"bytes {start}-{end}/{file_size}"
        else:
            data = await read_file(file_path, 0, file_size)
            status_code = 200

        headers["Content-Length"] = str(len(data))

        schedule_log_request(commsignia_manufacturer_name, request, firmware_id, False, "")
        return Response(
            content=data,
            status_code=status_code,
            media_type="application/octet-stream",
            headers=headers,
        )
    except HTTPException as e:
        # Expected client errors such as unknown firmware or invalid ranges
        schedule_log_request(
            commsignia_manufacturer_name, request, firmware_id, True, str(e)
        )
        logging.warning(
            f"get_fw: Responded with {e.status_code} for firmware_id: {firmware_id}"
        )
        raise
    except Exception as e:
        schedule_log_request(
            commsignia_manufacturer_name, request, firmware_id, True, str(e)
        )
        logging.exception(f"get_fw: Error responding with firmware_id: {firmware_id}")
        raise
