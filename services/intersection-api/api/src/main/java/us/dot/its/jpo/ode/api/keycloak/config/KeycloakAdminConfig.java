package us.dot.its.jpo.ode.api.keycloak.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminConfig.class);

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.auth-server-url}")
    private String authServer;

    // Keycloak admin client used for email
    @Bean
    public Keycloak keyCloakBuilder() {
        logger.info("Auth Server: {}", authServer);
        logger.info("Realm: {}", realm);
        logger.info("Client ID: {}", clientId);
        return KeycloakBuilder.builder()
            .serverUrl(authServer)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .build();
    }
}
