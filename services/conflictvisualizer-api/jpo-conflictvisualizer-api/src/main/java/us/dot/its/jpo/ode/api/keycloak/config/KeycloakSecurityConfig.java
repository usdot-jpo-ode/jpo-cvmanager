package us.dot.its.jpo.ode.api.keycloak.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.keycloak.support.AccessController;
import us.dot.its.jpo.ode.api.keycloak.support.CorsUtil;
import us.dot.its.jpo.ode.api.keycloak.support.KeycloakJwtAuthenticationConverter;


/**
 * Provides keycloak based spring security configuration.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security",
        name = "enabled",
        havingValue = "true")   // Allow disabling security
public class KeycloakSecurityConfig {

    final ConflictMonitorApiProperties properties;

    final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        System.out.println("Running with KeyCloak Authentication");

        return httpSecurity
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfigurer -> CorsUtil.configureCors(corsConfigurer, properties))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow CORS preflight
                        .requestMatchers("/**").access(AccessController::checkAccess)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServerConfigurer -> resourceServerConfigurer.jwt(
                        jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)

                ))
                .build();

    }


    @Bean
    AccessController accessController() {
        return new AccessController();
    }



}