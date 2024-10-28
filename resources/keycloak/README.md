# Keycloak Configuration

## Realm Configuration

The `realm.json` file included in this project initializes Keycloak with a sample configuration for the `cvmanager` realm. This includes creating a test user with the below credentials:

- **Email:** `test@gmail.com`
- **Password:** `tester`

## Keycloak Theme

A sample keycloak theme is provided in the `sample_theme.jar` file. This is a sample theme generated using [Keycloakify](https://github.com/CDOT-CV/keycloakify-starter), to use a custom theme put a generated .jar file in this directory and then update the `KEYCLOAK_LOGIN_THEME_NAME` with the name of the new .jar file.

## TLS Configuration

Due to the addition of the Keycloak custom user provider, a Java keystore is now required to build the Keycloak image. For development, the Dockerfile_localdev dockerfile automatically generates a self-signed certificate. For production deployments, a custom certificate should be generated and loaded into the image as a volume before being built. This process is as follows:

1. Create a certificate to be used by Keycloak. This should ideally be signed by a CA
   - The code to generate a self-signed certificate is as follows:

```sh
openssl req -newkey rsa:2048 -nodes -keyout key.pem -x509 -days 3650 -out cert.pem
```

2. Create a random password to be used for the java keystore. Set this in the docker image as en env variable "KEYSTORE_PASSWORD"
3. Load the certificate.crt and private.key files into the docker build as a volume, mounted under the /cert directory
4. Build the docker image!
