package us.dot.its.jpo.ode.api;

//import org.keycloak.adapters.KeycloakConfigResolver;
//import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
//import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
//import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
//import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

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
                .redirectUri(redirectServer + "/login/oauth2/code/" + resource)
                .issuerUri(authServer + "/realms/" + realm)
                .scope("openid")
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
                    .oauth2Login(withDefaults())
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




//
//
//    // sets KeycloakAuthenticationProvider as an authentication provider
//    // sets SimpleAuthorityMapper as the authority mapper
//    @Autowired
//    protected void configureGlobal(final AuthenticationManagerBuilder auth) {
//        final KeycloakAuthenticationProvider provider = super.keycloakAuthenticationProvider();
//        provider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
//        auth.authenticationProvider(provider);
//    }
//
//
//    @Bean
//    @Override
//    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
//
//        return new NullAuthenticatedSessionStrategy();
//    }
//
//    // ensure that spring boot will resolve the keycloak configuration
//    // from application.yml (or application.properties)
//    @Bean
//    public KeycloakConfigResolver keycloakConfigResolver() {
//        return new KeycloakSpringBootConfigResolver();
//    }
//
//    @Bean
//    public Keycloak keyCloakBuilder() {
//        System.out.println("Auth Server: " + authServer);
//        System.out.println("Realm: " + realm);
//        System.out.println("Resource: " + resource);
//        Keycloak keycloak = KeycloakBuilder.builder()
//        .serverUrl(authServer)
//        .grantType("password")
//        .realm("master")
//        .clientId("admin-cli")
//        .username(username)
//        .password(password)
//        .build();
//        return keycloak;
//    }
//
//    @Override
//    protected void configure(final HttpSecurity httpSecurity) throws Exception {
//        super.configure(httpSecurity);
//
//        if(securityEnabled){
//            System.out.println("Running with KeyCloak Authentication");
//            httpSecurity
//            .cors()
//            .and()
//            .csrf().disable()
//            .authorizeRequests()
//            .requestMatchers("/**").permitAll()
//            .anyRequest().fullyAuthenticated();
//        }else{
//            System.out.println("Running without KeyCloak Authentication");
//            httpSecurity
//            .cors()
//            .and()
//            .csrf().disable()
//            .authorizeRequests().anyRequest().permitAll();
//        }
//    }
//
//    @Override
//    public void init(WebSecurity builder) throws Exception {
//
//    }
//
//    @Override
//    public void configure(WebSecurity builder) throws Exception {
//
//    }
//
//
//    // This is condition allows for disabling securit
//    @ConditionalOnProperty(prefix = "security",
//    name = "enabled",
//    havingValue = "true")
//    @EnableGlobalMethodSecurity(prePostEnabled = true)
//    static class Dummy {
//        public Dummy(){
//            System.out.println("Initializing Security");
//        }
//
//    }
}