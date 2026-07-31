package us.dot.its.jpo.ode.api.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.RsuCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service("PermissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final IntersectionRepository intersectionRepository;
    private final RsuRepository rsuRepository;
    private final RsuCredentialRepository rsuCredentialRepository;
    private final SnmpCredentialRepository snmpCredentialRepository;
    private final UserRepository userRepository;

    public List<Integer> getAllowedIntersectionIdsByEmail(String email) {
        return intersectionRepository.findAllowedIntersectionIdsByEmail(email).stream().map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<Integer> getAllowedIntersectionIdsByOrganization(String email) {
        return intersectionRepository.findIntersectionsByOrganization(email).stream().map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * Gets the decoded token from the current security context.
     * Throws an IllegalStateException if the authentication context is invalid or
     * if the authentication token is not of type CvManagerAuthToken
     * 
     * @return CvManagerAuthToken containing user claims
     * @throws IllegalStateException if authentication is not valid
     */
    public CvManagerAuthToken getCvManagerAuthToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthValid(auth)) {
            throw new AccessDeniedException("Authentication token not provided");
        }

        if (auth instanceof CvManagerAuthToken authToken) {
            return authToken;
        }

        throw new AccessDeniedException("Invalid authentication token");
    }

    // Allow Connection if the user is a SuperUser
    public boolean isSuperUser() {
        CvManagerAuthToken authToken = getCvManagerAuthToken();
        return authToken.isSuperUser();
    }

    /**
     * Runs an organization-based permission check for the current user.
     * Invalid authentication returns false. Super users are always allowed.
     * If an Organization header was specified, then it will be verified to match
     * the qualified orgs.
     *
     * @param role              required minimum role
     * @param qualifiedOrgCheck check to perform against the user's qualified
     *                          organizations
     * @return true if the current user is authorized
     */
    private boolean checkQualifiedOrgs(String role, Predicate<List<String>> qualifiedOrgCheck) {
        CvManagerAuthToken authToken = getCvManagerAuthToken();

        if (authToken.isSuperUser()) {
            return true;
        }

        List<String> qualifiedOrgs = authToken.getQualifiedOrgList(UserRole.fromString(role));

        if (qualifiedOrgs == null || qualifiedOrgs.isEmpty()) {
            // No qualified organizations: deny access without hitting the repository
            return false;
        }

        // Verify that organization header matches qualified orgs, if specified
        String organization = getOrganizationFromHeader();
        if (organization != null) {
            if (!qualifiedOrgs.contains(organization)) {
                // If an organization is specified in the header, ensure it's in the qualified
                // orgs list
                return false;
            } else {
                return qualifiedOrgCheck.test(List.of(organization));
            }
        } else {
            return qualifiedOrgCheck.test(qualifiedOrgs);
        }
    }

    /**
     * Checks if the currently authenticated user has the specified role in at least
     * one organization. The method verifies the user's roles within the context of
     * organizations they belong to and determines if the role criteria is
     * satisfied.
     *
     * @param role the role to be checked against the user's permissions within
     *             organizations
     * @return true if the user possesses the specified role in at least one
     *         organization,
     *         or is a superuser; otherwise, false
     */
    public boolean hasRole(UserRole role) {
        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        String organization = getOrganizationFromHeader();

        if (organization != null) {
            Optional<UserRole> userRole = authToken.findRoleInOrg(organization);
            return userRole.map(roleValue -> roleValue.hasMinimumRole(role)).orElse(false);
        }
        return !authToken.getQualifiedOrgList(role).isEmpty();
    }

    public boolean hasRoleInOrgs(UserRole role, List<String> organizations) {
        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        List<String> qualifiedOrgs = authToken.getQualifiedOrgList(role);
        if (qualifiedOrgs == null || qualifiedOrgs.isEmpty()) {
            // No qualified organizations: deny access without hitting the repository
            return false;
        }
        return qualifiedOrgs.containsAll(organizations);
    }

    /**
     * Determines if the authenticated user has a specific role in the given
     * organization.
     *
     * @param organization the name of the organization to check the user's role in
     * @param role         the role to be validated within the specified
     *                     organization
     * @return true if the user has the specified role or a role above it in the
     *         organization,
     *         or if the user is a superuser; false otherwise
     */
    public boolean hasRoleInOrg(String organization, String role) {
        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        Optional<UserRole> userRole = authToken.findRoleInOrg(organization);
        return userRole.map(roleValue -> roleValue.hasMinimumRole(UserRole.fromString(role))).orElse(false);
    }

    // Allow Connection if the users organization controls the specified
    // intersection
    public boolean hasIntersection(Integer intersectionID, String role) {
        // Must be null check first, otherwise throws null pointer exception (if null)
        if (intersectionID == null || intersectionID == -1) {
            return true;
        }

        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        return checkQualifiedOrgs(role,
                qualifiedOrgs -> intersectionRepository.existsByIdAndOrganizations(
                        intersectionID.toString(),
                        qualifiedOrgs));
    }

    // Allow Connection if the users organization controls the specified RSU unit
    public boolean hasRsu(String rsuIP, String role) {
        InetAddress ipv4Address;
        try {
            ipv4Address = InetAddress.getByName(rsuIP);
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSU IP address: " + rsuIP, e);
        }

        return checkQualifiedOrgs(role,
                qualifiedOrgs -> rsuRepository.existsByIpAndOrganizations(
                        ipv4Address,
                        qualifiedOrgs));
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

        return checkQualifiedOrgs(role, qualifiedOrgs -> rsuRepository.findAllowedRsuIpsInOrganizations(qualifiedOrgs)
                .containsAll(ipv4Addresses));
    }

    // Allow Connection if the users organization(s) control the specified User
    public boolean hasUser(String email, String role) {
        if (email == null || email.isBlank()) {
            return false;
        }

        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        return checkQualifiedOrgs(role,
                qualifiedOrgs -> userRepository.existsByEmailAndOrganizations(email, qualifiedOrgs));
    }

    // Allow Connection if the users organization(s) control the specified Users
    public boolean hasUsers(List<String> emails, String role) {
        if (emails == null || emails.isEmpty()) {
            return true; // No emails to check, so allow connection
        }
        List<String> distinctEmails = emails.stream().distinct().toList();
        if (distinctEmails.isEmpty()) {
            return true; // No emails to check, so allow connection
        }

        return checkQualifiedOrgs(role,
                qualifiedOrgs -> userRepository.allUsersExistInOrganizations(distinctEmails, qualifiedOrgs,
                        distinctEmails.size()));
    }

    /**
     * Checks if an RSU credential exists in any organization where the
     * authenticated user
     * has the specified role or higher.
     *
     * @param nickname The unique identifier or nickname of the RSU credential to
     *                 check.
     * @param role     The minimum role level required (e.g., "ADMIN", "OPERATOR",
     *                 "USER").
     *                 The user must have this role or higher in an organization
     *                 that owns the credential.
     * @return {@code true} if either:
     *         - The user is a superuser
     *         - The RSU credential exists and belongs to at least one organization
     *         where
     *         the user has the specified role or higher
     *         {@code false} otherwise, including when authentication is invalid
     */
    public boolean hasRsuCredential(String nickname, String role) {
        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        return rsuCredentialRepository.existsByNicknameAndOrganizations(nickname,
                authToken.getQualifiedOrgList(UserRole.fromString(role)));
    }

    /**
     * Checks if an SNMP credential exists in any organization where the
     * authenticated user
     * has the specified role or higher.
     *
     * @param nickname The unique identifier or nickname of the SNMP credential to
     *                 check.
     * @param role     The minimum role level required (e.g., "ADMIN", "OPERATOR",
     *                 "USER").
     *                 The user must have this role or higher in an organization
     *                 that owns the credential.
     * @return {@code true} if either:
     *         - The user is a superuser
     *         - The SNMP credential exists and belongs to at least one organization
     *         where
     *         the user has the specified role or higher
     *         {@code false} otherwise, including when authentication is invalid
     */
    public boolean hasSnmpCredential(String nickname, String role) {
        CvManagerAuthToken authToken = getCvManagerAuthToken();
        if (authToken.isSuperUser()) {
            return true;
        }

        return snmpCredentialRepository.existsByNicknameAndOrganizations(nickname,
                authToken.getQualifiedOrgList(UserRole.fromString(role)));
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