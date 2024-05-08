from pathlib import Path
import abc
import subprocess
import time
from common import gcs_utils
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
    def download_blob(self, blob_name=None, local_file_name=None):
        # Create parent rsu_ip directory
        path = self.local_file_name[: self.local_file_name.rfind("/")]
        Path(path).mkdir(exist_ok=True)

        # Download blob, defaults to GCP blob storage
        bsp = os.environ.get("BLOB_STORAGE_PROVIDER", "GCP")
        if bsp == "GCP":
            blob_name = self.blob_name if blob_name is None else blob_name
            local_file_name = (
                self.local_file_name if local_file_name is None else local_file_name
            )
            return gcs_utils.download_gcp_blob(blob_name, local_file_name)
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

    def wait_until_online(self):
        iter = 0
        # Ping once every second for 3 minutes until online
        while iter < 180:
            time.sleep(1)
            code = subprocess.run(
                ["ping", "-n", "-c1", self.rsu_ip], capture_output=True
            ).returncode
            if code == 0:
                return 0
            iter += 1
        # 3 minutes pass with no response
        return -1

    # This needs to be defined for each implementation
    @abc.abstractclassmethod
    def upgrade(self):
        pass
