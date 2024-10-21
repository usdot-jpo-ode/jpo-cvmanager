# jpo-cvmanager

**US Department of Transportation (USDOT) Intelligent Transportation Systems (ITS) Joint Program Office (JPO) Connected Vehicle Manager**

The JPO Connected Vehicle Manager is a web-based application that helps an organization manage their deployed CV devices (Roadside Units and Onboard Units) through an interactive, graphical user interface using Mapbox.

<b>GUI:</b> ReactJS with Redux Toolkit and Mapbox GL

<b>API:</b> Python 3.12.2

<b>Features:</b>

- Visualize devices on a Mapbox map
- Display the current statuses of devices
  - Latest online status
  - ISS SCMS certificate expiration
  - Other identifying values tracked on a PostgreSQL database
- jpo-ode supported message counts, sorted by RSU IP (BSM, MAP, SPaT, SRM, SSM, TIM)
- Visualize an RSU's currently active MAP message
- Visualize Basic Safety Messages (BSMs) relative to a specified geofence and time period
- Device configuration over SNMP (v3) for message forwarding
- Device firmware upgrade support for Kapsch, Commsignia and Yunex devices
- Admin controls for adding, modifying and removing devices and users

To provide feedback, we recommend that you create an "issue" in this repository (<https://github.com/usdot-jpo-ode/jpo-cvmanager/issues>). You will need a GitHub account to create an issue. If you don’t have an account, a dialog will be presented to you to create one at no cost.

## Release Notes

The current version and release history of the JPO CV Manager: [Release Notes](docs/Release_notes.md)

## Requirements and Limitations

The JPO CV Manager was originally developed for the Google Cloud Platform and a few of its GCP dependencies still remain. The GCP dependencies will eventually be streamlined to support other options. However, there are a handful of technologies to know before attempting to utilize the CV Manager.

### CV Manager Webapp

- Supports OAuth2.0 through Keycloak for user authentication only. It can be configured for several different Identity Providers, including Google.

### CV Manager API

- PostgreSQL database is required. Run the [table creation script to create a to-spec database](resources/sql_scripts).
  - Follow along with the README to ensure your data is properly populated before running the CV Manager.
- GCP BigQuery is required to support J2735 message counts and BSM data. Message counts will be migrated to PostgreSQL eventually, however it is not recommended to store full J2735 messages in a PostgreSQL database. A noSQL database or a database that is specialized for storing big data is recommended. Support for MongoDB is planned to be implemented.
  - It is recommended to create a table for storing J2735 messages, one table per message type (BSM, MAP, SPaT, SRM, and SSM), before running the CV Manager.

### Keycloak

- Keycloak is used for the CV Manager webapp's authentication.
- The Keycloak pod requires a `realm.json` file in the folder: `./resources/keycloak/` to startup with the proper configurations. It also requires a login theme that can be modified and generated using the [keycloakify](https://github.com/keycloakify/keycloakify) forked repository in resources/keycloak/keycloakify. The theme will be automatically generated when using the docker image provided but can also be built using instructions found in the keycloakify folder.

### Intersection Data + ConflictMonitor Integration

The CVManager now has the ability to manage, configure, and display data from connected intersections. Using the JPO-ODE ConflictMonitor and other JPO-ODE resources, intersection-specific data can be collected, processed, and analyzed. The CVManager has the ability to display the results of this analysis, show live message data, and configure intersection monitoring. This includes the following:

- Displaying live MAPs, SPATs, and BSMs on a Mapbox map
- Displaying archived MAPs, SPATs, and BSMs on a Mapbox map
- Querying, downloading, and displaying events created by the ConflictMonitor
- Querying, downloading, and displaying assessments of events created by the ConflictMonitor
- Querying, managing, and displaying notifications created by the ConflictMonitor
- Updating and managing configuration parameters controlling message analysis, assessments, and notifications

More information on the ConflictMonitor and other services described above can be found here:

- [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor)
- [jpo-geojsonconverter](https://github.com/usdot-jpo-ode/jpo-geojsonconverter)
- [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode)
- [jpo-conflictvisualizer](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer)

**Ongoing Efforts**
This feature is under active development. This is a joint effort involving combining the features of the existing CIMMS conflictvisualizer tools with the CVManager components, to enable connected vehicle and intersection analysis in one application.

#### Local Development

Ease of local development has been a major consideration in the integration of intersection data into the CVManager application. Through the use of public docker images and sample datasets, this process is relatively simple. The services required to show intersection data on the CVManager webapp are:

- [intersection (api)](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer/tree/cvmgr-cimms-integration/api)
  - Modified jpo-conflictvisualizer api which is able to utilize the cvmanager keycloak realm
- kafka
  - Base kafka image used to supply required topics to the conflictvisualizer api
- kafka_init
  - Kafka topic creation image, to create required topics for the conflictvisualizer api
- MongoDB
  - Base MongoDB image, with sample data, used to supply data to the conflictvisualizer api

**ConflictVisualizer API Submodules**
The ConflictVisualizer API uses submodules to reference the ConflictMonitor, ODE, and other services. These submodules need to be initialized and updated before the API can be built and run locally. Run the following command to initialize the submodules:

```sh
git submodule update --init --recursive
```

If you get an error about filenames being too long for Git, run this command in an admin shell to enable long git file paths:

```sh
git config --system core.longpaths true
```

**Running a Simple Local Environment**

1. Update your .env from the sample.env, all intersection-specific service variables are at the bottom.
2. Build the docker-compose:

```sh
docker compose up -d
```

If any issues occur, try:

```sh
docker compose up --build -d
```

This command will create all of the CVManager containers as well a the intersection-specific containers. Now, intersection-specific data will be available through the CVManager webapp.

**Summary of all docker-compose files**

- docker-compose.yml
  - Run all base cvmanager services - cvmanager_postgres, cvmanager_keycloak, cvmanager_api, kafka, kafka_init, conflictvisualizer_api, mongodb_container
- docker-compose-addons.yml
  - Run cvmanager addons - jpo_geo_msg_query, jpo_count_metric, rsu_status_check, jpo_iss_health_check, firmware_manager
- docker-compose-cm-only.yml
  - Run intersection/conflictmonitor services only - cvmanager_postgres, cvmanager_keycloak, kafka, kafka_init, ode, geojsonconverter, conflictmonitor, conflictvisualizer_api, deduplicator, mongodb_container, connect
- docker-compose-full-cm.yml
  - Run all cvmanager and conflictmonitor services - cvmanager_api, cvmanager_webapp, cvmanager_postgres, cvmanager_keycloak, kafka, kafka_init, ode, geojsonconverter, conflictmonitor, conflictvisualizer_api, deduplicator, mongodb_container, connect
- docker-compose-mongo.yml
  - Run mongodb database for use by cvmanager - mongo, mongo-setup
- docker-compose-no-cm.yml
  - Run cvmanager components without any intersection/conflictmonitor services - cvmanager_api, cvmanager_webapp, cvmanager_postgres, cvmanager_keycloak
- docker-compose-obu-ota-server.yml
  - Run OBU-OTA server components only - jpo_ota_backend, jpo_ota_nginx
- docker-compose-webapp-deployment.yml
  - Run only the cvmanager webapp - cvmanager_webapp

**Running the CVManager without Intersection Services**

1. Update your .env from the sample_no_cm.env (It is not necessary to clear out the intersection-specific variables)
2. Build the docker-compose-no-cm:
   If you would like to run all of the ConflictMonitor services including the JPO-ODE and GeoJSONConverter, use the docker-compose-full-cm.yml:

```sh
docker compose -f docker-compose-no-cm.yml up --build -d
```

**Running all ConflictMonitor Services**

1. Update your .env from the sample.env, all intersection-specific service variables are at the bottom. No additional variables are currently required on top of the simple intersection configuration.
2. Build the combined docker-compose:

```sh
docker compose -f docker-compose-full-cm.yml up --build -d
```

**ConflictMonitor Configuration Scripts**

A set of scripts and data dumps exists in the [conflictmonitor folder](./conflictmonitor), see the readme in that location for more information.

#### ConflictVisualizer API

- The CV Manager webapp has been integrated with the ConflictVisualizer tool to allow users to view data directly from a jpo-conflictmonitor instance. This integration currently requires an additional jpo-conflictvisualizer api to be deployed alongside the jpo-cvmanager api. This allows the webapp to make authenticated requests to the jpo-conflictvisualizer api to retrieve the conflict monitor data.
- [jpo-conflictvisualizer (api)](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer/tree/cvmgr-cimms-integration/api)
- kafka
- kafka_init (to create required kafka topics)
- MongoDB (to hold message and configuration data)

The ConflictVisualizer api pulls archived message and configuration data from MongoDB, and is able to live-stream SPATs, MAPs, and BSMs from specific kafka topics

#### MongoDB

MongoDB is the backing database of the ConflictVisualizer api. This database holds configuration parameters, archived data (SPATs, MAPs, BSMs, ...), and processed data (notifications, assessments, events). For local development, a mongodump has been created in the conflictmonitor/mongo/dump_2024_08_20 directory. This includes notifications, assessments, events, as well as SPATs, MAPs, and BSMs. All of this data is available through the conflictvisualizer api.

#### Kafka

Kafka is used by the ConflictVisualizer api to receive data from the ODE, GeoJSONConverter, and ConflictMonitor. These connections enable live data to

#### Generating Sample Data

Some simple sample data is injected into the MongoDB instance when created. If more data is useful, the test-message-sender from the jpo-conflictmonitor can also be used to generate live sample data. This component should be cloned/installed separately, and is described here: [jpo-conflictmonitor/test-message-sender](https://github.com/usdot-jpo-ode/jpo-conflictmonitor/tree/develop/test-message-sender)

## Getting Started

The following steps are intended to help get a new user up and running the JPO CV Manager in their own environment.

1.  Follow the Requirements and Limitations section and make sure all requirements are met.
2.  Create a copy of the sample.env named ".env" and refer to the Environmental variables section below for more information on each variable.
    1.  Make sure at least the DOCKER_HOST_IP, KEYCLOAK_ADMIN_PASSWORD, KEYCLOAK_API_CLIENT_SECRET_KEY, and MAPBOX_TOKEN are set for this.
    2.  Some of these variables, delineated by sections, pertain to the [jpo-conflictvisualizer (api)](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer/tree/cvmgr-cimms-integration/api), [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor), [jpo-geojsonconverter](https://github.com/usdot-jpo-ode/jpo-geojsonconverter), [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode). Please see the documentation provided for these projects when setting these variables.
3.  The CV Manager has four components that need to be containerized and deployed: the API, the PostgreSQL database, Keycloak, and the webapp.

    - If you are looking to deploy the CV Manager locally, you can simply run the docker-compose, make sure to fill out the .env file to ensure it launches properly. Also, edit your host file ([How to edit the host file](<[resources/kubernetes](https://docs.rackspace.com/support/how-to/modify-your-hosts-file/)>)) and add IP address of your docker host to these custom domains (remove the carrot brackets and just put the IP address):

    CV Manager hosts:

         <DOCKER_HOST_IP> cvmanager.local.com
         <DOCKER_HOST_IP> cvmanager.auth.com

4.  Apply the docker compose to start the required components:

    ```sh
    docker compose up -d
    ```

    If any issues occur, try:

    ```sh
    docker compose up --build -d
    ```

5.  Access the website by going to:

    ```
      http://cvmanager.local.com
      Default Username: test@gmail.com
      Default Password: tester
    ```

6.  To access keycloak go to:

    ```
      http://cvmanager.auth.com:8084/
      Default Username: admin
      Default Password: admin
    ```

- If you are looking to deploy in Kubernetes or on separate VMs, refer to the Kubernetes YAML deployment files to deploy the four components to your cluster. ([Kubernetes YAML](resources/kubernetes))

### Debugging

Note that it is recommended to work with the Python API from a [virtual environment](https://docs.python.org/3/library/venv.html).

#### Setting up a virtual environment from the command line

1. Verify that you have Python 3.12.2 installed on your machine by running the following command:
   ```bash
   python3.12 --version
   ```
   ```cmd
   python --version
   ```
   If you have a different version installed, download and install Python 3.12.2 from the [Python website](https://www.python.org/downloads/).
2. Open a terminal and navigate to the root of the project.
3. Run the following command to create a virtual environment in the project root:
   ```bash
   python3.12 -m venv .venv
   ```
   ```cmd
   python -m venv .venv
   ```
4. Activate the virtual environment:
   ```bash
   source .venv/bin/activate
   ```
   ```cmd
   .venv\Scripts\activate
   ```
5. Install the required packages:
   ```bash
   pip3.12 install -r services/requirements.txt
   ```
   ```cmd
   pip install -r services/requirements.txt
   ```

#### Setting up a virtual environment with VSCode

See [Visual Studio Code](https://code.visualstudio.com/docs/python/environments) documentation for information on how to set up a virtual environment with VS Code.

#### Debugging Profile

A debugging profile has been set up for use with VSCode to allow ease of debugging with this application. To use this profile, simply open the project in VSCode and select the "Debug" tab on the left side of the screen. Then, select the "Debug Solution" profile and click the green play button. This will spin up a postgresql instance as well as the keycloak auth solution within docker containers. Once running, this will also start the debugger and attach it to the running API container. You can then set breakpoints and step through the code as needed.

For the "Debug Solution" to run properly on Windows 10/11 using WSL, the following must be configured:

1.  In a Powershell or Command Prompt terminal run the command: `ifconfig` and open up your `C:\Windows\System32\drivers\etc\hosts` file

    - Copy the `Ethernet adapter vEthernet (WSL) -> IPv4 Address` value to your hosts `cvmanager.auth.com` entry.
    - In the same hosts file, update the `cvmanager.local.com` value to: `127.0.0.1`.

2.  Update your main .env file variables as specified in the root of the cvmanager directory

    - Copy the `Ethernet adapter vEthernet (Default) -> IPv4 Address` value to your hosts `WEBAPP_HOST_IP` variable

3.  Apply the docker compose to start the required components:

```sh
docker compose up -d
```

To run only the critical cvmanager components (no conflictmonitor/conflictvisualizer), use this command:

```sh
docker compose up -d cvmanager_api cvmanager_webapp cvmanager_postgres cvmanager_keycloak
```

4.  Access the website by going to:

    ```
      http://cvmanager.local.com
      Default Username: test@gmail.com
      Default Password: tester
    ```

5.  To access keycloak go to:

    ```
      http://cvmanager.auth.com:8084/
      Default Username: admin
      Default Password: admin
    ```

### Environment Variables

<b>Generic Variables</b>

- DOCKER_HOST_IP: Set with the IP address of the eth0 port in your WSL instance. This can be found by installing networking tools in wsl and running the command `ifconfig`
- WEBAPP_HOST_IP: Defaults to DOCKER_HOST_IP value. Only change this if the webapp is being hosted on a separate endpoint.
- KC_HOST_IP: Defaults to DOCKER_HOST_IP value. Only change this if the webapp is being hosted on a separate endpoint.

<b>Webapp Variables</b>

- MAPBOX_TOKEN: A token from Mapbox used to render the map in the Webapp. The free version of Mapbox works great in most cases.
- WEBAPP_DOMAIN: The domain that the webapp will run on. This is required for Keycloak CORS authentication.
- API_URI: The endpoint for the CV manager API, must be on a Keycloak Authorized domain.
- COUNT_MESSAGE_TYPES: List of CV message types to query for counts.
- VIEWER_MSG_TYPES: List of CV message types to query geospatially.
- DOT_NAME: The name of the DOT using the CV Manager.
- MAPBOX_INIT_LATITUDE: Initial latitude value to use for MapBox view state.
- MAPBOX_INIT_LONGITUDE: Initial longitude value to use for MapBox view state.
- MAPBOX_INIT_ZOOM: Initial zoom value to use for MapBox view state.

<b>API Variables</b>

- COUNTS_MSG_TYPES: Set to a list of message types to include in counts query. Sample format is described in the sample.env.
- GEO_DB_NAME: The database name for geospatial message visualization data. This is currently only supported for BSM and PSM message types.
- SSM_DB_NAME: The database name for SSM visualization data.
- SRM_DB_NAME: The database name for SRM visualization data.
- FIRMWARE_MANAGER_ENDPOINT: Endpoint for the firmware manager deployment's API.
- CSM_EMAIL_TO_SEND_FROM: Origin email address for the API error developer emails.
- CSM_EMAILS_TO_SEND_TO: Destination email addresses for the API error developer emails.
- CSM_EMAIL_APP_USERNAME: Username for the SMTP server.
- CSM_EMAIL_APP_PASSWORD: Password for the SMTP server.
- CSM_TARGET_SMTP_SERVER_ADDRESS: Destination SMTP server address.
- CSM_TARGET_SMTP_SERVER_PORT: Destination SMTP server port.
- API_LOGGING_LEVEL: The level of which the CV Manager API will log. (DEBUG, INFO, WARNING, ERROR)
- CSM_TLS_ENABLED: Set to "true" if the SMTP server requires TLS.
- CSM_AUTH_ENABLED: Set to "true" if the SMTP server requires authentication.
- WZDX_ENDPOINT: WZDX datafeed endpoint.
- WZDX_API_KEY: API key for the WZDX datafeed.
- TIMEZONE: Timezone to be used for the API.
- GOOGLE_APPLICATION_CREDENTIALS: Path to the GCP service account credentials file. Attached as a volume to the CV manager API service.

<b>PostgreSQL Variables</b>

- PG_DB_HOST: The database host, must include the port (normally hostname:5432). Defaults to DOCKER_HOST_IP:5432 but can be configured to a separate endpoint.
- PG_DB_USER: The database user that will be used to authenticate the cloud function when it queries the database.
- PG_DB_PASS: The database user's password that will be used to authenticate the cloud function.
- INSTANCE_CONNECTION_NAME: The connection name for the Cloud SQL instance. (project-id:region:name)

<b>MongoDB Variables</b>

#### For Windows Users Only

If running on Windows, please make sure that your global git config is set up to not convert end-of-line characters during checkout.

Disable `git core.autocrlf` (One Time Only)

```bash
git config --global core.autocrlf false
```

- MONGO_DB_URI: URI for the MongoDB connections.
- MONGO_DB_NAME: Database name for RSU counts.
- MONGO_ADMIN_DB_USER: Admin Username for MongoDB
- MONGO_ADMIN_DB_PASS: Admin Password for MongoDB
- MONGO_CV_MANAGER_DB_USER: CV Manager Username for MongoDB
- MONGO_CV_MANAGER_DB_PASS: CV Manager Password for MongoDB

- MONGO_IP: IP Address of the MongoDB (Defaults to $DOCKER_HOST_IP)
- MONGO_DB_USER: Username of the account used to connect to MongoDB
- MONGO_DB_PASS: Password of the account used to connect to MongoDB
- MONGO_PORT: Port number of MongoDB (default is 27017)
- MONGO_COLLECTION_TTL: Number of days documents will be kept in a MongoDB collection

- INSERT_SAMPLE_DATA: If true, sample data will be inserted in the CVCounts, V2XGeoJson, and OdeSsmJson collections

<b>Keycloak Variables</b>

- KEYCLOAK_DOMAIN: Domain name that Keycloak will be served on.
- KEYCLOAK_ADMIN: Admin username for Keycloak configuration.
- KEYCLOAK_ADMIN_PASSWORD: Admin password for Keycloak configuration.
- KEYCLOAK_ENDPOINT: Keycloak base URL to send requests to. Reference the sample.env for the URL formatting.
- KEYCLOAK_REALM: Keycloak Realm name.
- KEYCLOAK_API_CLIENT_ID: Keycloak API client name.
- KEYCLOAK_API_CLIENT_SECRET_KEY: Keycloak API secret for the given client name.
- KEYCLOAK_LOGIN_THEME_NAME: Name of the jar file to use as the theme provider in Keycloak. For generating a custom theme reference the [Keycloakify](https://github.com/CDOT-CV/keycloakify-starter) Github
- KC_LOGGING_LEVEL: The level of which the Keycloak instance will log. (ALL, DEBUG, ERROR, FATAL, INFO, OFF, TRACE, and WARN)
- GOOGLE_CLIENT_ID: GCP OAuth2.0 client ID for SSO Authentication within keycloak.
- GOOGLE_CLIENT_SECRET: GCP OAuth2.0 client secret for SSO Authentication within keycloak.

Environment variables from addon services can also be set in the main `.env` file. These variables are defined in their own `README` files in the `services/addons/images` location of this repository.

## License Information

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
file except in compliance with the License.
You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. See the License for the specific language governing
permissions and limitations under the [License](http://www.apache.org/licenses/LICENSE-2.0).
