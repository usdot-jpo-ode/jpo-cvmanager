# RSU Ping Fetch

## Table of Contents

- [RSU Ping Fetch](#rsu-ping-fetch)
  - [Table of Contents](#table-of-contents)
  - [About ](#about-)
  - [Requirements ](#requirements-)

## About <a name = "about"></a>

This directory contains a microservice that runs within the CV Manager GKE Cluster. The rsu_ping_fetch application populates the CV Manager PostGreSQL database's 'ping' table with the current online statuses of all RSUs recorded in the 'rsus' table. These statuses are retrieved directly from a [Zabbix server](https://www.zabbix.com/).

It is possible to insert the same type of data into the 'ping' table with a custom script or application that pings each RSU directly at set intervals. However, it is much easier to rely on a well maintained monitoring service such as Zabbix.

Another feature this microservice provides is a ping data purger that will remove stale ping data from the CV Manager PostgreSQL database to allow for high performance RSU ping queries. The amount of time a message needs to be in the database to be considered stale is configurable with the STALE_PERIOD environment variable. This purger will run once every 24 hours to check for stale ping data in the database.

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
- STALE_PERIOD
- LOGGING_LEVEL (optional, defaults to 'info')
