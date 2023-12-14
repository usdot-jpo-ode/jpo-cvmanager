import abc
import download_blob
import logging
import os
import requests

class BaseUpgraderInterface( abc.ABC ):
  # Downloads firmware install package blob to specified destination file
  @abc.abstractclassmethod
  def download_blob(self, blob_name, destination_file_name):
    # Defaults to GCP blob storage
    bsp = os.environ.get("BLOB_STORAGE_PROVIDER", "GCP")
    if bsp == "GCP":
      download_blob.download_gcp_blob(blob_name, destination_file_name)
    else:
      logging.error("Unsupported blob storage provider")

  @abc.abstractclassmethod
  def notify_firmware_manager(self, rsu_ip, status):
    logging.info(f"Firmware upgrade script completed with status: {status}")
    url = 'http://127.0.0.1:8080/firmware_upgrade_completed'
    body = {"rsu_ip": rsu_ip, "status": status}
    requests.post(url, json=body)

  @abc.abstractclassmethod
  def upgrade(self, upgrade_info):
    pass