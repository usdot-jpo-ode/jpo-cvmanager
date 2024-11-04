from typing import Any
from fastapi import FastAPI, Request, Response, HTTPException, Depends
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from common import gcs_utils, pgquery
import commsignia_manifest
import os
import glob
import aiofiles
from starlette.responses import Response
import logging
from datetime import datetime
import asyncio

app = FastAPI()
log_level = "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]
logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)
security = HTTPBasic()

commsignia_file_ext = ".tar.sig"


def authenticate_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    correct_username = os.getenv("OTA_USERNAME")
    correct_password = os.getenv("OTA_PASSWORD")
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
    blob_storage_provider = os.getenv("BLOB_STORAGE_PROVIDER", "DOCKER")
    files = []
    file_extension = commsignia_file_ext
    if blob_storage_provider.upper() == "DOCKER":
        files = glob.glob(f"/firmwares/*{file_extension}")
    elif blob_storage_provider.upper() == "GCP":
        blob_storage_path = os.getenv("BLOB_STORAGE_PATH", "DOCKER")
        files = gcs_utils.list_gcs_blobs(blob_storage_path, file_extension)
    return files


def get_host_name() -> str:
    host_name = os.getenv("SERVER_HOST", "localhost")
    tls_enabled = os.getenv("NGINX_ENCRYPTION", "plain")
    if tls_enabled.lower() == "ssl":
        host_name = "https://" + host_name
    else:
        host_name = "http://" + host_name
    return host_name


@app.get("/firmwares/commsignia", dependencies=[Depends(authenticate_user)])
async def get_manifest(request: Request) -> dict[str, Any]:
    try:
        files = get_firmware_list()
        logging.debug(f"get_manifest :: Files: {files}")
        host_name = get_host_name()
        response_manifest = commsignia_manifest.add_contents(host_name, files)
        return response_manifest
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def get_firmware(firmware_id: str, local_file_path: str) -> bool:
    try:
        blob_storage_provider = os.getenv("BLOB_STORAGE_PROVIDER", "DOCKER")
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
    except Exception as e:
        logging.error(f"parse_range_header: Error getting firmware: {e}")
        raise HTTPException(status_code=500, detail="Error getting firmware")


def parse_range_header(range_header: str) -> tuple[int, int | None]:
    start, end = 0, None
    try:
        if range_header:
            start_end = range_header.partition("=")[2].split("-")
            start = int(start_end[0])
            end = int(start_end[1]) if start_end[1] else None
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid Range header")
    if end is not None and start > end:
        raise HTTPException(
            status_code=400,
            detail="Invalid Range header: start cannot be greater than end",
        )
    return start, end


async def read_file(
    file_path: str, start: int, end: int | None
) -> tuple[bytes, int, int]:
    try:
        async with aiofiles.open(file_path, mode="rb") as file:
            file_size = os.path.getsize(file_path)
            if end is None:
                end = file_size
            await file.seek(start)
            data = await file.read(end - start)
        return data, file_size, end
    except Exception as e:
        logging.error(f"read_file: Error reading file: {e}")
        raise HTTPException(status_code=500, detail="Error reading file")


def removed_old_logs(serialnum: str):
    try:
        max_count = int(os.getenv("MAX_COUNT", 10))
        success_count = pgquery.query_db(
            f"SELECT COUNT(*) FROM public.obu_ota_requests WHERE obu_sn = '{serialnum}' AND error_status = B'0'"
        )
        if success_count[0][0] > max_count:
            excess_count = success_count[0][0] - max_count
            oldest_entries = pgquery.query_db(
                f"SELECT request_id FROM public.obu_ota_requests WHERE obu_sn = '{serialnum}' AND error_status = B'0' ORDER BY request_datetime ASC LIMIT {excess_count}"
            )
            oldest_ids = [entry[0] for entry in oldest_entries]
            pgquery.write_db(
                f"DELETE FROM public.obu_ota_requests WHERE request_id IN ({','.join(map(str, oldest_ids))})"
            )
            logging.debug(
                f"removed_old_logs: Removed {excess_count} old logs for serialnum: {serialnum}"
            )
    except Exception as e:
        logging.error(f"removed_old_logs: Error removing old entry: {e}")


async def log_request(
    manufacturer: int,
    request: Request,
    firmware_id: str,
    error_status: int,
    error_message: str,
):
    try:
        query_params = request.query_params
        serialnum = query_params.get("serialnum")
        version = query_params.get("version")

        origin_ip = request.client.host

        current_dt = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        query = f"INSERT INTO public.obu_ota_requests (obu_sn, manufacturer, request_datetime, origin_ip, obu_firmware_version, requested_firmware_version, error_status, error_message) VALUES\
                ('{serialnum}', {manufacturer}, '{current_dt}', '{origin_ip}', '{version}', '{firmware_id}', B'{error_status}', '{error_message}')"
        logging.debug(f"Logging request to postgres with insert query: \n{query}")
        pgquery.write_db(query)
        removed_old_logs(serialnum)
    except Exception as e:
        logging.error(f"log_request: Error logging request: {e} with query: {query}")


@app.get(
    "/firmwares/commsignia/{firmware_id}", dependencies=[Depends(authenticate_user)]
)
async def get_fw(request: Request, firmware_id: str):
    try:
        file_path = f"/firmwares/{firmware_id}"

        # Checks if firmware exists locally or downloads it from GCS
        if not get_firmware(firmware_id, file_path):
            raise HTTPException(status_code=404, detail="Firmware not found")

        header_start, header_end = parse_range_header(request.headers.get("Range"))
        data, file_size, end = await read_file(file_path, header_start, header_end)

        headers = {
            "Content-Range": f"bytes {header_start}-{end-1}/{file_size}",
            "Content-Length": str(end - header_start),
            "Accept-Ranges": "bytes",
        }

        asyncio.create_task(log_request(1, request, firmware_id, 0, ""))
        return Response(
            content=data, media_type="application/octet-stream", headers=headers
        )
    except Exception as e:
        asyncio.create_task(log_request(1, request, firmware_id, 1, e.detail))
        logging.error(
            f"get_fw: Error responding with firmware with error: {e} for firmware_id: {firmware_id}"
        )
        raise
