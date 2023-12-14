import abc
import logging
import os
import download_blob

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
  def notify_firmware_manager(self, status):
    pass

  @abc.abstractclassmethod
  def upgrade(self, upgrade_info):
    pass