# RSU Ping Fetch

## Table of Contents

- [About ](#about-)
- [Requirements ](#requirements-)

## About <a name = "about"></a>

This directory contains a microservice that runs within the RSU Manager GKE Cluster. The rsu_ping_fetch application populates the CV Manager PostGreSQL database's 'ping' table with the current online statuses of all RSUs recorded in the 'rsus' table. These statuses are retrieved directly from a [Zabbix server](https://www.zabbix.com/).

It is possible to insert the same type of data into the 'ping' table with a custom script or application that pings each RSU directly at set intervals. However, it is much easier to rely on a well maintained monitoring service such as Zabbix.

## Requirements <a name = "requirements"></a>

To properly run the rsu_ping_fetch microservice the following services are also required:

- CV Manager PostgreSQL database with at least one RSU inserted into the 'rsus' table
- Zabbix server with the REST API enabled
- rsu_ping_fetch must be deployed in the same environment or K8s cluster as the PostgreSQL database
- Network rules must be in place to allow proper routing between the rsu_ping_fetch microservice and the Zabbix server

The rsu_ping_fetch microservice expects the following environment variables to be set:

- ZABBIX_ENDPOINT
- ZABBIX_USER
- ZABBIX_PASSWORD
- DB_USER
- DB_PASS
- DB_NAME
- DB_HOST
- LOGGING_LEVEL (optional, defaults to 'info')
