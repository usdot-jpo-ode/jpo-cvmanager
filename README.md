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

## Getting Started

The following steps are intended to help get a new user up and running the JPO CV Manager in their own environment.

1.  Follow the Requirements and Limitations section and make sure all requirements are met.
2.  Create a copy of the sample.env named ".env" and refer to the Environmental variables section below for more information on each variable.
    1.  Make sure at least the DOCKER_HOST_IP, KEYCLOAK_ADMIN_PASSWORD, KEYCLOAK_API_CLIENT_SECRET_KEY, and MAPBOX_TOKEN are set for this.
3.  The CV Manager has four components that need to be containerized and deployed: the API, the PostgreSQL database, Keycloak, and the webapp.

    - If you are looking to deploy the CV Manager locally, you can simply run the docker-compose, make sure to fill out the .env file to ensure it launches properly. Also, edit your host file ([How to edit the host file](<[resources/kubernetes](https://docs.rackspace.com/support/how-to/modify-your-hosts-file/)>)) and add IP address of your docker host to these custom domains (remove the carrot brackets and just put the IP address):

    CV Manager hosts:

         <DOCKER_HOST_IP> cvmanager.local.com
         <DOCKER_HOST_IP> cvmanager.auth.com

4.  Apply the docker compose to start the required components:

         docker compose up -d

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

         docker compose up -d

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
- DOT_NAME: The name of the DOT using the CV Manager.
- MAPBOX_INIT_LATITUDE: Initial latitude value to use for MapBox view state.
- MAPBOX_INIT_LONGITUDE: Initial longitude value to use for MapBox view state.
- MAPBOX_INIT_ZOOM: Initial zoom value to use for MapBox view state.

<b>API Variables</b>

- COUNTS_DB_TYPE: Set to either "MongoDB" or "BigQuery" depending on where the message counts are stored.
- COUNTS_MSG_TYPES: Set to a list of message types to include in counts query. Sample format is described in the sample.env.
- COUNTS_DB_NAME: The BigQuery table or MongoDB collection name where the RSU message counts are located.
- BSM_DB_NAME: The database name for BSM visualization data.
- SSM_DB_NAME: The database name for SSM visualization data.
- SRM_DB_NAME: The database name for SRM visualization data.
- FIRMWARE_MANAGER_ENDPOINT: Endpoint for the firmware manager deployment's API.
- CSM_EMAIL_TO_SEND_FROM: Origin email address for the API.
- CSM_EMAIL_APP_USERNAME: Username for the SMTP server.
- CSM_EMAIL_APP_PASSWORD: Password for the SMTP server.
- CSM_EMAILS_TO_SEND_TO: Destination email list.
- CSM_TARGET_SMTP_SERVER_ADDRESS: Destination SMTP server address.
- CSM_TARGET_SMTP_SERVER_PORT: Destination SMTP server port.
- API_LOGGING_LEVEL: The level of which the CV Manager API will log. (DEBUG, INFO, WARNING, ERROR)
- WZDX_ENDPOINT: WZDX datafeed enpoint.
- WZDX_API_KEY: API key for the WZDX datafeed.
- TIMEZONE: Timezone to be used for the API.
- GOOGLE_APPLICATION_CREDENTIALS: Path to the GCP service account credentials file. Attached as a volume to the CV manager API service.

<b>PostgreSQL Variables</b>

- PG_DB_HOST: The database host, must include the port (normally hostname:5432). Defaults to DOCKER_HOST_IP:5432 but can be configured to a separate endpoint.
- PG_DB_USER: The database user that will be used to authenticate the cloud function when it queries the database.
- PG_DB_PASS: The database user's password that will be used to authenticate the cloud function.
- INSTANCE_CONNECTION_NAME: The connection name for the Cloud SQL instance. (project-id:region:name)

<b>MongoDB Variables</b>

- MONGO_DB_URI: URI for the MongoDB connection.
- MONGO_DB_NAME: Database name for RSU counts.

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

## License Information

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
file except in compliance with the License.
You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. See the License for the specific language governing
permissions and limitations under the [License](http://www.apache.org/licenses/LICENSE-2.0).
