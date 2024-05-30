from fastapi import FastAPI, Request, Response, HTTPException, Depends
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from fastapi.middleware.cors import CORSMiddleware
from common import gcs_utils
import commsginia_manifest
import os
import glob
import aiofiles
from starlette.responses import Response
import logging

app = FastAPI()
log_level = "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]

logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

security = HTTPBasic()


def authenticate_user(credentials: HTTPBasicCredentials = Depends(security)):
    correct_username = os.getenv("BASIC_AUTH_USERNAME")
    correct_password = os.getenv("BASIC_AUTH_PASSWORD")
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


def get_firmware_list():
    blob_storage_provider = os.getenv("BLOB_STORAGE_PROVIDER", "LOCAL")
    files = []
    file_extension = ".tar.sig"
    if blob_storage_provider.upper() == "LOCAL":
        files = glob.glob(f"/firmwares/*{file_extension}")
    elif blob_storage_provider.upper() == "GCP":
        blob_storage_path = os.getenv("BLOB_STORAGE_PATH", "LOCAL")
        files = gcs_utils.list_gcs_blobs(blob_storage_path, file_extension)
    return files


@app.get("/firmwares/commsignia", dependencies=[Depends(authenticate_user)])
async def get_manifest(request: Request):
    try:
        files = get_firmware_list()
        host_name = os.getenv("SERVER_HOST", "localhost")
        response_manifest = commsginia_manifest.add_contents(host_name, files)
        return response_manifest
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def get_firmware(firmware_id: str, local_file_path: str):
    try:
        blob_storage_provider = os.getenv("BLOB_STORAGE_PROVIDER", "LOCAL")
        # checks if firmware exists locally
        if not os.path.exists(local_file_path):
            # If configured to only use local storage, return False as firmware does not exist
            if blob_storage_provider.upper() == "LOCAL":
                return False
            # If configured to use GCP storage, download the firmware from GCP
            elif blob_storage_provider.upper() == "GCP":
                # Download blob will attempt to download the firmware and return True if successful
                return gcs_utils.download_gcp_blob(firmware_id, local_file_path)
        return True
    except Exception as e:
        logging.error(f"Error getting firmware: {e}")
        raise HTTPException(status_code=500, detail="Error getting firmware")


def parse_range_header(range_header):
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


async def read_file(file_path, start, end):
    try:
        async with aiofiles.open(file_path, mode="rb") as file:
            file_size = os.path.getsize(file_path)
            if end is None:
                end = file_size
            await file.seek(start)
            data = await file.read(end - start)
        return data, file_size, end
    except Exception as e:
        logging.error(f"Error reading file: {e}")
        raise HTTPException(status_code=500, detail="Error reading file")


@app.get(
    "/firmwares/commsignia/{firmware_id}", dependencies=[Depends(authenticate_user)]
)
async def get_fw(request: Request, firmware_id: str):
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

    return Response(
        content=data, media_type="application/octet-stream", headers=headers
    )
