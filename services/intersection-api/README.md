# CVManager Intersection API

The cvmanager intersection-api is built off of the conflictvisualizer api. This directory contains that customized api.

This application is fully dockerized, build to run alongside an instance of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode), [jpo-geojsonconverter](https://github.com/usdot-jpo-ode/jpo-geojsonconverter), [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor). Information on how to build and run those projects is available in their repositories.

The docker-compose available in this repository will build the conflictvisualizer-api along with Kafka, and possibly

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
The root docker-compose-full-cm.yml file contains the latest released images for the jpo-ode, jpo-geojsonconverter, and jpo-conflictmonitor. To run these images, make sure the "intersection" docker profile is set (COMPOSE_PROFILES), and build the root docker project as normal:

```sh
docker compose up -d
```

Additionally, running the "conflictmonitor" docker profile will run the conflictmonitor, geojsonconverter, ode, and kafka connect services, which enable additional features like live data streaming (stomp websockets)

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

1. Make sure to set the JPO ConflictMonitor and cvmanager intersection api environment variables in the root .env file (from sample.env)

2. Make sure your GitHub token is set up, otherwise required maven packages will fail to authenticate. For instructions on this process, see [README.md GitHub Token](../../README.md#github-token) to generate and set your GitHub token.

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

#### Github Token Setup

1. Create a copy of settings.xml and save it to ~/.m2/settings.xml
2. Create a copy of [settings.xml](jpo-conflictvisualizer-api/settings.xml) and save it to `~/.m2/settings.xml`
3. Update the variables in your `~/.m2/settings.xml` with the token value and target jpo-ode organization. Here is an example filled in `settings.xml` file:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<settings>
    <activeProfiles>
        <activeProfile>default</activeProfile>
    </activeProfiles>
    <servers>
        <server>
            <id>github</id>
            <username>jpo_conflictvisualizer</username>
            <password>**ghp_token-string-value**</password>
        </server>
        <server>
            <id>github_jpo_ode</id>
            <username>jpo_conflictvisualizer</username>
            <password>**ghp_token-string-value**</password>
        </server>
        <server>
            <id>github_jpo_geojsonconverter</id>
            <username>jpo_conflictvisualizer</username>
            <password>**ghp_token-string-value**</password>
        </server>
        <server>
            <id>github_jpo_conflictmonitor</id>
            <username>jpo_conflictvisualizer</username>
            <password>**ghp_token-string-value**</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>default</id>
            <repositories>
                <repository>
                    <id>github</id>
                    <name>GitHub Apache Maven Packages</name>
                    <url>https://maven.pkg.github.com/usdot-jpo-ode/jpo-ode</url>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
                <repository>
                    <id>github_jpo_ode</id>
                    <name>GitHub JPO ODE</name>
                    <url>https://maven.pkg.github.com/usdot-jpo-ode/jpo-ode</url>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
                <repository>
                    <id>github_jpo_geojsonconverter</id>
                    <name>GitHub JPO GeojsonConverter</name>
                    <url>https://maven.pkg.github.com/usdot-jpo-ode/jpo-geojsonconverter</url>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
                <repository>
                    <id>github_jpo_conflictmonitor</id>
                    <name>GitHub JPO ConflictMonitor</name>
                    <url>https://maven.pkg.github.com/usdot-jpo-ode/jpo-conflictmonitor</url>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>
</settings>
```

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
