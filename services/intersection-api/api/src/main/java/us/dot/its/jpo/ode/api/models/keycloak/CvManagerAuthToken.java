package us.dot.its.jpo.ode.api.models.keycloak;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import lombok.Getter;
import us.dot.its.jpo.ode.api.models.UserRole;

@Getter
public class CvManagerAuthToken extends JwtAuthenticationToken {
    private final Map<String, UserRole> orgRoles; // Map<Org, Role>
    private final boolean isSuperUser;
    private final String email;

    public CvManagerAuthToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, String username) {
        super(jwt, authorities, username);
        Map<String, Object> cvmanagerClaims = Optional.ofNullable(jwt.getClaimAsMap("cvmanager_data")).orElse(Map.of());
        this.orgRoles = getOrgRolesFrom(cvmanagerClaims);
        this.isSuperUser = getIsSuperUserFrom(cvmanagerClaims);
        this.email = getEmailFrom(jwt);
    }

    protected Boolean getIsSuperUserFrom(Map<String, Object> claims) {
        Object superUserObj = claims.get("super_user");
        if (superUserObj instanceof String superUserStr) {
            return "1".equals(superUserStr);
        }
        return false;
    }

    protected Map<String, UserRole> getOrgRolesFrom(Map<String, Object> claims) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> orgList = (List<Map<String, String>>) claims.get("organizations");

        if (orgList == null) {
            return Map.of();
        }

        return orgList.stream()
                .collect(Collectors.toMap(m -> m.get("org"), m -> UserRole.fromString(m.get("role"))));
    }

    /**
     * Extracts email address from JWT token.
     * Tries multiple standard claim names in order of preference:
     * 1. "email" (standard OIDC claim)
     * 2. "preferred_username" (Keycloak often uses this for email)
     * 
     * @param jwt The JWT token
     * @return Email address if found, null otherwise
     */
    protected String getEmailFrom(Jwt jwt) {
        // Try standard "email" claim first
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isEmpty()) {
            return email;
        }

        // Try "preferred_username" (often contains email in Keycloak)
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isEmpty()) {
            return preferredUsername;
        }

        return null;
    }

    public boolean hasRoleInOrg(String orgId, String role) {
        return UserRole.fromString(role).equals(orgRoles.get(orgId));
    }

    public Set<String> getAllOrgs() {
        return orgRoles.keySet();
    }

    /**
     * Gets list of organizations where the user has a role at or above the required
     * level.
     * Role hierarchy: ADMIN > OPERATOR > USER
     * 
     * @param requiredRole Minimum required role (e.g., "USER", "OPERATOR", "ADMIN")
     * @return List of organization names where user meets the role requirement
     */
    public List<String> getQualifiedOrgList(UserRole requiredRole) {
        return orgRoles.entrySet().stream()
                .filter(entry -> entry != null && entry.getValue().hasMinimumRole(requiredRole))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Finds the user's role in a specific organization (case-insensitive).
     * 
     * @param orgName Name of the organization to search for
     * @return Optional containing the role if found, empty otherwise
     */
    public Optional<UserRole> findRoleInOrg(String orgName) {
        if (orgName == null) {
            return Optional.empty();
        }
        for (Map.Entry<String, UserRole> entry : orgRoles.entrySet()) {
            if (entry == null) {
                continue;
            }
            String organizationName = entry.getKey();
            if (organizationName != null && organizationName.equalsIgnoreCase(orgName)) {
                return Optional.ofNullable(entry.getValue());
            }
        }
        return Optional.empty();
    }
}