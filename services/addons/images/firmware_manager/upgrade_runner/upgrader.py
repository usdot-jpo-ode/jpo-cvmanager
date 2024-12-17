from pathlib import Path
import abc
import subprocess
import time
from common import gcs_utils
import logging
import os
import requests
import shutil
from common.emailSender import EmailSender
from common.email_util import get_email_list_from_rsu
import download_blob


class UpgraderAbstractClass(abc.ABC):
    def __init__(self, upgrade_info, firmware_extension):
        self.install_package = upgrade_info["install_package"]
        self.root_path = f"/home/{upgrade_info['ipv4_address']}"
        self.blob_name = f"{upgrade_info['manufacturer']}/{upgrade_info['model']}/{upgrade_info['target_firmware_version']}/{upgrade_info['install_package']}"
        self.local_file_name = (
            f"/home/{upgrade_info['ipv4_address']}/{upgrade_info['install_package']}"
        )
        self.rsu_ip = upgrade_info["ipv4_address"]
        self.ssh_username = upgrade_info["ssh_username"]
        self.ssh_password = upgrade_info["ssh_password"]
        self.firmware_extension = firmware_extension

    # Deletes the parent directory along with the firmware file
    def cleanup(self):
        if self.local_file_name is not None:
            path = Path(self.root_path)
            if path.exists() and path.is_dir():
                shutil.rmtree(path)

    # Downloads firmware install package blob to /home/rsu_ip/
    def download_blob(self, blob_name=None, local_file_name=None, firmware_extension=None):
        # Create parent rsu_ip directory
        path = self.local_file_name[: self.local_file_name.rfind("/")]
        Path(path).mkdir(exist_ok=True)
        blob_name = self.blob_name if blob_name is None else blob_name
        local_file_name = (
            self.local_file_name if local_file_name is None else local_file_name
        )

        # Download blob, defaults to GCP blob storage
        bspCaseInsensitive = os.environ.get(
            "BLOB_STORAGE_PROVIDER", "DOCKER"
        ).casefold()
        if bspCaseInsensitive == "gcp":
            return gcs_utils.download_gcp_blob(blob_name, local_file_name, self.firmware_extension) if firmware_extension is None else gcs_utils.download_gcp_blob(blob_name, local_file_name, firmware_extension)
        elif bspCaseInsensitive == "docker":
            return download_blob.download_docker_blob(blob_name, local_file_name)
        else:
            logging.error("Unsupported blob storage provider")
            raise StorageProviderNotSupportedException

    # Notifies the firmware manager of the completion status for the upgrade
    # success is a boolean
    def notify_firmware_manager(self, success):
        status = "success" if success else "fail"
        logging.info(f"Firmware upgrade script completed for {self.rsu_ip} with status: {status}")

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

    def check_online(self):
        iter = 0
        # Ping once every second for 5 seconds to verify RSU is online
        while iter < 5:
            code = subprocess.run(
                ["ping", "-n", "-c1", self.rsu_ip], capture_output=True
            ).returncode
            if code == 0:
                return True
            iter += 1
            time.sleep(1)
        # 5 seconds pass with no response
        return False

    def send_error_email(self, type="Firmware Upgrader", err=""):
        try:
            email_addresses = get_email_list_from_rsu(
                "Firmware Upgrade Failures", self.rsu_ip
            )

            subject = (
                f"{self.rsu_ip} Firmware Upgrader Failure"
                if type == "Firmware Upgrader"
                else f"{self.rsu_ip} Firmware Upgrader Post Upgrade Script Failure"
            )

            for email_address in email_addresses:
                emailSender = EmailSender(
                    os.environ["SMTP_SERVER_IP"],
                    587,
                )
                emailSender.send(
                    sender=os.environ["SMTP_EMAIL"],
                    recipient=email_address,
                    subject=subject,
                    message=f"{type}: Failed to perform update on RSU {self.rsu_ip} due to the following error: {err}",
                    replyEmail="",
                    username=os.environ["SMTP_USERNAME"],
                    password=os.environ["SMTP_PASSWORD"],
                    pretty=True,
                )
        except Exception as e:
            logging.error(e)

    # This needs to be defined for each implementation
    @abc.abstractclassmethod
    def upgrade(self):
        pass


class StorageProviderNotSupportedException(Exception):
    def __init__(self):
        super().__init__("Unsupported blob storage provider")
