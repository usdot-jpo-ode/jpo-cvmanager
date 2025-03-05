package us.dot.its.jpo.ode.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("PermissionService")
public class PermissionService {

    private final PostgresService postgresService;

    private static final Map<String, Integer> ROLE_HIERARCHY = new HashMap<>();

    static {
        ROLE_HIERARCHY.put("ADMIN", 3);
        ROLE_HIERARCHY.put("OPERATOR", 2);
        ROLE_HIERARCHY.put("USER", 1);
    }

    @Autowired
    public PermissionService(PostgresService postgresService) {
        this.postgresService = postgresService;
    }

    public static boolean checkRoleAbove(String userRole, String requiredRole) {
        if (userRole == null) {
            return false;
        }
        List<String> roles = List.of("USER", "OPERATOR", "ADMIN");
        return roles.indexOf(userRole.toUpperCase()) >= roles.indexOf(requiredRole.toUpperCase());
    }

    // Allow Connection if the user is a SuperUser
    public boolean isSuperUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        String username = getUsername(auth);
        Users user = postgresService.findUser(username);

        return user != null && user.isSuper_user();
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
            String userRole = postgresService.getUserRoleInOrg(username, organization);
            return checkRoleAbove(userRole, role);
        }

        return !postgresService.getQualifiedOrgList(username, role).isEmpty();
    }

    // Allow Connection if the users organization controls the specified
    // intersection
    public boolean hasIntersection(Integer intersectionID, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (intersectionID == -1 || intersectionID == null) {
            return true;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        String organization = getOrganizationFromHeader();
        if (organization != null) {
            return postgresService.checkIntersectionWithOrg(intersectionID.toString(), List.of(organization));
        }

        return postgresService.checkIntersectionWithOrg(intersectionID.toString(),
                postgresService.getQualifiedOrgList(username, role));
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
            return postgresService.checkRsuWithOrg(rsuIP, List.of(organization));
        }

        return postgresService.checkRsuWithOrg(rsuIP, postgresService.getQualifiedOrgList(username, role));
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