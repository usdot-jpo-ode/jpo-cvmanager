# RSU Ping Fetch

## Table of Contents

- [About](#about)
- [Usage](#usage)
## About <a name = "about"></a>

This directory describes an image that runs within the RSU Manager GKE Cluster. It is used to populate the PostGreSQL database with data from the Zabbix instance monitoring RSU status. It expects the following environment variables to be set:
- ZABBIX_ENDPOINT
- ZABBIX_USER
- ZABBIX_PASSWORD
- DB_USER
- DB_PASS
- DB_NAME
- DB_HOST
- LOGGING_LEVEL (optional, defaults to 'info')
