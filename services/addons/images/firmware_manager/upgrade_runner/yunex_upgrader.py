import upgrader
import json
import logging
import os
import subprocess
import sys
import tarfile
import time


class YunexUpgrader(upgrader.UpgraderAbstractClass):
    def __init__(self, upgrade_info):
        super().__init__(upgrade_info, firmware_extension=".tar")

    def run_xfer_upgrade(self, file_name):
        xfer_command = [
            "java",
            "-jar",
            f"/home/tools/xfer_yunex.jar",
            "-upload",
            file_name,
            f"{self.rsu_ip}:3600",
        ]
        proc = subprocess.run(xfer_command, capture_output=True)
        code, stdout, stderr = proc.returncode, proc.stdout, proc.stderr

        # If the command ends with a non-successful status code, return -1
        if code != 0:
            logging.error(
                "Firmware not successful for "
                + self.rsu_ip
                + ": "
                + stderr.decode("utf-8")
            )
            return -1

        output_lines = stdout.decode("utf-8").split("\n")[:-1]
        # If the command ends with a successful status code but the logs don't contain the expected line, return -1
        if (
            'TEXT: {"success":{"upload":"Processing OK. Rebooting now ..."}}'
            not in output_lines
        ):
            logging.error(
                "Firmware not successful for "
                + self.rsu_ip
                + ": "
                + stderr.decode("utf-8")
            )
            return -1

        # If everything goes as expected, the XFER upgrade was complete
        return 0

    def upgrade(self):
        try:
            # Download firmware installation package TAR file
            self.download_blob()

            # Unpack TAR file which must contain the following:
            # - Core upgrade file
            # - SDK upgrade file
            # - Application provision file
            # - upgrade_info.json which defines the files as a single JSON object
            logging.info("Unpacking TAR file prior to upgrading " + self.rsu_ip + "...")
            with tarfile.open(self.local_file_name, "r") as tar:
                tar.extractall(self.root_path)

            # Obtain upgrade info in the following format:
            # { "core": "core-file-name", "sdk": "sdk-file-name", "provision": "provision-file-name"}
            with open(f"{self.root_path}/upgrade_info.json") as json_file:
                upgrade_info = json.load(json_file)

            # Run Core upgrade
            logging.info("Running Core firmware upgrade for " + self.rsu_ip + "...")
            code = self.run_xfer_upgrade(f"{self.root_path}/{upgrade_info['core']}")
            if code == -1:
                raise Exception("Yunex RSU Core upgrade failed")
            if self.wait_until_online() == -1:
                raise Exception("RSU offline for too long after Core upgrade")
            # Wait an additional 60 seconds after the Yunex RSU is online - needs time to initialize
            time.sleep(60)

            # Run SDK upgrade
            logging.info("Running SDK firmware upgrade for " + self.rsu_ip + "...")
            code = self.run_xfer_upgrade(f"{self.root_path}/{upgrade_info['sdk']}")
            if code == -1:
                raise Exception("Yunex RSU SDK upgrade failed")
            if self.wait_until_online() == -1:
                raise Exception("RSU offline for too long after SDK upgrade")
            # Wait an additional 60 seconds after the Yunex RSU is online - needs time to initialize
            time.sleep(60)

            # Run application provision image
            logging.info("Running application provisioning for " + self.rsu_ip + "...")
            code = self.run_xfer_upgrade(
                f"{self.root_path}/{upgrade_info['provision']}"
            )
            if code == -1:
                raise Exception("Yunex RSU application provisioning upgrade failed")

            # Notify Firmware Manager of successful firmware upgrade completion
            self.cleanup()
            self.notify_firmware_manager(success=True)
        except Exception as err:
            # If something goes wrong, cleanup anything left and report failure if possible.
            # Yunex RSUs can handle having the same firmware upgraded over again.
            # There is no issue with starting from the beginning even with a partially complete upgrade.
            logging.error(
                f"Failed to perform firmware upgrade for {self.rsu_ip}: {err}"
            )
            self.cleanup()
            self.notify_firmware_manager(success=False)
            # send email to support team with the rsu and error
            self.send_error_email("Firmware Upgrader", err)


# sys.argv[1] - JSON string with the following key-values:
# - ipv4_address
# - manufacturer
# - model
# - ssh_username
# - ssh_password
# - target_firmware_id
# - target_firmware_version
# - install_package
if __name__ == "__main__":
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)
    # Trimming outer single quotes from the json.loads
    upgrade_info = json.loads(sys.argv[1][1:-1])
    yunex_upgrader = YunexUpgrader(upgrade_info)
    if yunex_upgrader.check_online():
        yunex_upgrader.upgrade()
    else:
        logging.error(f"RSU {upgrade_info['ipv4_address']} is offline")
        yunex_upgrader.notify_firmware_manager(success=False)
