package us.dot.its.jpo.ode.api.keycloak.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;

import us.dot.its.jpo.ode.api.auth.EndpointPermissionEvaluator;


/**
 * Enables security annotations via like {@link org.springframework.security.access.prepost.PreAuthorize} and
 * {@link org.springframework.security.access.prepost.PostAuthorize} annotations per-method.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true) // jsr250 = @RolesAllowed
@ConditionalOnProperty(prefix = "security",
        name = "enabled",
        havingValue = "true")   // Allow disabling security
class MethodSecurityConfig {

    private final ApplicationContext applicationContext;

    @Autowired
    public MethodSecurityConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        System.out.println("Method-level security annotations are enabled");
    }

    @Bean
    MethodSecurityExpressionHandler customMethodSecurityExpressionHandler() {
        var expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setApplicationContext(applicationContext);
        expressionHandler.setPermissionEvaluator(permissionEvaluator());
        return expressionHandler;
    }

    @Bean
    GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {
        var mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        return mapper;
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new EndpointPermissionEvaluator();
    }

}
