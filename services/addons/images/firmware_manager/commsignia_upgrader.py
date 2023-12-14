from paramiko import SSHClient, WarningPolicy
from pathlib import Path
from scp import SCPClient
import base_upgrader
import json
import logging
import sys

class CommsigniaUpgrader( base_upgrader.BaseUpgraderInterface ):
  def download_blob(self, blob_name, destination_file_name):
    super().download_blob(blob_name, destination_file_name)

  def notify_firmware_manager(self, rsu_ip, status):
    super().notify_firmware_manager(rsu_ip, status)

  def upgrade(self, upgrade_info):
    # Download firmware installation package
    logging.info("Downloading blob...")
    blob_name = f"{upgrade_info['manufacturer']}/{upgrade_info['model']}/{upgrade_info['target_firmware_name']}/{upgrade_info['install_package']}"
    local_file_name = f"/home/{upgrade_info['install_package']}"
    self.download_blob(blob_name, local_file_name)

    # Make connection with the target device
    logging.info("Making SSH connection with the device...")
    ssh = SSHClient()
    ssh.set_missing_host_key_policy(WarningPolicy)
    ssh.connect(upgrade_info['ipv4_address'], username=upgrade_info['ssh_username'], password=upgrade_info['ssh_password'], look_for_keys=False, allow_agent=False)

    # Make SCP client to copy over the firmware installation package to the /tmp/ directory on the remote device
    logging.info("Copying installation package to the device...")
    scp = SCPClient(ssh.get_transport())
    scp.put(local_file_name, remote_path='/tmp/')
    scp.close()
    # Delete the local installation package so it doesn't take up storage space
    p = Path(local_file_name)
    p.unlink()

    # Run firmware upgrade and reboot
    logging.info("Running firmware upgrade...")
    _stdin, _stdout,_stderr = ssh.exec_command(f"signedUpgrade.sh /tmp/{upgrade_info['install_package']}")
    output = _stdout.read().decode()
    logging.info(output)
    if "ALL OK" not in output:
      ssh.close()
      self.notify_firmware_manager(upgrade_info['ipv4_address'], "fail")
    ssh.exec_command("reboot")
    ssh.close()
    self.notify_firmware_manager(upgrade_info['ipv4_address'], "success")


# sys.argv[1] - JSON string with the following key-values:
# - ipv4_address
# - manufacturer
# - model
# - ssh_username
# - ssh_password
# - target_firmware_id
# - target_firmware_name
# - install_package
if __name__ == "__main__":
  logging.info("Commsignia Upgrader initiated")
  upgrade_info = json.loads(sys.argv[1])
  CommsigniaUpgrader().upgrade(upgrade_info)
