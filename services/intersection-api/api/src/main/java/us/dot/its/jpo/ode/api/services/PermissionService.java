package us.dot.its.jpo.ode.api.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RoleRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository.UserOrgRoleProjection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("PermissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IntersectionRepository intersectionRepository;
    private final RsuRepository rsuRepository;

    private static final Map<String, Integer> ROLE_HIERARCHY = new HashMap<>();

    static {
        ROLE_HIERARCHY.put("ADMIN", 3);
        ROLE_HIERARCHY.put("OPERATOR", 2);
        ROLE_HIERARCHY.put("USER", 1);
    }

    public static boolean checkRoleAbove(String userRole, String requiredRole) {
        if (userRole == null) {
            return false;
        }
        List<String> roles = List.of("USER", "OPERATOR", "ADMIN");
        return roles.indexOf(userRole.toUpperCase()) >= roles.indexOf(requiredRole.toUpperCase());
    }

    private List<String> getQualifiedOrgList(String email, String requiredRole) {
        List<UserOrgRoleProjection> organizationRoles = userRepository.findUserOrgRoles(email);
        return getQualifiedOrgList(organizationRoles, requiredRole);
    }

    private List<String> getQualifiedOrgList(List<UserOrgRoleProjection> organizationRoles, String requiredRole) {
        return organizationRoles.stream()
                .filter(entry -> PermissionService.checkRoleAbove(entry.getRoleName(), requiredRole))
                .map(UserOrgRoleProjection::getOrganizationName)
                .collect(Collectors.toList());
    }

    public List<Integer> getAllowedIntersectionIdsByEmail(String email) {
        return intersectionRepository.findAllowedIntersectionIdsByEmail(email).stream().map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<Integer> getAllowedIntersectionIdsByOrganization(String email) {
        return intersectionRepository.findIntersectionsByOrganization(email).stream().map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    // Allow Connection if the user is a SuperUser
    public boolean isSuperUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        String username = getUsername(auth);
        User user = userRepository.findByEmail(username);

        return user.getSuperUser();
    }

    // Allow Connection if the user is a part of at least one organization with a
    // matching roll.
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        String organization = getOrganizationFromHeader();
        if (organization != null) {
            Optional<String> userRole = roleRepository.findUserRoleInOrg(username, organization);
            return userRole.map(roleValue -> checkRoleAbove(roleValue, role)).orElse(false);
        }

        return !getQualifiedOrgList(username, role).isEmpty();
    }

    // Allow Connection if the users organization controls the specified
    // intersection
    public boolean hasIntersection(Integer intersectionID, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        // Must be null check first, otherwise throws null pointer exception (if null)
        if (intersectionID == null || intersectionID == -1) {
            return true;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        String organization = getOrganizationFromHeader();
        if (organization != null) {
            return intersectionRepository.existsByIdAndOrganizations(intersectionID.toString(), List.of(organization));
        }

        return intersectionRepository.existsByIdAndOrganizations(intersectionID.toString(),
                getQualifiedOrgList(username, role));
    }

    // Allow Connection if the users organization controls the specified RSU unit
    public boolean hasRSU(String rsuIP, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        String organization = getOrganizationFromHeader();
        if (organization != null) {
            return rsuRepository.existsByIpAndOrganizations(rsuIP, List.of(organization));
        }

        return rsuRepository.existsByIpAndOrganizations(rsuIP, getQualifiedOrgList(username, role));
    }

    // helper method to make sure authentication is valid
    public boolean isAuthValid(Authentication auth) {
        if (!auth.isAuthenticated()) {
            return false;
        }

        return auth instanceof JwtAuthenticationToken;
    }

    public static String getUsername(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        return jwtAuth.getToken().getClaimAsString("preferred_username");
    }

    public static String getOrganizationFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String organization = null;
        if (attributes != null) {
            organization = attributes.getRequest().getHeader("Organization");
        }
        return organization;
    }
}