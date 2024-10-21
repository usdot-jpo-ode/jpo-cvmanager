package us.dot.its.jpo.ode.api;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.mockito.Mockito.*;

public class MockKeyCloakAuth {
    public static void setSecurityContextHolder(String user, Set<String> roles){
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user);

        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("http://localhost:3000");

        Map<String, Object> headers = new HashMap<>();
        headers.put("allowed-origins", allowedOrigins);

        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", user);


        Jwt token = new Jwt(user, Instant.now(), Instant.now().plusSeconds(10), headers, claims);

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getToken()).thenReturn(token);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        // type safe "when" doesn't work here
        doReturn(authorities).when(authentication).getAuthorities();

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
