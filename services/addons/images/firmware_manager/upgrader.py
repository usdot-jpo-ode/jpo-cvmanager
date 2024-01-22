from pathlib import Path
import abc
import download_blob
import logging
import os
import requests
import shutil


class UpgraderAbstractClass(abc.ABC):
    def __init__(self, upgrade_info):
        self.install_package = upgrade_info["install_package"]
        self.root_path = f"/home/{upgrade_info['ipv4_address']}"
        self.blob_name = f"{upgrade_info['manufacturer']}/{upgrade_info['model']}/{upgrade_info['target_firmware_version']}/{upgrade_info['install_package']}"
        self.local_file_name = (
            f"/home/{upgrade_info['ipv4_address']}/{upgrade_info['install_package']}"
        )
        self.rsu_ip = upgrade_info["ipv4_address"]
        self.ssh_username = upgrade_info["ssh_username"]
        self.ssh_password = upgrade_info["ssh_password"]

    # Deletes the parent directory along with the firmware file
    def cleanup(self):
        if self.local_file_name is not None:
            path = Path(self.root_path)
            if path.exists() and path.is_dir():
                shutil.rmtree(path)

    # Downloads firmware install package blob to /home/rsu_ip/
    def download_blob(self):
        # Create parent rsu_ip directory
        path = self.local_file_name[: self.local_file_name.rfind("/")]
        Path(path).mkdir(exist_ok=True)

        # Download blob, defaults to GCP blob storage
        bsp = os.environ.get("BLOB_STORAGE_PROVIDER", "GCP")
        if bsp == "GCP":
            download_blob.download_gcp_blob(self.blob_name, self.local_file_name)
        else:
            logging.error("Unsupported blob storage provider")

    # Notifies the firmware manager of the completion status for the upgrade
    # success is a boolean
    def notify_firmware_manager(self, success):
        status = "success" if success else "fail"
        logging.info(f"Firmware upgrade script completed with status: {status}")

        url = "http://127.0.0.1:8080/firmware_upgrade_completed"
        body = {"rsu_ip": self.rsu_ip, "status": status}
        try:
            requests.post(url, json=body)
        except Exception as err:
            logging.error(
                f"Failed to connect to the Firmware Manager API for '{self.rsu_ip}': {err}"
            )

    # This needs to be defined for each implementation
    @abc.abstractclassmethod
    def upgrade(self):
        pass
