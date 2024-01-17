import upgrader
import json
import logging
import os
import subprocess
import sys
import tarfile
import time

class YunexUpgrader( upgrader.UpgraderAbstractClass ):
  def __init__(self, upgrade_info):
    super().__init__(upgrade_info)

  def run_xfer_upgrade(self, file_name):
    proc = subprocess.run(["java", "-jar", f"/home/tools/xfer_yunex.jar", "-upload", file_name, f"{self.rsu_ip}:3600"], capture_output=True)
    code, stdout, stderr = proc.returncode, proc.stdout, proc.stderr

    # If the command ends with a non-successful status code, return -1
    if code != 0:
      logging.error("Firmware not successful: " + stderr.decode("utf-8"))
      return -1

    output_lines = stdout.decode("utf-8").split('\n')[:-1]
    # If the command ends with a successful status code but the logs don't contain the expected line, return -1
    if 'TEXT: {"success":{"upload":"Processing OK. Rebooting now ..."}}' not in output_lines:
      logging.error("Firmware not successful: " + stderr.decode("utf-8"))
      return -1

    # If everything goes as expected, the XFER upgrade was complete
    return 0

  def wait_until_online(self):
    logging.info("Pinging RSU until online")
    iter = 0
    while iter < 100:
      code = subprocess.run(['ping', '-n', '-w5', '-c3', self.rsu_ip], capture_output=True).returncode
      if code == 0:
        return 0
      iter += 1
    return -1

  def upgrade(self):
    try:
      # Download firmware installation package TAR file
      self.download_blob()

      # Unpack TAR file which must contain the following:
      # - Core upgrade file
      # - SDK upgrade file
      # - Application provision file
      # - upgrade_info.json which defines the files
      logging.info("Unpacking TAR file...")
      with tarfile.open(self.local_file_name, 'r') as tar:
        tar.extractall(self.root_path)

      # Obtain upgrade info
      with open(f"{self.root_path}/upgrade_info.json") as json_file:
        upgrade_info = json.load(json_file)

      # Run Core upgrade
      logging.info("Running Core firmware upgrade...")
      code = self.run_xfer_upgrade(f"{self.root_path}/{upgrade_info['core']}")
      if code == -1:
        raise Exception("Yunex RSU Core upgrade failed")
      if self.wait_until_online() == -1:
        raise Exception("RSU offline for too long after Core upgrade")
      # Wait an additional 60 seconds after the Yunex RSU is online - needs time to initialize
      time.sleep(60)

      # Run SDK upgrade
      logging.info("Running SDK firmware upgrade...")
      code = self.run_xfer_upgrade(f"{self.root_path}/{upgrade_info['sdk']}")
      if code == -1:
        raise Exception("Yunex RSU SDK upgrade failed")
      if self.wait_until_online() == -1:
        raise Exception("RSU offline for too long after SDK upgrade")
      # Wait an additional 60 seconds after the Yunex RSU is online - needs time to initialize
      time.sleep(60)

      # Run application provision image
      logging.info("Running application provisioning...")
      code = self.run_xfer_upgrade(f"{self.root_path}/{upgrade_info['provision']}")
      if code == -1:
        raise Exception("Yunex RSU application provisioning upgrade failed")

      # Notify Firmware Manager of successful firmware upgrade completion
      self.cleanup()
      self.notify_firmware_manager(success=True)
    except Exception as err:
      # If something goes wrong, cleanup anything left and report failure if possible.
      # Yunex RSUs can handle having the same firmware upgraded over again.
      # There is no issue with starting from the beginning even with a partially complete upgrade.
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
  yunex_upgrader = YunexUpgrader(upgrade_info)
  yunex_upgrader.upgrade()
