package us.dot.its.jpo.ode.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Shared Testcontainers configuration for integration tests.
 *
 * <p>Declares a single {@link MongoDBContainer} bean annotated with {@link ServiceConnection},
 * which Spring Boot uses to automatically configure {@code spring.data.mongodb.uri}. Import this
 * class in any {@code @SpringBootTest} that needs a real MongoDB connection:
 *
 * <pre>{@code
 * @SpringBootTest
 * @ActiveProfiles("integration-test")
 * @Import(TestcontainersConfiguration.class)
 * class MyIntegrationTest { ... }
 * }</pre>
 *
 * <p>Spring's test context cache ensures that test classes with identical configurations share a
 * single {@link org.springframework.context.ApplicationContext} — and therefore a single container
 * instance — for the duration of the test suite.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer("mongo:7");
    }
}
