package us.dot.its.jpo.ode.api.keycloak.support;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

/**
 * Converts a JWT into a Spring authentication token (by extracting
 * the username and roles from the claims of the token, delegating
 * to the {@link KeycloakGrantedAuthoritiesConverter})
 */
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter;

    public KeycloakJwtAuthenticationConverter(Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter) {
        this.grantedAuthoritiesConverter = grantedAuthoritiesConverter;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
        String username = getUsernameFrom(jwt);

        var token = new JwtAuthenticationToken(jwt, authorities, username);
        return token;
    }

    protected String getUsernameFrom(Jwt jwt) {

        if (jwt.hasClaim("preferred_username")) {
            return jwt.getClaimAsString("preferred_username");
        }

        return jwt.getSubject();
    }
}
