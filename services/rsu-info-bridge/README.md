# RSU Info Bridge

## Overview
The RSU Info Bridge is a Spring Boot-based microservice designed to provide a standardized interface for external systems to retrieve information about Roadside Units (RSUs).

## Getting Started

### Prerequisites
- Java 25 JDK
- Maven 3.9+

### Building the Project
To build the project and run tests, use the following command:

```bash
./mvnw clean install
```

## Running the Application
### Using Maven
You can run the application using the Spring Boot Maven plugin:

```bash
./mvnw spring-boot:run
```

The service will be available at `http://localhost:16543` (default port).

### Using Docker Compose
First, copy the sample environment file:

```bash
cp sample.env .env
```

Then, build the Docker image using the Spring Boot Maven plugin:

```bash
./mvnw spring-boot:build-image
```

Finally, start the service using Docker Compose:

```bash
docker compose up -d
```

## Configuration
Configuration is managed via `src/main/resources/application.yaml`.

## Running Tests

```bash
./mvnw test
```
