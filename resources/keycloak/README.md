# Keycloak Configuration

## Realm Configuration

The `realm.json` file included in this project initializes Keycloak with a sample configuration for the `cvmanager` realm. This includes creating a test user with the below credentials:

- **Email:** `test@gmail.com`
- **Password:** `tester`

## Keycloak Theme

A sample keycloak theme is provided in the `sample_theme.jar` file. This is a sample theme generated using [Keycloakify](https://github.com/CDOT-CV/keycloakify-starter), to use a custom theme put a generated .jar file in this directory and then update the `KEYCLOAK_LOGIN_THEME_NAME` with the name of the new .jar file.
