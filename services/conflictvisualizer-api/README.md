# jpo-conflictvisualizer

The CIMMS Conflict Visualizer is a web-based user interface for configuring the [CIMMS Conflict Monitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor), as well as displaying notifications, downloading data, and visualizing conflicts. This repository also contains the associated API, which hosts endpoints for the GUI to access data from the jpo-conflictmonitor MongoDB database.

This application is fully dockerized, with the API and GUI alongside an instance of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode), [jpo-geojsonconverter](https://github.com/usdot-jpo-ode/jpo-geojsonconverter), [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor). Information on how to build and run those projects is available in their repositories. The docker-compose available in this repository will build three components: the conflictvisualizer-api, the conflictvisualizer-gui and a [Keycloak](https://www.keycloak.org/getting-started/getting-started-docker) server used to authenticate both.

This application is a part of the [JPO Connected Vehicle Portal](https://github.com/usdot-jpo-ode/jpo-cvportal), which is made up of this repository and others, to provide a single application with access to other connected intersection tools.

To provide feedback, we recommend that you create an "issue" in this repository (<https://github.com/usdot-jpo-ode/jpo-conflictvisualizer/issues>). You will need a GitHub account to create an issue. If you don’t have an account, a dialog will be presented to you to create one at no cost.

## Contents

<b>API:</b> Java Spring Boot REST application. Contains submodule of [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor) repo

<b>Description:</b> An application that helps an organization monitor their connected intersections alerting when conflicts occur and visualizing the conflicts to identify real vehicle incidents and issues within RSU message configuration.

<b>Features:</b>

- Login Authentication hosted by
- View and accept Notifications
- View and update configuration parameters
- Visualize Notifications (SPATs, MAPs, and BSMs)
- Query and download data (events/assessments/SPATs/MAPs/BSMs)

## Installation

### 1. Initialize and update submodules

Alternatively, clone the repository first, then import submodules second

```
git submodule update --init --recursive
```

If you get an error about filenames being too long for Git, run this command to enable long file paths:

```
git config --system core.longpaths true
```

### 2. Build and Run jpo-ode, jpo-geojsonconverter, and jpo-conflictmonitor docker images

**Option 1: Released Images**
The root docker-compose-full-cm.yml file contains the latest released images for the jpo-ode, jpo-geojsonconverter, and jpo-conflictmonitor. To run these images, simply run the following command from the root of the project:

```sh
docker compose -f docker-compose-full-cm.yml up -d
```

**Option 2: Build and Run Locally**
Clone, install, and run [ODE](https://github.com/usdot-jpo-ode/jpo-ode#step-2---build-and-run-the-application), then [GeoJSONConverter](https://github.com/usdot-jpo-ode/jpo-geojsonconverter#step-2---build-and-run-jpo-ode-application), then [ConflictMonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor#step-2---build-and-run-jpo-ode-application). Each can be built and run by navigating to their respective directories that contain a pom.xml, then running:

```
mvn install
```

or to skip the tests:

```
mvn install -DskipTests
```

### 3. Setup Docker Environment Variables

1. Make a copy of the root sample.env file ./sample.env

```
cd ../
cp sample.env .env

```

2. Modify the .env file and set the appropriate deployment variables

3. Optional - Modify application.properties and application.yaml files in api/jpo-conflictvisualizer-api/src/main/resources/ and configure them for deployment. Most features are controlled by environment variables, but some features may require additional configuration.

### 4. Start Conflict Visualizer

To run the conflictvisualizer API alongside public images of the ODE, geojsonconverter, and conflictmonitor without the cvmanager api or webapp, use the following command from the root of the project:

```sh
docker compose -f docker-compose-cm-only.yml up --build -d
```

To run the conflictvisualizer API alongside public images of the ODE, geojsonconverter, and conflictmonitor, as well as the webapp and cvmanager api, use the following command from the root of the project:

```sh
docker compose -f docker-compose-full-cm.yml up -d
```

## Development Environments

The below section provides additional instruction on how to setup the conflict visualizer components for development. This section describes how to run the components locally to allow for quick changes and rapid iteration.

### 1. Running Conflict Visualizer API Locally

The conflict visualizer API requires the following dependencies be installed to run locally

- Java 21
- Maven

Additionally there are other dependencies installed through maven.
Before building the conflictvisualizer-api. Make sure that local copies of the ODE, JPO-GeoJsonConverter, and JPO-ConflictMonitor have been built and installed on your system. For instructions on building these locally, please see each ones respective repository.

Once these components have been installed. Download and install additional dependencies for the conflict visualizer using the following:

```

cd api/jpo-conflictvisualizer-api
mvn clean install
mvn spring-boot:run

```

### 2. Running Smtp4dev

An Smtp4dev server can be used locally to test the Email capabilities of the conflict monitor API: [smtp4dev](https://github.com/rnwood/smtp4dev). Once running, this server can be connected to the ap (and Keycloak).

```

```
