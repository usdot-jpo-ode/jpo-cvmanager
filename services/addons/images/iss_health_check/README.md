# Integrity Security Services (ISS) Security Credential Management System (SCMS) Health Checker

## Table of Contents

- [Integrity Security Services (ISS) Security Credential Management System (SCMS) Health Checker](#integrity-security-services-iss-security-credential-management-system-scms-health-checker)
  - [Table of Contents](#table-of-contents)
  - [About ](#about-)
  - [Requirements ](#requirements-)

## About <a name = "about"></a>

This directory contains a microservice that runs within the CV Manager GKE Cluster. The iss_health_checker application populates the CV Manager PostGreSQL database's 'scms_health' table with the current ISS SCMS statuses of all RSUs recorded in the 'rsus' table. These statuses are queried by this application from a provided ISS Green Hills SCMS API endpoint.

The application schedules the iss_health_checker script to run every 6 hours. A new SCMS API access key is generated every run of the script to ensure the access never expires. This is due to a limitation of the SCMS API not allowing permanent access keys. Access keys are stored in GCP Secret Manager to allow for versioning and encrypted storage. The application removes the previous access key from the SCMS API after runtime to reduce clutter of access keys on the API service account.

Currently only GCP is supported to run this application due to a reliance on the GCP Secret Manager. Storing the access keys on a local volume is not recommended due to security vulnerabilities. Feel free to contribute to this application for secret manager equivalent support for other cloud environments.

## Requirements <a name = "requirements"></a>

To properly run the iss_health_checker microservice the following services are also required:

- GCP project and service account with GCP Secret Manager access
- CV Manager PostgreSQL database with at least one RSU inserted into the 'rsus' table
- Service agreement with ISS Green Hills to have access to the SCMS API REST service endpoint
- iss_health_checker must be deployed in the same environment or K8s cluster as the PostgreSQL database
- iss_health_checker deployment must have access to the internet or at least the SCMS API endpoint

The iss_health_checker microservice expects the following environment variables to be set:

- GOOGLE_APPLICATION_CREDENTIALS - file location for GCP JSON service account key.
- PROJECT_ID - GCP project ID.
- ISS_API_KEY - Initial ISS SCMS API access key to perform the first run of the script. This access key must not expire before the first runtime.
- ISS_API_KEY_NAME - Human readable reference for the access key within ISS SCMS API. Generated access keys will utilize this same name.
- ISS_PROJECT_ID - Project ID the RSUs are under that the SCMS API will be queried for.
- ISS_SCMS_TOKEN_REST_ENDPOINT - Token generation HTTPS endpoint for the ISS Green Hills SCMS API. (https://scms-api-domain/api/v3/token)
- ISS_SCMS_VEHICLE_REST_ENDPOINT - Vehicle/RSU HTTPS endpoint for the ISS Green Hills SCMS API. (https://scms-api-domain/api/v3/devices)
- PG_DB_USER - PostgreSQL access username.
- PG_DB_PASS - PostgreSQL access password.
- PG_DB_NAME - PostgreSQL database name.
- PG_DB_HOST - PostgreSQL hostname, make sure to include port number.
- LOGGING_LEVEL (optional, defaults to 'info')
