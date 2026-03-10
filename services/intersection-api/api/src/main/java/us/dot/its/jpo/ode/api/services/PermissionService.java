package us.dot.its.jpo.ode.api.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RoleRepository;
import us.dot.its.jpo.ode.api.repositories.RsuCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository.UserOrgRoleProjection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
    private final RsuCredentialRepository rsuCredentialRepository;
    private final SnmpCredentialRepository snmpCredentialRepository;

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

    public List<String> getQualifiedOrgList(String email, String requiredRole) {
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

    /**
     * Checks if the currently authenticated user has the specified role in at least
     * one organization. The method verifies the user's roles within the context of
     * organizations they belong to and determines if the role criteria is satisfied.
     *
     * @param role the role to be checked against the user's permissions within organizations
     * @return true if the user possesses the specified role in at least one organization,
     *         or is a superuser; otherwise, false
     */
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

    /**
     * Determines if the authenticated user has a specific role in the given organization.
     *
     * @param organization the name of the organization to check the user's role in
     * @param role the role to be validated within the specified organization
     * @return true if the user has the specified role or a role above it in the organization,
     *         or if the user is a superuser; false otherwise
     */
    public boolean hasRoleInOrg(String organization, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);
        Optional<String> userRole = roleRepository.findUserRoleInOrg(username, organization);
        return userRole.map(roleValue -> checkRoleAbove(roleValue, role)).orElse(false);
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
    public boolean hasRsu(String rsuIP, String role) {
        InetAddress ipv4Address;
        try {
            ipv4Address = InetAddress.getByName(rsuIP);
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSU IP address: " + rsuIP, e);
        }

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
            return rsuRepository.existsByIpAndOrganizations(ipv4Address, List.of(organization));
        }

        return rsuRepository.existsByIpAndOrganizations(ipv4Address, getQualifiedOrgList(username, role));
    }

    // Allow Connection if the users organization controls the specified RSU unit
    public boolean hasRsus(List<String> rsuIP, String role) {
        List<InetAddress> ipv4Addresses = new ArrayList<>();
        for (String ip : rsuIP) {
            try {
                ipv4Addresses.add(InetAddress.getByName(ip));
            } catch (UnknownHostException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSU IP address: " + ip, e);
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        List<String> authorizedOrgs = getQualifiedOrgList(username, role);
        List<InetAddress> allowedRsuIps = rsuRepository.findAllowedRsuIpsInOrganizations(authorizedOrgs);
        return allowedRsuIps.containsAll(ipv4Addresses);
    }

    /**
     * Checks if an RSU credential exists in any organization where the authenticated user
     * has the specified role or higher.
     *
     * @param nickname The unique identifier or nickname of the RSU credential to check.
     * @param role     The minimum role level required (e.g., "ADMIN", "OPERATOR", "USER").
     *                 The user must have this role or higher in an organization that owns the credential.
     * @return {@code true} if either:
     * - The user is a superuser
     * - The RSU credential exists and belongs to at least one organization where
     * the user has the specified role or higher
     * {@code false} otherwise, including when authentication is invalid
     */
    public boolean hasRsuCredential(String nickname, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        return rsuCredentialRepository.existsByNicknameAndOrganizations(nickname, getQualifiedOrgList(username, role));
    }

    /**
     * Checks if an SNMP credential exists in any organization where the authenticated user
     * has the specified role or higher.
     *
     * @param nickname The unique identifier or nickname of the SNMP credential to check.
     * @param role     The minimum role level required (e.g., "ADMIN", "OPERATOR", "USER").
     *                 The user must have this role or higher in an organization that owns the credential.
     * @return {@code true} if either:
     * - The user is a superuser
     * - The SNMP credential exists and belongs to at least one organization where
     * the user has the specified role or higher
     * {@code false} otherwise, including when authentication is invalid
     */
    public boolean hasSnmpCredential(String nickname, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        String username = getUsername(auth);

        return snmpCredentialRepository.existsByNicknameAndOrganizations(nickname, getQualifiedOrgList(username, role));
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