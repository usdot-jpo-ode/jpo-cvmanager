# Intersection Api

The Intersection API enables users to see events and assessments generated by the conflict monitor application.
For more information on this api, see the parent [README.md](../README.md)

## Setup

(Optional) Fill in any additional data in the [application.properties](application.properties) file before running the API to ensure proper operation.

## Running Locally

The intersection API requires the following dependencies be installed to run locally

- Java 21
- Maven
- For tests
  - Docker **(Engine must be running)**

Additionally there are other dependencies installed through maven.
Before building the intersection-api. Make sure that local copies of the ODE, JPO-GeoJsonConverter, and JPO-ConflictMonitor have been built and installed on your system. For instructions on building these locally, please see each ones respective repository.

### Create an application-dev.yaml

1. Navigate to the `src/main/resources` directory:

   ```sh
   cd src/main/resources
   ```

2. Copy the contents of the `application.yaml` file to a new file named `application-dev.yaml`:

   ```sh
   cp application.yaml application-dev.yaml
   ```

3. Replace all environment variable references in `application-dev.yaml` with your local variables (usually copied from the project root .env file).

### Project Updates

If running alongside the rest of the cv-manager, please make the following updates to your root .env file:

1. Use the intersection_no_api docker profile
   If intending to start the mongodb and kafka services through docker, use the `intersection_no_api` profile, instead of the `intersection` profile. This is exactly the same, but allows the API to be run locally instead of through docker. This is updated through the COMPOSE_PROFILES environment variable, like so:

```
COMPOSE_PROFILES=basic,webapp,intersection_no_api
```

2. If using alongside the webapp, update the following Webapp env variables:

```
CVIZ_API_SERVER_URL=http://localhost:8089
CVIZ_API_WS_URL=ws://localhost:8089
```

### Run the application

Install and run the intersection API using the following commands:

1. **Clean and install the project**:

   ```sh
   mvn clean install
   ```

2. **Run the application with the `dev` profile**:

   ```sh
   mvn -Dspring-boot:run.profiles=dev spring-boot:run
   ```

## Running Tests

To run unit tests, run the following command (ensure docker engine is running):

```sh
mvn test
```

## Swagger API

The Intersection API utilizes swagger for viewing and testing api endpoints. The Swagger endpoint can be accessed here:
http://localhost:8088/swagger-ui/index.html

To facilitate easy development of front-end applications, the Intersection API is equipped with the capability to provide testData from each of its endpoints. To retrieve test data, set the url param testData to True when making the request.
