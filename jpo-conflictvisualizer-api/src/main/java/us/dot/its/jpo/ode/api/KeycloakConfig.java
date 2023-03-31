package us.dot.its.jpo.ode.api;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
 
// provides keycloak based spring security configuration
// annotation covers 2 annotations - @Configuration and @EnableWebSecurity
@KeycloakConfiguration

public class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Value("${security.enabled:true}")
    private boolean securityEnabled;
 
    // sets KeycloakAuthenticationProvider as an authentication provider
    // sets SimpleAuthorityMapper as the authority mapper
    @Autowired
    protected void configureGlobal(final AuthenticationManagerBuilder auth) {
        final KeycloakAuthenticationProvider provider = super.keycloakAuthenticationProvider();
        provider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(provider);
    }
    
 
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }
 
    // ensure that spring boot will resolve the keycloak configuration 
    // from application.yml (or application.properties)
    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }
 
    @Override
    protected void configure(final HttpSecurity httpSecurity) throws Exception {
        super.configure(httpSecurity);
        if(securityEnabled){
            System.out.println("Running with KeyCloak Authentication");
            httpSecurity
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            .anyRequest().fullyAuthenticated();
        }else{
            System.out.println("Running without KeyCloak Authentication");
            httpSecurity.csrf().disable().authorizeRequests().anyRequest().permitAll();
        }
    }


    // This is condition allows for disabling securit
    @ConditionalOnProperty(prefix = "security",
    name = "enabled",
    havingValue = "true")
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class Dummy {
    }
}