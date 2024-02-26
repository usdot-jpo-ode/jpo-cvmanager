from paramiko import SSHClient, WarningPolicy
from scp import SCPClient
import upgrader
import json
import logging
import os
import sys


class CommsigniaUpgrader(upgrader.UpgraderAbstractClass):
    def __init__(self, upgrade_info):
        super().__init__(upgrade_info)

    def upgrade(self):
        try:
            # Download firmware installation package
            self.download_blob()

            # Make connection with the target device
            logging.info("Making SSH connection with the device...")
            ssh = SSHClient()
            ssh.set_missing_host_key_policy(WarningPolicy)
            ssh.connect(
                self.rsu_ip,
                username=self.ssh_username,
                password=self.ssh_password,
                look_for_keys=False,
                allow_agent=False,
            )

            # Make SCP client to copy over the firmware installation package to the /tmp/ directory on the remote device
            logging.info("Copying installation package to the device...")
            scp = SCPClient(ssh.get_transport())
            scp.put(self.local_file_name, remote_path="/tmp/")
            scp.close()

            # Delete local installation package and its parent directory so it doesn't take up storage space
            self.cleanup()

            # Run firmware upgrade and reboot
            logging.info("Running firmware upgrade...")
            _stdin, _stdout, _stderr = ssh.exec_command(
                f"signedUpgrade.sh /tmp/{self.install_package}"
            )
            decoded_stdout = _stdout.read().decode()
            logging.info(decoded_stdout)
            if "ALL OK" not in decoded_stdout:
                ssh.close()
                # Notify Firmware Manager of failed firmware upgrade completion
                self.notify_firmware_manager(success=False)
                return
            ssh.exec_command("reboot")
            ssh.close()

            # Notify Firmware Manager of successful firmware upgrade completion
            self.notify_firmware_manager(success=True)
        except Exception as err:
            # If something goes wrong, cleanup anything left and report failure if possible
            logging.error(f"Failed to perform firmware upgrade: {err}")
            self.cleanup()
            self.notify_firmware_manager(success=False)


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
    commsignia_upgrader = CommsigniaUpgrader(upgrade_info)
    commsignia_upgrader.upgrade()
