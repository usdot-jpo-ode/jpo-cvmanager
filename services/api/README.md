# CV Manager API

The CV Manager API is a single application alternative to running all of the separate cloud functions as individual micro services. The Cloud Run function is triggered over HTTP just like a cloud function and performs all of the same features with optimized authentication and communication with the RSU REST API.

### Benefits:

- Reduces network hops when making calls to RSUs to 2 hops instead of 5
- Authentication and role assignment is integrated into a middleware script in the REST API
- Less technical overhead (Less duplicated code across functions and less deployments)

## Middleware

Before the Cloud Run CV Manager will allow an endpoint to be hit, the middleware function will run to first authorize the user credentials. This makes it so that the user must always be authorized to be able to run the cloud run endpoints with no exceptions. (Besides HTTP OPTIONS methods for CORS support)

The middleware makes the following assumptions:

- Users are unique based on their email
- Users are only assigned a single role

## Supported Endpoints

Expected headers for all endpoints:

- `"Content-Type": "application/json"`
- `"Authorization": "tokenId"`

### <b>/user-auth</b> <b>(GET)</b>

Returns authorized user information including full name, email, and role.

Example return value:

- {"name": "John Doe", "email": "jdoe@gmail.com", "role": "admin"}

### <b>/contact-support</b> <b>(POST)</b>

Sends a support request email to all users subscribed to 'Support Requests' in the cv-manager. Please note that this functionality
relies on the user_email_notification table in PostgreSQL to pull in all users subscribed to receive these notifications.

### <b>/rsuinfo</b> <b>(GET)</b>

Returns all basic data for RSUs in the GCP Cloud SQL database. It performs a basic select all query from a table named "RsuData" that is located in a database specified by the environments variables. Returns single JSON object.

### <b>/rsu-online-status</b> <b>(GET)</b>

Returns the online status of every RSU and the last time each RSU has been documented to be online in a single JSON object.

### <b>/rsucounts</b> <b>(GET)</b>

Returns the message counts for a single, selected RSU from a BigQuery table. It performs a basic select query on a table specified by the environments variable. Returns single JSON object.

### <b>/rsu-command</b> <b>(GET, POST)</b>

1. Verifies the command and calls the corresponding function.
2. Provided RSU data is plugged into the appropriate data structure depending upon the RSU REST endpoint.
   - HTTP GET URL arguments
   - HTTP POST body data
3. Directly hit RSUs with SNMP commands or trigger the RSU REST endpoint for SSH commands.
4. Return response, varies depending upon request.

### <b>/rsu-map-info</b> <b>(GET)</b>

Returns the list of all ipv4 addresses with MAP message data in the PostgreSQL database when argument ip_list is true. Returns the MAP message geoJSON data for the RSU specified in the ip_address argument as a single JSON object when ip_list is false.

### <b>/rsu-geo-msg-data</b> <b>(POST)</b>

Returns geoJSON data for BSM / PSM messages from a MongoDB collection given start time, end time, and geofence coordinates. It performs a find query on on either the MONGO_PROCESSED_BSM_COLLECTION_NAME or MONGO_PROCESSED_PSM_COLLECTION_NAME collection depending on the requested message type. Returns an array of GeoJSON objects. In the event that the number of records exceeds the threshold specified by the MAX_GEO_QUERY_RECORDS environment variable filtering will occur so that each nth record is returned.

Example request body:

```json
{
  "pointList": [
    [-122.4194, 37.7749],
    [-122.4194, 37.7749]
  ],
  "start": "2024-01-01T00:00:00Z",
  "end": "2024-01-01T00:00:00Z",
  "msg_type": "bsm"
}
```

Example response:

```json
[
  {
    "type": "Feature",
    "geometry": { "coordinates": [-105.0, 40.0], "type": "Point" },
    "properties": {
      "schemaVersion": 1,
      "id": "test_id_001",
      "originIp": "8.8.8.8",
      "messageType": "BSM",
      "time": "2025-01-17T03:45:52Z",
      "heading": 1000.0,
      "msgCnt": 1,
      "speed": 0.0
    }
  }
]
```

## Admin Endpoints

The CV Manager supports users who are application admins (super users) to add new RSUs, users and organizations to the CV Manager. This will then effect the database so it will be viewable to all users in the chosen organizations.

## RSUs

### <b>/admin-new-rsu</b> <b>(GET)</b>

Returns the field options for specific RSU fields that do not take free-form responses.

- primary_routes (will still allow new route names)
- rsu_models
- ssh_credential_groups
- snmp_credential_groups
- snmp_version_groups
- organizations

### <b>/admin-new-rsu</b> <b>(POST)</b>

Adds a new RSU to the CV Manager database and allows for it to be viewable and configurable via the CV Manager. Currently supports Commsignia, Kapsch and Yunex. Associates the RSU with every organization specified.

body example:

```
{
  "ip": "10.0.0.1",
  "geo_position": {
    "latitude": 40.00,
    "longitude": -100.00
  },
  "milepost": 56.8,
  "primary_route": "I25",
  "serial_number": "55EE002211",
  "model": "Commsignia",
  "scms_id": "",
  "ssh_credential_group": "ssh profile",
  "snmp_credential_group": "snmp profile",
  "snmp_version_group": "snmp version",
  "organizations": ["Organization 1"]
}
```

### <b>/admin-rsu</b> <b>(GET)</b>

Depending upon the rsu_ip argument's value, this endpoint returns a list of all RSUs in the CV Manager's PostgreSQL DB or the details of a single RSU along with the options for specific RSU fields that do not take free-form responses.

HTTP URL Arguments:

- rsu_ip:
  - Set to "all" if you want a list of all RSUs regardless of organization affiliation. Will not return the RSU field options.
  - Set to a specific RSU IP such as "10.0.0.1" to return all of the RSU details of that single RSU along with the allowed RSU field options.

### <b>/admin-rsu</b> <b>(PATCH)</b>

Modifies an RSU within the CV Manager database, including RSUs that may not have been made through the /admin-new-rsu endpoint. Currently supports Commsignia, Kapsch and Yunex.

body example:

```
{
  "ip": "10.0.0.1",
  "geo_position": {
    "latitude": 40.00,
    "longitude": -100.00
  },
  "milepost": 56.8,
  "primary_route": "I25",
  "serial_number": "55EE002211",
  "model": "Commsignia",
  "scms_id": "",
  "ssh_credential_group": "ssh profile",
  "snmp_credential_group": "snmp profile",
  "snmp_version_group": "snmp version",
  "organizations_to_add": ["Organization 1"],
  "organizations_to_remove": []
}
```

### <b>/admin-rsu</b> <b>(DELETE)</b>

Deletes the specified RSU from the CV Manager PostgreSQL database based off the IP specified in the rsu_ip argument.

HTTP URL Arguments:

- rsu_ip: Delete a specific RSU specified by its IP such as "10.0.0.1" from the CV Manager's PostgreSQL database.

## Users

### <b>/admin-new-user</b> <b>(GET)</b>

Returns the field options for specific user fields that do not take free-form responses.

- organizations
- roles

### <b>/admin-new-user</b> <b>(POST)</b>

Adds a new user to the CV Manager database. Associates the user with every organization specified. The specified user will be able to login to the CV Manager as soon as this is complete. The email associated with the user MUST be a Gmail account or an email address that is an alias of a Gmail.

body example:

```
{
  "email": "jdoe@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "super_user": True,
  "organizations": [
    {"name": "Test Org", "role": "operator"}
  ]
}
```

### <b>/admin-user</b> <b>(GET)</b>

Depending upon the user_email argument's value, this endpoint returns a list of all users in the CV Manager's PostgreSQL DB or the details of a single user along with the options for specific user fields that do not take free-form responses.

HTTP URL Arguments:

- user_email:
  - Set to "all" if you want a list of all users regardless of organization affiliation. Will not return the user field options.
  - Set to a specific user email such as "user@email.com" to return all of the user details of that single user along with the allowed user field options.

### <b>/admin-user</b> <b>(PATCH)</b>

Modifies a user within the CV Manager database, including users that may not have been made through the /admin-new-user endpoint.

body example:

```
{
  "email": "jdoe@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "super_user": True,
  "organizations_to_add": [
    {"name": "Test Org3", "role": "admin"}
  ],
  "organizations_to_modify": [
    {"name": "Test Org2", "role": "user"}
  ],
  "organizations_to_remove": [
    {"name": "Test Org", "role": "user"}
  ]
}
```

### <b>/admin-user</b> <b>(DELETE)</b>

Deletes the specified user from the CV Manager PostgreSQL database based off the user email specified in the user_email argument.

HTTP URL Arguments:

- user_email: Delete a specific user specified by its email such as "user@email.com" from the CV Manager's PostgreSQL database.

## Organizations

### <b>/admin-new-org</b> <b>(POST)</b>

Adds a new organization to the CV Manager database. The new organization will be usable for new RSUs and users from that point onward. Adding existing RSUs and users to the new organization will require calls of the edit endpoints.

body example:

```
{
  "name": "Test Org"
}
```

### <b>/admin-org</b> <b>(GET)</b>

Depending upon the org_name argument's value, this endpoint returns a list of all organizations in the CV Manager's PostgreSQL DB or the details of a single organization. The list of all organizations will also include a count of the number of RSUs and users that are associated with each individual organization. Requesting a specific organization will include exactly which RSUs and users are a part of that organization.

HTTP URL Arguments:

- org_name:
  - Set to "all" if you want a list of all organizations. Will also include counts of the number of RSUs and users for each organization.
  - Set to a specific organization name such as "Org1" to return all of the organization information including affiliated RSUs and users.

### <b>/admin-org</b> <b>(PATCH)</b>

Modifies an organization within the CV Manager database, including organizations that may not have been made through the /admin-new-org endpoint.

body example:

```
{
  "name": "Test Org",
  "users_to_add": [
    {"email": "testing3@email.com", "role": "admin"}
  ],
  "users_to_modify": [
    {"email": "testing2@email.com", "role": "user"}
  ],
  "users_to_remove": [
    {"email": "testing1@email.com", "role": "user"}
  ],
  "rsus_to_add": ["10.0.0.2"],
  "rsus_to_remove": ["10.0.0.1"]
}
```

### <b>/admin-org</b> <b>(DELETE)</b>

Deletes the specified organization from the CV Manager PostgreSQL database based off the organization name specified in the org_name argument.

HTTP URL Arguments:

- org_name: Delete a specific organization specified by its name such as "Org1" from the CV Manager's PostgreSQL database.

## Deploying CV Manager Cloud Run REST API

1. Build docker image, tag it and push it to a GCP image repository (Container Registry)
   - `cd ~/RSU_Management/GCP_cloud_run/rsu_manager` (~ represents wherever you cloned the repository within your local machine)
   - `docker build .`
   - `docker image tag <image-id> <image-name>:<tag>`
   - `docker push <image-name>:<tag>`
2. Go to GCP Cloud Run and click "Create Service"
3. Configure the Cloud Run deployment container settings
   - Select container image (the one from step 1)
   - Set container port to 8080
   - CPU allocation setting is up to the user
   - 512MB is enough memory to run the application
4. Configure the Cloud Run deployment variables and secrets settings
   - The following environment variables are required to be set by environment variable or secret:

<b>Environment Variables:</b>

- CORS_DOMAIN: The CV Manager webapp domain that CORS will allow API responses to.
- INSTANCE_CONNECTION_NAME: The connection name for the Cloud SQL instance. (project-id:region:name)
- PG_DB_HOST: The database IP.
- PG_DB_PORT: The database port.
- PG_PG_DB_USER: The database user that will be used to authenticate the cloud function when it queries the database.
- PG_PG_DB_PASS: The database user's password that will be used to authenticate the cloud function.
- COUNTS_MSG_TYPES: Set to a list of message types to include in counts query. Sample format is described in the sample.env.
- MONGO_PROCESSED_BSM_COLLECTION_NAME: The database name for processed BSM messages output from the [Geojson Converter](https://github.com/usdot-jpo-ode/geojson-converter).
- MONGO_PROCESSED_PSM_COLLECTION_NAME: The database name for processed PSM messages output from the [Geojson Converter](https://github.com/usdot-jpo-ode/geojson-converter).
- SSM_DB_NAME: The database name for SSM visualization data.
- SRM_DB_NAME: The database name for SRM visualization data.
- MONGO_DB_URI: URI for the MongoDB connection.
- MONGO_DB_NAME: Database name for RSU counts.
- KEYCLOAK_ENDPOINT: Keycloak base URL to send requests to. Reference the sample.env for the URL formatting.
- KEYCLOAK_REALM: Keycloak Realm name.
- KEYCLOAK_API_CLIENT_ID: Keycloak API client name.
- KEYCLOAK_API_CLIENT_SECRET_KEY: Keycloak API secret for the given client name.
- FIRMWARE_MANAGER_ENDPOINT: Endpoint for the firmware manager deployment's API.
- LOGGING_LEVEL: The level of which the application will log. (DEBUG, INFO, WARNING, ERROR)
- CSM_EMAIL_TO_SEND_FROM: Origin email address for the API.
- CSM_EMAIL_APP_USERNAME: Username for the SMTP server.
- CSM_EMAIL_APP_PASSWORD: Password for the SMTP server.
- CSM_TARGET_SMTP_SERVER_ADDRESS: Destination SMTP server address.
- CSM_TARGET_SMTP_SERVER_PORT: Destination SMTP server port.
- WZDX_ENDPOINT: WZDX datafeed enpoint.
- WZDX_API_KEY: API key for the WZDX datafeed.
- GOOGLE_ACCESS_KEY_NAME: The required Google environment variable for authenticating with Google Cloud.
- GCP_PROJECT_ID: The Google Cloud project ID for which the service account associated with GOOGLE_ACCESS_KEY_NAME is for.
- MOOVE_AI_SEGMENT_AGG_STATS_TABLE: The BigQuery table name for Moove.Ai's segment aggregate statistics.
- MOOVE_AI_SEGMENT_EVENT_STATS_TABLE: The BigQuery table name for Moove.Ai's segment event statistics.
- TIMEZONE: Timezone to be used for the API.

1. Configure the Cloud Run deployment connections settings
   - The application assumes there is a Cloud SQL DB, select the DB under "Cloud SQL connections". Ensure the environment variables match the selected DB.
   - The application makes requests to the automated RSU REST API located in K8s. If this is in a VPC, configure the proper VPC connector. Route only requests to private IPs.
2. Configure the Cloud Run deployment security settings
   - Ensure a service account has been selected that has:
     - Cloud SQL Client permissions
     - VPC Connector permissions
     - BigQuery access permissions
3. Deploy and utilize the assigned endpoint in the CV Manager React application's environment variables
