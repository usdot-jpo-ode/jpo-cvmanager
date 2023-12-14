import logging
import sys
import json
import base_upgrader

class CommsigniaUpgrader( base_upgrader.BaseUpgraderInterface ):
  def download_blob(self, blob_name, destination_file_name):
    super().download_blob(blob_name, destination_file_name)

  def notify_firmware_manager(self, status):
    super().notify_firmware_manager(status)

  def upgrade(self, upgrade_info):
    logging.info(upgrade_info)


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
  upgrade_info = json.loads(sys.argv[1])
  CommsigniaUpgrader().upgrade(upgrade_info)
