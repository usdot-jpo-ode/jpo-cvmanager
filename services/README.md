# CV Manager Services

The CV Manager has multiple backend services that are required to allow the CV Manager to operate at full capacity.

## Python Version
These services are implemented using Python 3.12.2. It is recommended to use this version when developing, testing, and deploying the services.

## CV Manager API

The CV Manager API is the backend service for the CV Manager webapp. This API is required to be run in an accessible location for the web application to function. The API is a Python Flask REST service.

To learn more of what the CV Manager API offers, refer to its [README](api/README.md).

## CV Manager Add-Ons

The CV Manager add-ons are services that are very useful in allowing a user to collect and create all of the required data to be inserted into the CV Manager PostgreSQL database to allow the CV Manager to function. None of these services are required to be run. Alternative data sources for the following services can be used. However, all of these services are Kubernetes ready and are easy to integrate.

### bsm_query

The bsm_query service allows for BSM data to be geospatially queryable in a MongoDB collection.

Read more about the deployment process in the [bsm_query directory](addons/images/bsm_query/README.md).

### count_metric

The count_metric service allows for the creation of count metrics for received V2X data, counted by data type for each RSU in the CV Manager PostgreSQL database. The counter relies on Kafka and an existing deployment of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode/tree/master). The message counts will then be displayable on the CV Manager.

Read more about the deployment process in the [count_metric directory](addons/images/count_metric/README.md).

### firmware_manager

The firmware_manager service monitors the CV Manager PostgreSQL database for RSU's with different firmware_version values than their target_firmware_version in the 'rsus' table and performs firmware upgrades accordingly. These checks occur on an hourly basis for all RSUs but can also be executed immediately for an individual RSU utilizing the hosted API endpoints. This feature is intended to be used by the CV Manager API but can also be done manually for test purposes.

Read more about the deployment process in the [firmware_manager directory](addons/images/firmware_manager/README.md).

### iss_health_check

The iss_health_check service allows for RSU ISS SCMS certificate status information to be displayed on the CV Manager. This service has a dependency on the GCP Secret Manager but can be reworked to work with any secret manager. This service requires a service agreement with Greenhills ISS so an API key can be obtained to access a user's RSU profile.

Read more about the deployment process in the [iss_health_check directory](addons/images/iss_health_check/README.md).

### rsu_ping

The rsu_ping directory can be built as the rsu_ping_fetch or rsu_pinger service. Both versions allows for RSU online status information to be displayed on the CV Manager. The rsu_ping_fetch service requires a Zabbix API endpoint to function. The Zabbix server must be configured to monitor all of the RSUs displayed on the CV Manager to successfully receive online status information for each device. The rsu_pinger allows for obtaining RSU online status information without the need of a Zabbix server. The rsu_pinger is a very streamlined option without any other use besides gathering online status information. For a more robust collector of RSU data, a Zabbix server is recommended.

Read more about the deployment process in the [rsu_ping directory](addons/images/rsu_ping/README.md).

## Testing

The API and Add-Ons both have unit tests that must be run from the services directory. The VSCode tasks can alo be used to simply run all of the unit tests. Before running either method, make sure to first install all of the dependencies from the [requirements.txt](requirements.txt).

1. Ensure working directory is the `services` directory.
2. Install requirements.txt `pip3 install -r requirements.txt`.
3. Run `python3 -m pytest` to run all of the Python unit tests for all services.
