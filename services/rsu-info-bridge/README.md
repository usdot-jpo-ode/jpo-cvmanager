# RSU Info Bridge

## Overview
The RSU Info Bridge is a Spring Boot-based microservice designed to provide a standardized interface for external systems to retrieve information about Roadside Units (RSUs).

## Getting Started

### Prerequisites
- Java 21 JDK
- Maven 3.9+

### Building the Project
To build the project and run tests, use the following command:

```bash
./mvnw clean install
```

### Running the Application
You can run the application using the Spring Boot Maven plugin:

```bash
./mvnw spring-boot:run
```

The service will be available at `http://localhost:16543` (default port).

## Configuration
Configuration is managed via `src/main/resources/application.yaml`.

## Running Tests

```bash
./mvnw test
```
