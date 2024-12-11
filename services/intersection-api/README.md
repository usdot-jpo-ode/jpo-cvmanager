# CVManager Intersection API

The cvmanager intersection-api is built off of the [conflictvisualizer api](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer/tree/cvmgr-cimms-integration/api). This directory contains that customized api.

This application is fully dockerized and is designed to run alongside an instance of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode), [jpo-geojsonconverter](https://github.com/usdot-jpo-ode/jpo-geojsonconverter), [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor). This project imports pre-built images for these services. If you would like to build them locally, information is available in their respective repositories.

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
git config --global core.longpaths true
```

### 2. Run Required Docker Resources

Docker compose profiles allow customization of which features to run. In this case, we want to run all of the basic, intersection, and conflictmonitor services, excluding the conflictvisualizer api. This can be done like so:

CD into the root project directory

```sh
cd ../../
```

Ensure your COMPOSE_PROFILES env var (in root .env) is set to: "basic,intersection_no_api,conflictmonitor"

Run all docker images

```sh
docker compose up -d
```

### 3. Setup Docker Environment Variables

Make sure to set the JPO ConflictMonitor and cvmanager intersection api environment variables in the root .env file (from sample.env)

- Optional - Modify application.properties and application.yaml files in api/jpo-conflictvisualizer-api/src/main/resources/ and configure them for deployment. Most features are controlled by environment variables, but some features may require additional configuration.

#### Github Token

A GitHub token is required to pull artifacts from GitHub repositories. This is required to obtain the jpo-ode jars and must be done before attempting to build this repository.

1. Log into GitHub.
2. Navigate to Settings -> Developer settings -> Personal access tokens.
3. Click "New personal access token (classic)".
   1. As of now, GitHub does not support Fine-grained tokens for obtaining packages.
4. Provide the name "jpo_conflictmonitor"
5. Set an expiration date
6. Select the read:packages scope.
7. Click "Generate token" and copy the token.
8. Set this token as the MAVEN_GITHUB_TOKEN environment variable in the .env file (root and ./services/intersection-api/.env)
9. Create a copy of [settings.xml](jpo-conflictvisualizer-api/settings.xml) and save it to `~/.m2/settings.xml`
10. Update the variables in your `~/.m2/settings.xml` with the token value and target usdot-jpo-ode organization. Here is an example filled in `settings.xml` file:

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
            <password>**github_token**</password>
        </server>
    ... apply same to other servers
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
                ... apply same to other repositories
            </repositories>
        </profile>
    </profiles>
</settings>
```

### 4. Start Intersection API

To run the intersection API, after all of the other services have been run,

```sh
docker compose up --build -d intersection_api
```

## Development Environments

The below section provides additional instruction on how to setup the intersection api for development. This section describes how to run the components locally to allow for quick changes and rapid iteration.

### 1. Running Intersection API Locally

The intersection API requires the following dependencies be installed to run locally

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

Install and run the intersection API using the following commands:

```sh
cd services/intersection-api/jpo-conflictvisualizer-api
mvn clean install
mvn spring-boot:run
```

### 2. Running Smtp4dev

An Smtp4dev server can be used locally to test the Email capabilities of the conflict monitor API: [smtp4dev](https://github.com/rnwood/smtp4dev). Once running, this server can be connected to the api (and Keycloak).
