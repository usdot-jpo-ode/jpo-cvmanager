package us.dot.its.jpo.ode.api.keycloak.support;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * Example for generic custom access checks on request level.
 *
 * Logs access attempts and checks whether authenticated.
 */
public class AccessController {

    private static final AuthorizationDecision GRANTED = new AuthorizationDecision(true);
    private static final AuthorizationDecision DENIED = new AuthorizationDecision(false);

    public static AuthorizationDecision checkAccess(Supplier<Authentication> authentication,
            RequestAuthorizationContext requestContext) {

        var auth = authentication.get();

        return auth.isAuthenticated() ? GRANTED : DENIED;
    }
}
