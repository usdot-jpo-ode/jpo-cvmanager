package us.dot.its.jpo.ode.api;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;

public class MockKeyCloakAuth {
    public static void setSecurityContextHolder(String user, Set<String> roles){
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        // type safe "when" doesn't work here
        doReturn(authorities).when(authentication).getAuthorities();

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
