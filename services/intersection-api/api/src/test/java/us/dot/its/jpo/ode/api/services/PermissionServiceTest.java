package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RoleRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository.UserOrgRoleProjection;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private IntersectionRepository intersectionRepository;

    @Mock
    private RsuRepository rsuRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PermissionService permissionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private JwtAuthenticationToken createAuthenticatedToken(String email) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", email)
                .build();
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
        token.setAuthenticated(true);
        return token;
    }

    private void setupSecurityContext(JwtAuthenticationToken token) {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(token);
    }

    // ==================== checkRoleAbove Tests ====================

    @Test
    void testCheckRoleAbove_AdminAboveOperator() {
        assertTrue(PermissionService.checkRoleAbove("ADMIN", "OPERATOR"));
    }

    @Test
    void testCheckRoleAbove_AdminAboveUser() {
        assertTrue(PermissionService.checkRoleAbove("ADMIN", "USER"));
    }

    @Test
    void testCheckRoleAbove_OperatorAboveUser() {
        assertTrue(PermissionService.checkRoleAbove("OPERATOR", "USER"));
    }

    @Test
    void testCheckRoleAbove_SameRole() {
        assertTrue(PermissionService.checkRoleAbove("ADMIN", "ADMIN"));
        assertTrue(PermissionService.checkRoleAbove("OPERATOR", "OPERATOR"));
        assertTrue(PermissionService.checkRoleAbove("USER", "USER"));
    }

    @Test
    void testCheckRoleAbove_UserNotAboveOperator() {
        assertFalse(PermissionService.checkRoleAbove("USER", "OPERATOR"));
    }

    @Test
    void testCheckRoleAbove_OperatorNotAboveAdmin() {
        assertFalse(PermissionService.checkRoleAbove("OPERATOR", "ADMIN"));
    }

    @Test
    void testCheckRoleAbove_NullRole() {
        assertFalse(PermissionService.checkRoleAbove(null, "ADMIN"));
    }

    @Test
    void testCheckRoleAbove_CaseInsensitive() {
        assertTrue(PermissionService.checkRoleAbove("admin", "user"));
        assertTrue(PermissionService.checkRoleAbove("Admin", "User"));
    }

    // ==================== getAllowedIntersectionIds Tests ====================

    @Test
    void testGetAllowedIntersectionIdsByEmail() {
        when(intersectionRepository.findAllowedIntersectionIdsByEmail("test@example.com"))
                .thenReturn(List.of("123", "456", "789"));

        List<Integer> result = permissionService.getAllowedIntersectionIdsByEmail("test@example.com");

        assertEquals(List.of(123, 456, 789), result);
        verify(intersectionRepository).findAllowedIntersectionIdsByEmail("test@example.com");
    }

    @Test
    void testGetAllowedIntersectionIdsByOrganization() {
        when(intersectionRepository.findIntersectionsByOrganization("TestOrg"))
                .thenReturn(List.of("111", "222"));

        List<Integer> result = permissionService.getAllowedIntersectionIdsByOrganization("TestOrg");

        assertEquals(List.of(111, 222), result);
        verify(intersectionRepository).findIntersectionsByOrganization("TestOrg");
    }

    // ==================== isSuperUser Tests ====================

    @Test
    void testIsSuperUser_WhenUserIsSuperUser() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User superUser = new User();
        superUser.setSuperUser(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(superUser);

        assertTrue(permissionService.isSuperUser());
    }

    @Test
    void testIsSuperUser_WhenUserIsNotSuperUser() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);

        assertFalse(permissionService.isSuperUser());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testIsSuperUser_WhenNotAuthenticated() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        token.setAuthenticated(false);
        setupSecurityContext(token);

        assertFalse(permissionService.isSuperUser());
        verify(userRepository, never()).findByEmail(anyString());
    }

    // ==================== hasRole Tests ====================

    @Test
    void testHasRole_SuperUserAlwaysHasRole() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User superUser = new User();
        superUser.setSuperUser(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(superUser);

        assertTrue(permissionService.hasRole("ADMIN"));
        assertTrue(permissionService.hasRole("OPERATOR"));
        assertTrue(permissionService.hasRole("USER"));
    }

    @Test
    void testHasRole_WithOrganizationHeader_HasSufficientRole() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(roleRepository.findUserRoleInOrg("test@example.com", "TestOrg"))
                .thenReturn(Optional.of("ADMIN"));

        assertTrue(permissionService.hasRole("OPERATOR"));
    }

    @Test
    void testHasRole_WithOrganizationHeader_InsufficientRole() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(roleRepository.findUserRoleInOrg("test@example.com", "TestOrg"))
                .thenReturn(Optional.of("USER"));

        assertFalse(permissionService.hasRole("ADMIN"));
        verify(userRepository).findByEmail("test@example.com");
        verify(roleRepository).findUserRoleInOrg("test@example.com", "TestOrg");
    }

    @Test
    void testHasRole_WithoutOrganizationHeader_HasRoleInSomeOrg() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        UserOrgRoleProjection projection = mock(UserOrgRoleProjection.class);
        when(projection.getRoleName()).thenReturn("ADMIN");
        when(projection.getOrganizationName()).thenReturn("Org1");

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(userRepository.findUserOrgRoles("test@example.com")).thenReturn(List.of(projection));

        assertTrue(permissionService.hasRole("OPERATOR"));
    }

    @Test
    void testHasRole_WithoutOrganizationHeader_NoQualifiedOrgs() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        UserOrgRoleProjection projection = mock(UserOrgRoleProjection.class);
        when(projection.getRoleName()).thenReturn("USER");

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(userRepository.findUserOrgRoles("test@example.com")).thenReturn(List.of(projection));

        assertFalse(permissionService.hasRole("ADMIN"));
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).findUserOrgRoles("test@example.com");
    }

    @Test
    void testHasRole_NotAuthenticated() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        token.setAuthenticated(false);
        setupSecurityContext(token);

        assertFalse(permissionService.hasRole("USER"));
    }

    // ==================== hasIntersection Tests ====================

    @Test
    void testHasIntersection_NullIntersectionId() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        assertTrue(permissionService.hasIntersection(null, "USER"));
        verify(intersectionRepository, never()).existsByIdAndOrganizations(anyString(), anyList());
    }

    @Test
    void testHasIntersection_NegativeIntersectionId() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        assertTrue(permissionService.hasIntersection(-1, "USER"));
        verify(intersectionRepository, never()).existsByIdAndOrganizations(anyString(), anyList());
    }

    @Test
    void testHasIntersection_SuperUser() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User superUser = new User();
        superUser.setSuperUser(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(superUser);

        assertTrue(permissionService.hasIntersection(123, "USER"));
        verify(intersectionRepository, never()).existsByIdAndOrganizations(anyString(), anyList());
    }

    @Test
    void testHasIntersection_WithOrganizationHeader_HasAccess() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(intersectionRepository.existsByIdAndOrganizations("123", List.of("TestOrg")))
                .thenReturn(true);

        assertTrue(permissionService.hasIntersection(123, "USER"));
    }

    @Test
    void testHasIntersection_WithOrganizationHeader_NoAccess() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(intersectionRepository.existsByIdAndOrganizations("123", List.of("TestOrg")))
                .thenReturn(false);

        assertFalse(permissionService.hasIntersection(123, "USER"));
        verify(userRepository).findByEmail("test@example.com");
        verify(intersectionRepository).existsByIdAndOrganizations("123", List.of("TestOrg"));
    }

    @Test
    void testHasIntersection_WithoutOrganizationHeader_HasAccessInQualifiedOrg() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        UserOrgRoleProjection projection = mock(UserOrgRoleProjection.class);
        when(projection.getRoleName()).thenReturn("ADMIN");
        when(projection.getOrganizationName()).thenReturn("Org1");

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(userRepository.findUserOrgRoles("test@example.com")).thenReturn(List.of(projection));
        when(intersectionRepository.existsByIdAndOrganizations("123", List.of("Org1")))
                .thenReturn(true);

        assertTrue(permissionService.hasIntersection(123, "OPERATOR"));
    }

    // ==================== hasRSU Tests ====================

    @Test
    void testHasRSU_SuperUser() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User superUser = new User();
        superUser.setSuperUser(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(superUser);

        assertTrue(permissionService.hasRSU("192.168.1.1", "USER"));
        verify(rsuRepository, never()).existsByIpAndOrganizations(anyString(), anyList());
    }

    @Test
    void testHasRSU_WithOrganizationHeader_HasAccess() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(rsuRepository.existsByIpAndOrganizations("192.168.1.1", List.of("TestOrg")))
                .thenReturn(true);

        assertTrue(permissionService.hasRSU("192.168.1.1", "USER"));
    }

    @Test
    void testHasRSU_WithOrganizationHeader_NoAccess() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(rsuRepository.existsByIpAndOrganizations("192.168.1.1", List.of("TestOrg")))
                .thenReturn(false);

        assertFalse(permissionService.hasRSU("192.168.1.1", "USER"));
        verify(userRepository).findByEmail("test@example.com");
        verify(rsuRepository).existsByIpAndOrganizations("192.168.1.1", List.of("TestOrg"));
    }

    @Test
    void testHasRSU_WithoutOrganizationHeader_HasAccessInQualifiedOrg() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        setupSecurityContext(token);

        User regularUser = new User();
        regularUser.setSuperUser(false);

        UserOrgRoleProjection projection = mock(UserOrgRoleProjection.class);
        when(projection.getRoleName()).thenReturn("ADMIN");
        when(projection.getOrganizationName()).thenReturn("Org1");

        when(userRepository.findByEmail("test@example.com")).thenReturn(regularUser);
        when(userRepository.findUserOrgRoles("test@example.com")).thenReturn(List.of(projection));
        when(rsuRepository.existsByIpAndOrganizations("192.168.1.1", List.of("Org1")))
                .thenReturn(true);

        assertTrue(permissionService.hasRSU("192.168.1.1", "OPERATOR"));
    }

    // ==================== isAuthValid Tests ====================

    @Test
    void testIsAuthValid_ValidJwtAuthentication() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");

        assertTrue(permissionService.isAuthValid(token));
    }

    @Test
    void testIsAuthValid_NotAuthenticated() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        token.setAuthenticated(false);

        assertFalse(permissionService.isAuthValid(token));
    }

    @Test
    void testIsAuthValid_NotJwtAuthentication() {
        Authentication nonJwtAuth = mock(Authentication.class);
        when(nonJwtAuth.isAuthenticated()).thenReturn(true);

        assertFalse(permissionService.isAuthValid(nonJwtAuth));
    }

    // ==================== Static Utility Tests ====================

    @Test
    void testGetUsername() {
        JwtAuthenticationToken token = createAuthenticatedToken("test@example.com");
        String username = PermissionService.getUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void testGetOrganizationFromHeader_WithHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization", "TestOrg");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String organization = PermissionService.getOrganizationFromHeader();

        assertEquals("TestOrg", organization);
    }

    @Test
    void testGetOrganizationFromHeader_WithoutHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String organization = PermissionService.getOrganizationFromHeader();

        assertNull(organization);
    }

    @Test
    void testGetOrganizationFromHeader_NoRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        String organization = PermissionService.getOrganizationFromHeader();

        assertNull(organization);
    }
}