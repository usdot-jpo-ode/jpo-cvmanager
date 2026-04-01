package com.trihydro.rsuinfobridge.testutil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test configuration that provides a PostGIS-enabled PostgreSQL container
 * initialized with production schema and sample data.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
            .parse("postgis/postgis:15-3.4-alpine")
            .asCompatibleSubstituteFor("postgres");

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> postgisContainer() {
        Path sqlScriptsDir = findSqlScriptsDir();

        return new PostgreSQLContainer<>(POSTGIS_IMAGE)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(sqlScriptsDir.resolve("CVManager_CreateTables.sql")),
                        "/docker-entrypoint-initdb.d/01-schema.sql"
                )
                .withCopyFileToContainer(
                        MountableFile.forHostPath(sqlScriptsDir.resolve("CVManager_SampleData.sql")),
                        "/docker-entrypoint-initdb.d/02-sample-data.sql"
                );
    }

    /**
     * Finds the sql_scripts directory containing CVManager_CreateTables.sql.
     */
    private Path findSqlScriptsDir() {
        Path currentDir = Paths.get("").toAbsolutePath();

        Path[] possiblePaths = {
                // Running from rsu-info-bridge directory
                currentDir.resolve("../../resources/sql_scripts").normalize(),
                // Running from services directory
                currentDir.resolve("../resources/sql_scripts").normalize(),
                // Running from project root
                currentDir.resolve("resources/sql_scripts").normalize(),
        };

        for (Path path : possiblePaths) {
            if (Files.exists(path.resolve("CVManager_CreateTables.sql"))) {
                return path;
            }
        }

        throw new IllegalStateException(
                "Could not find sql_scripts directory. Current directory: " + currentDir +
                        ". Searched paths: " + java.util.Arrays.toString(possiblePaths));
    }
}
