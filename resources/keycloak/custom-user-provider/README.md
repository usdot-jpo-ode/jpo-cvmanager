# Custom Postgres Keycloak User provider

This is a java project which builds a custom user provider to be integrated into keycloak. This utilizes environment variables set in the realm.json to authenticate to the postgres database, and allows keycloak to query the postgres database to retrieve user information.

## Running the project

This is a maven Spring-boot java project. This project generated a .jar file in ./target/keycloak-custom-providers-0.1.0-SNAPSHOT.jar. Please see the commands below for installing the project and generating the .jar file:

### Installation

```sh
mvn clean install
```

### Regenerating the .jar file

The .jar file generated in the target folder is used by the Dockerfile to build the custom user provider into the keycloak image.

```sh
mvn package
```

## User Data

This custom user provider primarily uses the public.users postgres table, while joining this data with the public.user_organization, public.organizations, and public.roles tables to incorporate organization and role data.

Data stored in public.users table:

| Field                | Type      | Description                                         |
| -------------------- | --------- | --------------------------------------------------- |
| user_id              | UUID      | Unique user identifier, set by custom user provider |
| email                | String?   | Email of user, also used as username                |
| first_name           | String?   | First name                                          |
| last_name            | String?   | Last name                                           |
| created_timestamp    | Long      | Time of user creation, in milliseconds since epoch  |
| super_user           | int (0-1) | 1 if user has super-user admin privileges           |
| receive_error_emails | int (0-1) | 1 if user chooses to receive CvManager error emails |

Data pulled into custom user config joined from public.user_organization, public.organizations, and public.roles:

| Field         | Type   | Description                                             |
| ------------- | ------ | ------------------------------------------------------- |
| organizations | List   | List of all organization-specific roles granted by user |
| - org         | String | Organization name                                       |
| - role        | String | Role within organization                                |

## User Federation

This custom user provider is configured as a user federation provider in Keycloak, for the cvmanager realm. This is configured through the realm.json, like so:

```json
"org.keycloak.storage.UserStorageProvider": [
    {
        "id": "a6d0ac0a-279e-40f6-a226-81cc7d5be501",
        "name": "postgres-user-provider",
        "providerId": "custom-user-provider",
        "subComponents": {},
        "config": {
        "JDBC_URL": ["${KC_DB_URL_PROVIDER}"],
        "VALIDATION_QUERY": ["select 1"],
        "cachePolicy": ["NO_CACHE"],
        "DB_USERNAME": ["${KC_DB_USERNAME}"],
        "DB_PASSWORD": ["${KC_DB_PASSWORD}"],
        "enabled": ["true"],
        "JDBC_DRIVER": ["org.postgresql.Driver"]
        }
    }
],
```

This federated user provider handles the user information described above, but does not handle passwords, credentials, or keycloak-specific fields as "EMAIL_VERIFIED" and "REQUIRED_USER_ACTIONS". Those fields are managed by keycloak, which stores them in it's own Postgres tables.

Keycloak custom user provider walkthrough: [Using Custom User Providers with Keycloak](https://www.baeldung.com/java-keycloak-custom-user-providers)

## Token Mapping

A custom token mapper is configured to include the above user information in generated JWT tokens, and is configured on the cvmanager-gui client in keycloak. The returned JWT token looks as follows:

```json
...
"cvmanager_data": {
    "receive_error_emails": "1",
    "super_user": "1",
    "organizations": [
        {
            "org": "Test Org",
            "role": "admin"
        },
        {
            "org": "Test Org 2",
            "role": "user"
        }
    ],
    "user_created_timestamp": 1719549350683
},
...
```

Keycloak custom toke mapper walkthrough: [Custom Protocol Mapper with Keycloak](https://www.baeldung.com/keycloak-custom-protocol-mapper)
