package us.dot.its.jpo.ode.api.keycloak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import us.dot.its.jpo.ode.api.keycloak.support.KeycloakGrantedAuthoritiesConverter;
import us.dot.its.jpo.ode.api.keycloak.support.KeycloakJwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Configures JWT handling (decoder and validator)
 */
@Configuration
@ConditionalOnProperty(prefix = "security",
        name = "enabled",
        havingValue = "true")   // Allow disabling security
class JwtSecurityConfig {

    /**
     * Configures a decoder with the specified validators (validation key fetched from JWKS endpoint)
     *
     * @param validators validators for the given key
     * @param properties key properties (provides JWK location)
     * @return the decoder bean
     */
    @Bean
    JwtDecoder jwtDecoder(List<OAuth2TokenValidator<Jwt>> validators, OAuth2ResourceServerProperties properties) {

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder //
                .withJwkSetUri(properties.getJwt().getJwkSetUri()) //
                .jwsAlgorithms(algs -> algs.addAll(Set.of(SignatureAlgorithm.RS256, SignatureAlgorithm.ES256))) //
                .build();

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

        return jwtDecoder;
    }

    /**
     * Configures the token validator. Specifies two additional validation constraints:
     * <p>
     * * Timestamp on the token is still valid
     * * The issuer is the expected entity
     *
     * @param properties JWT resource specification
     * @return token validator
     */
    @Bean
    OAuth2TokenValidator<Jwt> defaultTokenValidator(OAuth2ResourceServerProperties properties) {

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new JwtIssuerValidator(properties.getJwt().getIssuerUri()));

        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    @Bean
    KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter(Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter) {
        return new KeycloakJwtAuthenticationConverter(authoritiesConverter);
    }



    @Bean
    Converter<Jwt, Collection<GrantedAuthority>> keycloakGrantedAuthoritiesConverter(
            GrantedAuthoritiesMapper authoritiesMapper,
            @Value("${keycloak.client-id}") String clientId) {
        return new KeycloakGrantedAuthoritiesConverter(clientId, authoritiesMapper);
    }

}
