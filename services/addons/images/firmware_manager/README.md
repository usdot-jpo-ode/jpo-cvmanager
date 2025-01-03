# Firmware Manager

## Table of Contents

- [Firmware Manager](#firmware-manager)
  - [Table of Contents](#table-of-contents)
  - [About ](#about-)
  - [Requirements ](#requirements-)
  - [Vendor Specific Requirements](#vendor-specific-requirements)
    - [Commsignia](#commsignia)
    - [Yunex](#yunex)

## About <a name = "about"></a>

This directory contains two microservices that run within the CV Manager GKE Cluster. The firmware manager upgrade scheduler monitors the CV Manager PostgreSQL database to determine if there are any RSUs that are targeted for a firmware upgrade. This monitoring is a once-per-hour, scheduled occurrence. Alternatively, this micro-service hosts a REST API for directly initiating firmware upgrades - this is used by the CV Manager API. Firmware upgrades then schedule off tasks to the firmware manager upgrade runner that is initiated through an HTTP request. This allows for better scaling for more parallel upgrades.

An RSU is determined to be ready for upgrade if its entry in the "rsus" table in PostgreSQL has its "target_firmware_version" set to be different than its "firmware_version". The Firmware Manager will ignore all devices with incompatible firmware upgrades set as their target firmware based on the "firmware_upgrade_rules" table. The CV Manager API will only offer CV Manager webapp users compatible options so this generally is a precaution.

Hosting firmware files is recommended to be done via the cloud. GCP cloud storage is the currently supported method, but a directory mounted as a docker volume can also be used. Alternative cloud support can be added via the [download_blob.py](download_blob.py) script. Firmware storage must be organized by: `vendor/rsu-model/firmware-version/install_package`.

Firmware upgrades have unique procedures based on RSU vendor/manufacturer. To avoid requiring a unique bash script for every single firmware upgrade, the firmware manager upgrade runner has been written to use vendor based upgrade scripts that have been thoroughly tested. An interface-like abstract class, [base_upgrader.py](base_upgrader.py), has been made for helping create upgrade scripts for vendors not yet supported. The firmware manager upgrade runner selects the script to use based off the RSU's "model" column in the "rsus" table. These scripts report back to the firmware manager upgrade scheduler on completion with a status of whether the upgrade was a success or failure. Regardless, the Firmware Manager will remove the process from its tracking and update the PostgreSQL database accordingly.

List of currently supported vendors:

- Commsignia
- Yunex

Available Firmware Manager Upgrade Scheduler REST endpoints:

- /init_firmware_upgrade [ **POST** ] `{ "rsu_ip": "" }`
  - `rsu_ip` is the target RSU being upgraded (The target firmware is separately updated in PostgreSQL, this is just to get the Firmware Manager to immediately go look)
- /firmware_upgrade_completed [ **POST** ] `{ "rsu_ip": "", "status": "" }`
  - `rsu_ip` is the target RSU being upgraded
  - Allowed `status` values are `"success"` or `"fail"`
- /list_active_upgrades [ **GET** ]
  - Used to list all active upgrades in the form:
    `{"active_upgrades": {"1.1.1.1": {"manufacturer": "Commsignia", "model": "ITS-RS4-M", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", "install_package": "blob.blob"}}}`

Available Firmware Manager Upgrade Runner REST endpoints:

- /run_firmware_upgrade [ **POST** ] `{ "ipv4_address": "", "manufacturer": "", "model": "", "ssh_username": "", "ssh_password": "","target_firmware_id": "",  "target_firmware_version": "", "install_package": ""}`

## Requirements <a name = "requirements"></a>

To properly run the firmware_manager microservice the following services are also required:

- Blob storage (cloud-based or otherwise)
  - Firmware storage must be organized by: `vendor/rsu-model/firmware-version/install_package`.
- CV Manager PostgreSQL database with data in the "rsus", "rsu_models", "manufacturers", "firmware_images", and "firmware_upgrade_rules" tables
- Network connectivity from the environment the firmware_manager is deployed into to the blob storage and the RSUs

The firmware_manager microservice expects the following environment variables to be set:

- ACTIVE_UPGRADE_LIMIT - The number of concurrent upgrades that are allowed to be running at any given moment. Any upgrades requested beyond this limit will wait on the upgrade queue.
- BLOB_STORAGE_PROVIDER - Host for the blob storage. Default is GCP.
- BLOB_STORAGE_BUCKET - Cloud blob storage bucket for firmware storage.
- PG_DB_USER - PostgreSQL access username.
- PG_DB_PASS - PostgreSQL access password.
- PG_DB_NAME - PostgreSQL database name.
- PG_DB_HOST - PostgreSQL hostname, make sure to include port number.
- LOGGING_LEVEL (optional, defaults to 'info')

The Firmware Manager is capable of sending an email to the support team in the event that an online RSU experiences a firmware upgrade failure.
This functionality relies on the user_email_notification PostgreSQL table to pull in the list of users that are subscribed to receive these emails.
To do so the following environment variables must be set:

- SMTP_EMAIL - Email to send from.
- SMTP_USERNAME - SMTP username for SMTP_EMAIL.
- SMTP_PASSWORD - SMTP password for SMTP_EMAIL.
- SMTP_SERVER_IP - Address of the SMTP server.

GCP Required environment variables:

- GCP_PROJECT - GCP project for the firmware cloud storage bucket
- GOOGLE_APPLICATION_CREDENTIALS - Service account location. Recommended to attach as a volume.

Docker volume required environment variables:

- HOST_BLOB_STORAGE_DIRECTORY - Directory mounted as a docker volume for firmware storage. A relative path can be specified here.

## Vendor Specific Requirements

### Commsignia

Each upgrade requires just one firmware file. Upload target firmware to a cloud storage bucket or alternative hosting service according to the `vendor/rsu-model/firmware-version/install_package` directory path format.

The Firmware Manager is also able to run a bash script on Commsignia RSUs after the firmware update has been completed. If uploading a script to a cloud storage bucket or alternative hosting service do so at the directory path `vendor/rsu-model/firmware-version/post_upgrade.sh`. Additionally, the post_upgrade.sh script will need to output "ALL OK" to stdout to notify the Firmware Manager that it has completed successfully.

### Yunex

Each upgrade requires 4 total files tarred up into a single TAR file:

- Core upgrade file - Provided by Yunex
- SDK upgrade file - Provided by Yunex
- Application provisioning file - Provided by Yunex
- upgrade_info.json - Custom JSON file defining the upgrade files' names

The content of `upgrade_info.json` is created by the implementer in the following format:

```
{
  "core": "core-upgrade-file",
  "sdk": "sdk-upgrade-file",
  "provision": "provision-upgrade-file"
}
```

Upload target firmware TAR to a cloud storage bucket or alternative hosting service according to the `vendor/rsu-model/firmware-version/install_package` directory path format, where `install_package` is the TAR file.
