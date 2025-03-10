package us.dot.its.jpo.ode.api.keycloak.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;

/**
 * Enables security annotations via like
 * {@link org.springframework.security.access.prepost.PreAuthorize} and
 * {@link org.springframework.security.access.prepost.PostAuthorize} annotations
 * per-method.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true) // jsr250 = @RolesAllowed
class MethodSecurityConfig {

    private final ApplicationContext applicationContext;

    public MethodSecurityConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    MethodSecurityExpressionHandler customMethodSecurityExpressionHandler() {

        var expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setApplicationContext(applicationContext);
        return expressionHandler;
    }

    @Bean
    GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {

        var mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        return mapper;
    }

}
