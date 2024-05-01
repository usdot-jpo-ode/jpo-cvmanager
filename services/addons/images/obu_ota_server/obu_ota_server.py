from fastapi import FastAPI, Request, Response, HTTPException
from services.common import gcs_utils
import commsginia_manifest
import os
import glob
import aiofiles
from starlette.responses import Response
import logging

app = FastAPI()
log_level = "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]

logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)


@app.get("/")
def read_root(request: Request):
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}


def get_firmware_list():
    blob_storage_provider = os.getenv("BLOB_STORAGE_PROVIDER", "LOCAL")
    files = []
    file_extension = ".tar.sig"
    if blob_storage_provider.upper() == "LOCAL":
        files = glob.glob(f"/firmwares/*{file_extension}")
    elif blob_storage_provider.upper() == "GCP":
        files = gcs_utils.list_gcs_blobs("firmwares", file_extension)
    return files


@app.get("/firmwares")
def get_manifest(request: Request):
    try:
        files = get_firmware_list()
        logging.debug(files)
        host_name = os.getenv("SERVER_HOST", "localhost")
        response_manifest = commsginia_manifest.add_contents(host_name, files)
        logging.debug(response_manifest)
        return response_manifest
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def get_firmware(firmware_id: str, local_file_path: str):
    blob_storage_provider = os.getenv("BLOB_STORAGE_PROVIDER", "LOCAL")
    # checks if firmware exists locally
    if not os.path.exists(local_file_path):
        # If configured to only use local storage, return False as firmware does not exist
        if blob_storage_provider.upper() == "LOCAL":
            return False
        # If configured to use GCP storage, download the firmware from GCP
        elif blob_storage_provider.upper() == "GCP":
            # Download blob will attempt to download the firmware and return True if successful
            return gcs_utils(firmware_id, local_file_path)
    return True


@app.get("/firmwares/{firmware_id}")
async def get_fw(request: Request, firmware_id: str):
    logging.debug(f"Method: {request.method}")
    logging.debug(f"URL: {request.url}")
    logging.debug(f"Headers: {request.headers}")
    logging.debug(f"Query Parameters: {request.query_params}")
    logging.debug(f"Client: {request.client}")
    logging.debug(f"Cookies: {request.cookies}")
    file_path = f"/firmwares/{firmware_id}"

    # Checks if firmware exists locally or downloads it from GCS
    if not get_firmware(firmware_id, file_path):
        raise HTTPException(status_code=404, detail="Firmware not found")

    range_header = request.headers.get("Range")
    start, end = 0, None

    if range_header:
        start_end = range_header.partition("=")[2].split("-")
        start = int(start_end[0])
        end = int(start_end[1]) if start_end[1] else None

    async with aiofiles.open(file_path, mode="rb") as file:
        file_size = os.path.getsize(file_path)
        if end is None:
            end = file_size
        await file.seek(start)
        data = await file.read(end - start)

    headers = {
        "Content-Range": f"bytes {start}-{end-1}/{file_size}",
        "Content-Length": str(end - start),
        "Accept-Ranges": "bytes",
    }

    logging.debug(f"Completed Firmware Response for lient: {request.client}")
    return Response(
        content=data, media_type="application/octet-stream", headers=headers
    )
