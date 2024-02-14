package us.dot.its.jpo.ode.api;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

// provides keycloak based spring security configuration
// annotation covers 2 annotations - @Configuration and @EnableWebSecurity
//@KeycloakConfiguration
@Configuration
@EnableWebSecurity
public class KeycloakConfig  {

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

    @Value("${keycloak.redirect-server-url}")
    private String redirectServer;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public ClientRegistrationRepository clientRepository() {
        ClientRegistration keycloak = keycloakClientRegistration();
        return new InMemoryClientRegistrationRepository(keycloak);
    }

    private ClientRegistration keycloakClientRegistration() {

        return ClientRegistration
                .withRegistrationId(realm)
                .clientId(resource)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .issuerUri(authServer + "/realms/" + realm)
                .redirectUri(redirectServer)
                .authorizationUri(authServer + "/realms/" + realm + "/protocol/openid-connect/auth")
                .tokenUri(authServer + "/realms/" + realm + "/protocol/openid-connect/token")
                .userInfoUri(authServer + "/realms/" + realm + "/protocol/openid-connect/userinfo")
                .userInfoAuthenticationMethod(AuthenticationMethod.HEADER)
                .build();
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        if(securityEnabled){
            System.out.println("Running with KeyCloak Authentication");

            return httpSecurity
                    .cors(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(request -> {
                                request.requestMatchers("/**").permitAll();
                                request.anyRequest().fullyAuthenticated();
                            }
                    )
                    .oauth2Client(withDefaults())
                    .build();
        }else{
            System.out.println("Running without KeyCloak Authentication");
            return httpSecurity
                    .cors(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(
                        request -> request.anyRequest().permitAll()
                    )
                    .oauth2Client(withDefaults())
                    .build();
        }
    }




    @Bean
    public Keycloak keyCloakBuilder() {
        System.out.println("Auth Server: " + authServer);
        System.out.println("Realm: " + realm);
        System.out.println("Resource: " + resource);
        Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(authServer)
        .grantType("password")
        .realm("master")
        .clientId("admin-cli")
        .username(username)
        .password(password)
        .build();
        return keycloak;
    }


}