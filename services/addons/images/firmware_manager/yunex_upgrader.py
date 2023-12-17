import upgrader
import json
import logging
import os
import sys

class YunexUpgrader( upgrader.UpgraderAbstractClass ):
  def __init__(self, upgrade_info):
    super().__init__(upgrade_info)

  def upgrade(self):
    try:
      # Download firmware installation package
      super().download_blob()

      # Perform upgrade
      # TODO

      # Delete local installation package and its parent directory so it doesn't take up storage space
      super().cleanup()

      # Notify Firmware Manager of successful firmware upgrade completion
      super().notify_firmware_manager("success")
    except Exception as err:
      # If something goes wrong, cleanup anything left and report failure if possible
      logging.error(f"Failed to perform firmware upgrade: {err}")
      self.cleanup()
      self.notify_firmware_manager("fail")


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
