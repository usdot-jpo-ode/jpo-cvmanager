package us.dot.its.jpo.ode.api.keycloak.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.keycloak.support.CorsUtil;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Alternative keycloack configuration for when security is disabled
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security",
        name = "enabled",
        havingValue = "false")   // Allow disabling security
public class KeycloakNoSecurityConfig {

    final ConflictMonitorApiProperties properties;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        System.out.println("Running without KeyCloak Authentication");
        return httpSecurity
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfigurer -> CorsUtil.configureCors(corsConfigurer, properties))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        request -> request.anyRequest().permitAll()
                )
                .anonymous(withDefaults())
                .build();

    }


}
