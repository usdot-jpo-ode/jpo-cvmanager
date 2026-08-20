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
Run the application using the Spring Boot Maven plugin:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Spring Boot's Docker Compose integration will automatically start the PostgreSQL database container from the root `docker-compose.yml` when the application starts.

The service will be available at `http://localhost:16543` (default port).

### Using Docker Compose
First, build the Docker image (see [Building Docker Images](#building-docker-images) below).

Then, copy the sample environment file:

```bash
cp sample.env .env
```

Then start the service using Docker Compose:

```bash
docker compose up -d
```

This will start both the PostgreSQL database (from the root docker-compose.yml) and the RSU Info Bridge service. The required profiles are configured in the `.env` file via `COMPOSE_PROFILES`.

### Accessing the API Documentation
Once the application is running, you can access the Swagger UI to view and interact with the OpenAPI documentation:

```
http://localhost:16543/swagger-ui.html
```

The OpenAPI specification is also available in JSON format at:

```
http://localhost:16543/v3/api-docs
```

## Building Docker Images

### Jib (Recommended for GKE)
For deployment to Google Kubernetes Engine (GKE), use the Jib Maven plugin to build the Docker image:

```bash
./mvnw compile jib:dockerBuild
```

### Spring Boot Build Image

> **Warning:** Images built with `spring-boot:build-image` may cause `CreateContainerError` in GKE due to "too many symbolic links". Use the Jib approach above for GKE deployments.

For local development or non-GKE environments, you can use the Spring Boot Maven plugin:

```bash
./mvnw spring-boot:build-image
```

## Configuration
Configuration is managed via `src/main/resources/application.yaml`.

## Running Tests

```bash
./mvnw test
```
