from pathlib import Path
import abc
import download_blob
import logging
import os
import requests
import shutil

class BaseUpgraderInterface( abc.ABC ):
  # Deletes the parent directory of the target local firmware file, this should be the device-ip directory
  @abc.abstractclassmethod
  def cleanup(self, local_file_name):
    shutil.rmtree(local_file_name[:local_file_name.rfind("/")])

  # Downloads firmware install package blob to specified destination file
  # Creates parent directory if it doesn't exist - parent directory should be "/home/device-ip/"
  @abc.abstractclassmethod
  def download_blob(self, blob_name, local_file_name):
    # Create parent directory that may not exist, only the immediate parent
    # Example: "/home/device-ip/blob.blob" would create the device-ip directory
    path = local_file_name[:local_file_name.rfind("/")]
    Path(path).mkdir(exist_ok=True)

    # Download blob, defaults to GCP blob storage
    bsp = os.environ.get("BLOB_STORAGE_PROVIDER", "GCP")
    if bsp == "GCP":
      download_blob.download_gcp_blob(blob_name, local_file_name)
    else:
      logging.error("Unsupported blob storage provider")

  @abc.abstractclassmethod
  def notify_firmware_manager(self, rsu_ip, status):
    logging.info(f"Firmware upgrade script completed with status: {status}")
    url = 'http://127.0.0.1:8080/firmware_upgrade_completed'
    body = {"rsu_ip": rsu_ip, "status": status}
    #requests.post(url, json=body)
    logging.info(f"Completed firmware upgrade with status: {status}")

  @abc.abstractclassmethod
  def upgrade(self, upgrade_info):
    pass