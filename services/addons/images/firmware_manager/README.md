# Firmware Manager

## Table of Contents

- [Firmware Manager](#firmware-manager)
  - [Table of Contents](#table-of-contents)
  - [About ](#about-)
  - [Requirements ](#requirements-)

## About <a name = "about"></a>

This directory contains a microservice that runs within the CV Manager GKE Cluster. The firmware manager monitors the CV Manager PostgreSQL database to determine if there are any RSUs that are targeted for a firmware upgrade. This monitoring is a once-per-hour, scheduled occurrence. Alternatively, this micro-service hosts a REST API for directly initiating firmware upgrades - this is used by the CV Manager API. Firmware upgrades are then run in parallel and tracked until completion.

An RSU is determined to be ready for upgrade if its entry in the "rsus" table in PostgreSQL has its "target_firmware_version" set to be different than its "firmware_version". The Firmware Manager will ignore all devices with incompatible firmware upgrades set as their target firmware based on the "firmware_upgrade_rules" table. The CV Manager API will only offer CV Manager webapp users compatible options so this generally is a precaution.

Hosting firmware files is recommended to be done via the cloud. GCP cloud storage is the currently supported method. Alternatives can be added via the [download_blob.py](download_blob.py) script. Firmware storage must be organized by: `vendor/rsu-model/firmware-version/install_package`.

Firmware upgrades have unique procedures based on RSU vendor/manufacturer. To avoid requiring a unique bash script for every single firmware upgrade, the Firmware Manager has been written to use vendor based upgrade scripts that have been thoroughly tested. An interface-like abstract class, [base_upgrader.py](base_upgrader.py), has been made for helping create upgrade scripts for vendors not yet supported. The Firmware Manager selects the script to use based off the RSU's "model" column in the "rsus" table. These scripts report back to the Firmware Manager on completion with a status of whether the upgrade was a success or failure. Regardless, the Firmware Manager will remove the process from its tracking and update the PostgreSQL database accordingly.

List of currently supported vendors:

- Commsignia
- Yunex

Available REST endpoints:

- /init_firmware_upgrade [ **POST** ] `{ "rsu_ip": "" }`
  - `rsu_ip` is the target RSU being upgraded (The target firmware is separately updated in PostgreSQL, this is just to get the Firmware Manager to immediately go look)
- /firmware_upgrade_completed [ **POST** ] `{ "rsu_ip": "", "status": "" }`
  - `rsu_ip` is the target RSU being upgraded
  - Allowed `status` values are `"success"` or `"fail"`
- /list_active_upgrades [ **GET** ]
  - Used to list all active upgrades in the form:
    `{"active_upgrades": {"1.1.1.1": {"manufacturer": "Commsignia", "model": "ITS-RS4-M", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", "install_package": "blob.blob"}}}`

## Requirements <a name = "requirements"></a>

To properly run the firmware_manager microservice the following services are also required:

- Cloud based blob storage
  - Firmware storage must be organized by: `vendor/rsu-model/firmware-version/install_package`.
- CV Manager PostgreSQL database with data in the "rsus", "rsu_models", "manufacturers", "firmware_images", and "firmware_upgrade_rules" tables
- Network connectivity from the environment the firmware_manager is deployed into to the blob storage and the RSUs

The firmware_manager microservice expects the following environment variables to be set:

- BLOB_STORAGE_PROVIDER - Host for the blob storage. Default is GCP.
- BLOB_STORAGE_BUCKET - Cloud blob storage bucket for firmware storage.
- DB_USER - PostgreSQL access username.
- DB_PASS - PostgreSQL access password.
- DB_NAME - PostgreSQL database name.
- DB_HOST - PostgreSQL hostname, make sure to include port number.
- LOGGING_LEVEL (optional, defaults to 'info')

GCP Required environment variables:

- GCP_PROJECT - GCP project for the firmware cloud storage bucket
- GOOGLE_APPLICATION_CREDENTIALS - Service account location. Recommended to attach as a volume.
