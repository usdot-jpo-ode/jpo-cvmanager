package us.dot.its.jpo.ode.api.models.keycloak;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import lombok.Getter;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;

@Getter
public class CvManagerAuthToken extends JwtAuthenticationToken {
    private final Map<Integer, Pair<Organization, UserRole>> orgRoles; // Map<Org, Role>
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
        return superUserObj instanceof Boolean superUserBool && superUserBool;
    }

    protected Map<Integer, Pair<Organization, UserRole>> getOrgRolesFrom(Map<String, Object> claims) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orgList = (List<Map<String, Object>>) claims.get("organizations");

        if (orgList == null) {
            return Map.of();
        }

        return orgList.stream()
                .map(value -> {
                    Organization org = new Organization();
                    org.setId(Integer.parseInt(value.get("org_id").toString()));
                    org.setName((String) value.get("org_name"));
                    org.setEmail((String) value.get("org_email"));
                    return Pair.of(org, UserRole.fromString((String) value.get("role")));
                })
                .collect(Collectors.toMap(
                        m -> m.getLeft().getId(),
                        m -> m));
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
        String emailClaim = jwt.getClaimAsString("email");
        if (emailClaim != null && !emailClaim.isEmpty()) {
            return emailClaim;
        }

        // Try "preferred_username" (often contains email in Keycloak)
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isEmpty()) {
            return preferredUsername;
        }

        return null;
    }

    public boolean hasRoleInOrg(Integer orgId, String role) {
        return UserRole.fromString(role).equals(orgRoles.get(orgId) != null ? orgRoles.get(orgId).getRight() : null);
    }

    public Set<Organization> getAllOrgs() {
        return orgRoles.keySet().stream()
                .map(orgRoles::get)
                .filter(pair -> pair != null && pair.getLeft() != null)
                .map(Pair::getLeft)
                .collect(Collectors.toSet());
    }

    /**
     * Gets list of organizations where the user has a role at or above the required
     * level.
     * Role hierarchy: ADMIN > OPERATOR > USER
     * 
     * @param requiredRole Minimum required role (e.g., "USER", "OPERATOR", "ADMIN")
     * @return List of organization names where user meets the role requirement
     */
    public List<Organization> getQualifiedOrgList(UserRole requiredRole) {
        return orgRoles.entrySet().stream()
                .filter(entry -> entry != null && entry.getValue() != null
                        && entry.getValue().getRight().hasMinimumRole(requiredRole))
                .map(entry -> entry.getValue().getLeft())
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
        for (Map.Entry<Integer, Pair<Organization, UserRole>> entry : orgRoles.entrySet()) {
            if (entry == null) {
                continue;
            }
            Organization organization = entry.getValue().getLeft();
            if (organization != null && organization.getName().equals(orgName)) {
                return Optional.ofNullable(entry.getValue().getRight());
            }
        }
        return Optional.empty();
    }
}