package us.dot.its.jpo.ode.api.keycloak.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String resource;

    @Value("${keycloak.auth-server-url}")
    private String authServer;

    @Value("${keycloak_username}")
    private String username;

    @Value("${keycloak_password}")
    private String password;

    // Keycloak admin client used for email
    @Bean
    public Keycloak keyCloakBuilder() {
        System.out.println("Auth Server: " + authServer);
        System.out.println("Realm: " + realm);
        System.out.println("Resource: " + resource);
        return KeycloakBuilder.builder()
                .serverUrl(authServer)
                .grantType("password")
                .realm("master")
                .clientId("admin-cli")
                .username(username)
                .password(password)
                .build();
    }
}
