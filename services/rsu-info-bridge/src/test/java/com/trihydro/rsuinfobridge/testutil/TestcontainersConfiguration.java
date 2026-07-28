package com.trihydro.rsuinfobridge.testutil;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
            .parse("postgis/postgis:15-3.4-alpine")
            .asCompatibleSubstituteFor("postgres");

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> postgisContainer() {
        return new PostgreSQLContainer<>(POSTGIS_IMAGE);
    }

    // Spring Boot 4 removed FlywayAutoConfiguration so wire it explicitly.
    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .mixed(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}
