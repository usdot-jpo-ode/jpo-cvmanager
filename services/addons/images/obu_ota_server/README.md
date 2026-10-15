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
  - Firmware file names must follow `<variant>-<rw|ro>[-secureboot]-<release>.tar.sig` and be at most 128 characters, using only letters, digits, `.`, `_`, and `-`:
    - `variant` is `ob4-generic` (OBU) or `rs4-generic` (RSU), optionally followed by `-<suffix>`.
    - `rw` or `ro` sets `writableSystemPartition`, and `-secureboot` sets `secure`.
    - `release` must start with a letter or digit. When a name contains more than one `rw`/`ro` marker, the last one that leaves a valid release is used.
    - For example, `ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig`.
  - All matching firmware files are returned in a single page. The page `size` is 32, or the number of firmware files when there are more than 32.
  - Will either query GCS or the specified local path for a latest list of firmware files. Generates a manifest that includes details about each firmware file along with a `href` field that the OBU uses to generate the download request. Here is a sample response:
    `{"content":[{"id":"","name":"ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig","variant":"ob4-generic","releaseVersion":"y20.48.2-b228647","type":"OBU","writableSystemPartition":false,"secure":true,"uploadedAt":"2024-05-29T22:12:33Z","size":135055616,"links":[{"rel":"local-file","href":"localhost/firmwares/commsignia/ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig","type":"application/octet-stream"}]}],"pageable":{"sort":{"empty":false,"unsorted":false,"sorted":true},"offset":0,"pageNumber":0,"pageSize":32,"paged":true,"unpaged":false},"last":true,"totalPages":1,"totalElements":1,"size":32,"number":0,"sort":{"empty":false,"unsorted":false,"sorted":true},"numberOfElements":1,"first":true,"empty":false}`
- /firmwares/commsignia/{firmware_id} [ **GET** ]
  - Secured with basic authentication.
  - Used as an endpoint for the OBU to download a firmware file from.
  - `firmware_id` must match the firmware naming filter above; any other value returns `404` without accessing storage.
  - Supports a single `Range: bytes=<start>-[<end>]` header for partial downloads. The end offset is inclusive, partial responses return `206 Partial Content` with a `Content-Range` header, and a start offset beyond the end of the file returns `416 Range Not Satisfiable`. Requests without a range header return the full file with `200 OK`.
    - NOTE: Commsignia currently doesn't send these range headers.
  - Every download attempt is recorded in the `obu_ota_requests` PostgreSQL table using the OBU's `serialnum` and `version` query parameters. Only the newest `MAX_COUNT` successful requests are kept per serial number. The manufacturer is looked up by name, so a `Commsignia` row must exist in the `manufacturers` table for requests to be logged. Recorded requests can be viewed through the Intersection API `GET /admin/obu-ota-requests` endpoint.

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

<b>PG_DB_HOST:</b> PostgreSQL hostname, optionally followed by `:port`. Defaults to port 5432 when no port is provided.

<b>FORWARDED_ALLOW_IPS:</b> Comma-separated proxy addresses trusted to provide the `X-Forwarded-For` header, so the OBU address is logged instead of the proxy address. Docker Compose sets this from `OBU_OTA_FORWARDED_ALLOW_IPS` and defaults to `*`. With `*`, the server records the first address in the header, so the proxy in front of it must not pass a client-supplied value through:

- The bundled NGINX configurations (`resources/ota/nginx`) replace the header with `$remote_addr`, the address NGINX received the connection from, and Compose publishes the backend port (8085) on localhost only. Remote clients must connect through NGINX on port 80 or 443.
- If another proxy or load balancer sits in front of NGINX, `$remote_addr` is that proxy's address. Configure the NGINX `realip` module with the upstream proxy's addresses (`set_real_ip_from <proxy address or CIDR>;`, `real_ip_header X-Forwarded-For;`, and `real_ip_recursive on;`) so `$remote_addr` becomes the client address.
- Without NGINX, for example behind a Kubernetes ingress, never use `*`. Set `FORWARDED_ALLOW_IPS` to the ingress proxy addresses so the server uses the last address in the header that is not a trusted proxy, which a client cannot forge. When it is unset, only `127.0.0.1` is trusted and the proxy address is logged.

<b>MAX_COUNT:</b> Max number of succesfull firmware upgrades to keep in the database per device SN.

### GCP required variables <a name = "gcp-requirements"></a>

<b>BLOB_STORAGE_BUCKET:</b> Cloud blob storage bucket for firmware storage.

<b>BLOB_STORAGE_PATH:</b> Path to firmware files directory within the storage bucket. Only files directly inside this folder are offered. The Intersection API stores uploaded Commsignia OBU firmware in `ota/Commsignia/`, so set this to `ota/Commsignia/` (and `BLOB_STORAGE_BUCKET` to the API's `OBJECT_STORAGE_GCP_BUCKET_NAME`) to serve firmware uploaded through the CV Manager.

<b>GCP_PROJECT:</b> GCP project for the firmware cloud storage bucket.

<b>GOOGLE_APPLICATION_CREDENTIALS:</b> Service account location. Recommended to attach as a volume. Docker Compose always sets this to the file mounted from `GOOGLE_APPLICATION_CREDENTIALS_HOST_PATH`, so that variable must point to a real service account key when `BLOB_STORAGE_PROVIDER` is `GCP`. The default mounted sample file contains no key, and metadata server or gcloud credentials are not used under Docker Compose.

### Local Required variables <a name = "local-requirements"></a>

<b>NGINX_ENCRYPTION:</b> Used for the NGINX proxy configuration, set to "plain" or "ssl" depending on if you want to secure traffic with TLS.

<b>SERVER_CERT_FILE:</b> Path to the server cert file if using SSL.

<b>SERVER_KEY_FILE:</b> Path to the server key file if using SSL.
