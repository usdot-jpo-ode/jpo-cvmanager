package us.dot.its.jpo.ode.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Set;

//import org.keycloak.adapters.OidcKeycloakAccount;
//import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockKeyCloakAuth {
    public static void setSecurityContextHolder(String user, Set<String> roles){
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user);

//        OidcKeycloakAccount account = mock(OidcKeycloakAccount.class);
//        when(account.getRoles()).thenReturn(roles);
//        when(account.getPrincipal()).thenReturn(principal);
//
//        KeycloakAuthenticationToken authentication = mock(KeycloakAuthenticationToken.class);
//        when(authentication.getAccount()).thenReturn(account);
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
