package us.dot.its.jpo.ode.api.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom {@link PermissionEvaluator} for method level permission checks.
 */
@Component
@RequiredArgsConstructor
class DefaultPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        System.out.printf("check permission user=%s target=%s permission=%s%n", auth.getName(), targetDomainObject, permission);

        // TODO implement sophisticated permission check here
        return true;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        DomainObjectReference dor = new DomainObjectReference(targetType, targetId.toString());
        return hasPermission(auth, dor, permission);
    }
}
