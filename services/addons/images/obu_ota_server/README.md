# OBU OTA Server

## Table of Contents

- [OBU OTA Server](#obu-ota-server)
  - [Table of Contents](#table-of-contents)
  - [About ](#about-)
  - [Requirements ](#requirements-)
    - [Common required variables ](#common-required-variables-)
    - [GCP required variables ](#gcp-required-variables-)
    - [Local Required variables ](#local-required-variables-)

## About <a name = "about"></a>

This directory contains a microservice that runs within the CV Manager GKE Cluster. This service can take either local or GCP stored firmware files and serves them for OBU devices to receive Over the Air (OTA) updates. OBU devices will query the OTA server for a manifest of available firmware files and will send another request for the full firmware file if it has a newer version number. For local deployments the OBU OTA server is deployed behind a NGINX proxy to allow for TLS encryption. In k8's an ingress resource is used to handle certificates and TLS.

List of currently supported vendors:

- Commsignia

Available REST endpoints:

- / [ **GET** ]
  - Used as a health check path for K8 deployment. This path is not secured with basic authentication
- /firmwares/commsignia [ **GET** ]
  - Secured with basic authentication.
  - Firmware file name must comply with the following regex naming filter:
    `(?P<variant>(?P<type>ob4|rs4)-generic(?:-.*)?)-(?:(?P<writable>rw)|ro)(?P<secure>-secureboot)?-(?P<release>.*).tar.sig`
  - Will either query GCS or the specified local path for a latest list of firmware files. Generates a manifest that includes details about each firmware file along with a `href` field that the OBU uses to generate the download request. Here is a sample response:
    `{"content":[{"id":"","name":"ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig","variant":"ob4-generic","releaseVersion":"y20.48.2-b228647","type":"OBU","writableSystemPartition":false,"secure":true,"uploadedAt":"2024-05-29T22:12:33Z","size":135055616,"links":[{"rel":"local-file","href":"localhost/firmwares/commsignia/ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig","type":"application/octet-stream"}]}],"pageable":{"sort":{"empty":false,"unsorted":false,"sorted":true},"offset":0,"pageNumber":0,"pageSize":32,"paged":true,"unpaged":false},"last":true,"totalPages":1,"totalElements":1,"size":32,"number":0,"sort":{"empty":false,"unsorted":false,"sorted":true},"numberOfElements":1,"first":true,"empty":false}`
- /firmwares/commsignia/{firmware_id} [ **GET** ]
  - Secured with basic authentication.
  - Used as an endpoint for the OBU to download a firmware file from.
  - Supports a range header to allow for partial downloads of files.
    - NOTE: Commsignia currently doesn't send these range headers so as of now this functionality has not be tested.

## Requirements <a name = "requirements"></a>

The following environmental variables must be set:

### Common required variables <a name = "common-requirements"></a>

<b>LOGGING_LEVEL:</b> The logging level of the deployment. Options are: 'critical', 'error', 'warning', 'info' and 'debug'. If not specified, will default to 'info'. Refer to Python's documentation for more info: [Python logging](https://docs.python.org/3/howto/logging.html).

<b>SERVER_HOST:</b> The base URL of the OTA server, this must be resolvable from the OBU. Generally this should be set to the IP address of the server or the DNS name.

<b>BLOB_STORAGE_PROVIDER:</b> Set to either "DOCKER" or "GCP" depending on deployment environment.

<b>OTA_USERNAME:</b> Username to be used with basic authentication.

<b>OTA_PASSWORD:</b> Password to be used with basic authentication

<b>PG_DB_USER:</b> PostgreSQL access username.

<b>PG_DB_PASS:</b> PostgreSQL access password.

<b>PG_DB_NAME:</b> PostgreSQL database name.

<b>PG_DB_HOST:</b> PostgreSQL hostname, make sure to include port number.

<b>MAX_COUNT:</b> Max number of succesfull firmware upgrades to keep in the database per device SN.

### GCP required variables <a name = "gcp-requirements"></a>

<b>BLOB_STORAGE_BUCKET:</b> Cloud blob storage bucket for firmware storage.

<b>BLOB_STORAGE_PATH:</b> Path to firmware files directory within the storage bucket.

<b>GCP_PROJECT:</b> GCP project for the firmware cloud storage bucket.

<b>GOOGLE_APPLICATION_CREDENTIALS:</b> Service account location. Recommended to attach as a volume.

### Local Required variables <a name = "local-requirements"></a>

<b>NGINX_ENCRYPTION:</b> Used for the NGINX proxy configuration, set to "plain" or "ssl" depending on if you want to secure traffic with TLS.

<b>SERVER_CERT_FILE:</b> Path to the server cert file if using SSL.

<b>SERVER_KEY_FILE:</b> Path to the server key file if using SSL.
