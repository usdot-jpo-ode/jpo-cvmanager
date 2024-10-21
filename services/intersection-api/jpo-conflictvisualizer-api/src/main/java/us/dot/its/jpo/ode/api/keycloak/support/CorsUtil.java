package us.dot.its.jpo.ode.api.keycloak.support;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

import java.util.List;

/**
 * Provides utility methods for configuring CORS
 */
public class CorsUtil {

    /**
     * Configures CORS
     *
     * @param cors mutable cors configuration
     *
     *
     */
    public static void configureCors(CorsConfigurer<HttpSecurity> cors, ConflictMonitorApiProperties properties) {

        UrlBasedCorsConfigurationSource defaultUrlBasedCorsConfigSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.addAllowedOrigin(properties.getCors());
        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS").forEach(corsConfiguration::addAllowedMethod);
        defaultUrlBasedCorsConfigSource.registerCorsConfiguration("/**", corsConfiguration);

        cors.configurationSource(req -> {

            CorsConfiguration config = new CorsConfiguration();

            config = config.combine(defaultUrlBasedCorsConfigSource.getCorsConfiguration(req));

            // check if request Header "origin" is in white-list -> dynamically generate cors config

            return config;
        });
    }
}
