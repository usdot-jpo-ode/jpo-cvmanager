package us.dot.its.jpo.ode.api;

import java.util.List;

// import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
 
// provides keycloak based spring security configuration
// annotation covers 2 annotations - @Configuration and @EnableWebSecurity
@KeycloakConfiguration
@EnableWebSecurity
public class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

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

    @Bean
    public Keycloak keyCloakBuilder() {
        System.out.println("Auth Server" + authServer);
        System.out.println("Realm" + realm);
        System.out.println("Resource" + resource);
        System.out.println("Username" +username);
        System.out.println("Password" + password);
        System.out.println(password);
        Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(authServer)
        .grantType("password")
        .realm("master")
        .clientId("admin-cli")
        .username(username)
        .password(password)
        .build();

        System.out.println(keycloak);
        

        List<UserRepresentation> test = keycloak.realm(realm).users().list();
        for(UserRepresentation user : test){
            System.out.println("User: "+ user.getUsername());
        }


        return keycloak;
    }
 
    @Override
    protected void configure(final HttpSecurity httpSecurity) throws Exception {
        super.configure(httpSecurity);

        if(securityEnabled){
            System.out.println("Running with KeyCloak Authentication");            
            httpSecurity
            .cors()
            .and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            .anyRequest().authenticated();
        }else{
            System.out.println("Running without KeyCloak Authentication");
            httpSecurity
            .cors()
            .and()
            .csrf().disable()
            .authorizeRequests().anyRequest().permitAll();
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