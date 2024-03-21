import subprocess
import time
from paramiko import SSHClient, WarningPolicy
from scp import SCPClient
import upgrader
import json
import logging
import os
import sys


class CommsigniaUpgrader(upgrader.UpgraderAbstractClass):
    def __init__(self, upgrade_info):
        # set file/blob location for post_upgrade script
        self.post_upgrade_file_name = f"/home/{upgrade_info['ipv4_address']}/post_upgrade.sh"
        self.post_upgrade_blob_name = f"{upgrade_info['manufacturer']}/{upgrade_info['model']}/{upgrade_info['target_firmware_version']}/post_upgrade.sh"
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

            # If post_upgrade script exists execute it
            if (self.download_blob(self.post_upgrade_blob_name, self.post_upgrade_file_name)):
                self.post_upgrade()

            # Delete local installation package and its parent directory so it doesn't take up storage space
            self.cleanup()

            # Notify Firmware Manager of successful firmware upgrade completion
            self.notify_firmware_manager(success=True)
        except Exception as err:
            # If something goes wrong, cleanup anything left and report failure if possible
            logging.error(f"Failed to perform firmware upgrade: {err}")
            self.cleanup()
            self.notify_firmware_manager(success=False)

    def post_upgrade(self):
        if self.wait_until_online() == -1:
            raise Exception("RSU offline for too long after firmware upgrade")
        try:
            time.sleep(60)
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

            # Make SCP client to copy over the post upgrade script to the /tmp/ directory on the remote device
            logging.info("Copying post upgrade script to the device...")
            scp = SCPClient(ssh.get_transport())
            scp.put(self.post_upgrade_file_name, remote_path="/tmp/")
            scp.close()

            # Change permissions and execute post upgrade script
            logging.info("Running post upgrade script...")
            ssh.exec_command(
                f"chmod +x /tmp/post_upgrade.sh"
            )
            _stdin, _stdout, _stderr = ssh.exec_command(
                f"/tmp/post_upgrade.sh"
            )
            decoded_stdout = _stdout.read().decode()
            logging.info(decoded_stdout)
            if "ALL OK" not in decoded_stdout:
                ssh.close()
                logging.error(f"Failed to execute post upgrade script for rsu {self.rsu_ip}: {decoded_stdout}")
                return
            ssh.close()
            logging.info(f"Post upgrade script executed successfully for rsu: {self.rsu_ip}.")
        except Exception as err:
            logging.error(f"Failed to execute post upgrade script for rsu {self.rsu_ip}: {err}")
            
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
