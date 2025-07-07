package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PermissionServiceTest {

    @Mock
    private PostgresService postgresService;

    @Mock
    private JwtAuthenticationToken authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Jwt jwtToken;

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        when(authentication.getToken()).thenReturn(jwtToken);
        when(jwtToken.getClaimAsString("preferred_username")).thenReturn("user@example.com");
        permissionService = spy(permissionService);
    }

    @Test
    public void testIsSuperUserWhenAuthIsInvalid() {
        when(permissionService.isAuthValid(authentication)).thenReturn(false);

        boolean result = permissionService.isSuperUser();

        assertFalse(result);
    }

    @Test
    public void testIsSuperUserWhenUserIsNull() {
        when(permissionService.isAuthValid(authentication)).thenReturn(true);
        // when(permissionService.getUsername(authentication)).thenReturn("testUser");
        when(postgresService.findUser("testUser")).thenReturn(null);

        boolean result = permissionService.isSuperUser();

        assertFalse(result);
    }

    @Test
    public void testIsSuperUserWhenUserIsNotSuperUser() {
        Users user = new Users();
        user.setSuper_user(false);

        when(permissionService.isAuthValid(authentication)).thenReturn(true);
        // when(permissionService.getUsername(authentication)).thenReturn("testUser");
        when(postgresService.findUser("testUser")).thenReturn(user);

        boolean result = permissionService.isSuperUser();

        assertFalse(result);
    }

    @Test
    public void testIsSuperUserWhenUserIsSuperUser() {

        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(securityContext.getAuthentication()))
                    .thenReturn("testUser");
            Users user = new Users();
            user.setSuper_user(true);

            when(permissionService.isAuthValid(authentication)).thenReturn(true);
            when(postgresService.findUser("testUser")).thenReturn(user);

            boolean result = permissionService.isSuperUser();

            assertTrue(result);
        }
    }

    @Test
    public void testHasIntersection_ValidAuth_AllowedIntersection() {
        List<String> organizations = Arrays.asList("org1");
        when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
        when(postgresService.checkIntersectionWithOrg("2", organizations)).thenReturn(true);

        boolean result = permissionService.hasIntersection(2, "USER");

        assertTrue(result);
    }

    @Test
    public void testHasIntersection_ValidAuth_AllowedIntersectionWithOrg() {

        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn("org1");

            List<String> organizations = Arrays.asList("org1");
            when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
            when(postgresService.checkIntersectionWithOrg(eq("2"), anyList())).thenReturn(true);

            boolean result = permissionService.hasIntersection(2, "USER");

            assertTrue(result);
        }
    }

    @Test
    public void testHasIntersection_ValidAuth_NotAllowedOrg() {

        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn("TestOrg");

            List<String> organizations = Arrays.asList("org1");
            when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
            when(postgresService.checkIntersectionWithOrg(eq("2"), anyList())).thenReturn(false);

            boolean result = permissionService.hasIntersection(2, "USER");

            assertFalse(result);
        }
    }

    @Test
    public void testHasIntersection_ValidAuth_AllowedSuperUser() {
        when(permissionService.isSuperUser()).thenReturn(true);

        boolean result = permissionService.hasIntersection(2, "USER");

        assertTrue(result);
    }

    @Test
    public void testHasIntersection_ValidAuth_NotAllowedIntersection() {
        List<String> organizations = Arrays.asList("org1");
        when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
        when(postgresService.checkIntersectionWithOrg("2", organizations)).thenReturn(false);

        boolean result = permissionService.hasIntersection(4, "USER");

        assertFalse(result);
    }

    @Test
    public void testHasIntersection_InvalidAuth() {
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = permissionService.hasIntersection(1, "USER");

        assertFalse(result);
    }

    @Test
    public void testHasIntersection_DefaultIntersectionId() {
        when(authentication.isAuthenticated()).thenReturn(true);

        assertTrue(permissionService.hasIntersection(-1, "USER"));
        assertTrue(permissionService.hasIntersection(null, "USER"));
    }

    @Test
    public void testHasRSU_ValidAuth_AllowedRSU() {
        List<String> organizations = Arrays.asList("org1");
        when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
        when(postgresService.checkRsuWithOrg("192.168.1.1", organizations)).thenReturn(true);

        boolean result = permissionService.hasRSU("192.168.1.1", "USER");

        assertTrue(result);
    }

    @Test
    public void testHasRSU_ValidAuth_AllowedIntersectionWithOrg() {

        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn("org1");

            List<String> organizations = Arrays.asList("org1");
            when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
            when(postgresService.checkRsuWithOrg("192.168.1.1", organizations)).thenReturn(true);

            boolean result = permissionService.hasRSU("192.168.1.1", "USER");

            assertTrue(result);
        }
    }

    @Test
    public void testHasRSU_ValidAuth_NotAllowedOrg() {

        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn("TestOrg");

            List<String> organizations = Arrays.asList("org1");
            when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
            when(postgresService.checkRsuWithOrg("192.168.1.1", organizations)).thenReturn(false);

            boolean result = permissionService.hasRSU("192.168.1.1", "USER");

            assertFalse(result);
        }
    }

    @Test
    public void testHasRSU_ValidAuth_AllowedSuperUser() {
        when(permissionService.isSuperUser()).thenReturn(true);

        boolean result = permissionService.hasRSU("192.168.1.1", "USER");

        assertTrue(result);
    }

    @Test
    public void testHasRSU_ValidAuth_NotAllowedRSU() {
        List<String> organizations = Arrays.asList("org1");
        when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
        when(postgresService.checkRsuWithOrg("192.168.1.1", organizations)).thenReturn(false);

        boolean result = permissionService.hasRSU("192.168.1.1", "USER");

        assertFalse(result);
    }

    @Test
    public void testHasRSU_InvalidAuth() {
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = permissionService.hasRSU("192.168.1.1", "USER");

        assertFalse(result);
    }

    @Test
    void testHasRoleWhenAuthIsInvalid() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(permissionService.isAuthValid(authentication)).thenReturn(false);

        // Act
        boolean result = permissionService.hasRole("admin");

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasRoleWhenUserIsSuperUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(permissionService.isAuthValid(authentication)).thenReturn(true);
        when(permissionService.isSuperUser()).thenReturn(true);

        // Act
        boolean result = permissionService.hasRole("admin");

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRoleWhenOrganizationIsProvidedAndRoleMatches() {
        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(authentication)).thenReturn("testUser");
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn("TestOrg");
            mockedStatic.when(() -> PermissionService.checkRoleAbove("admin", "admin")).thenReturn(true);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(permissionService.isAuthValid(authentication)).thenReturn(true);
            when(permissionService.isSuperUser()).thenReturn(false);
            when(postgresService.getUserRoleInOrg("testUser", "TestOrg")).thenReturn("admin");

            boolean result = permissionService.hasRole("admin");

            assertTrue(result);
        }
    }

    @Test
    void testHasRoleWhenOrganizationIsProvidedAndRoleDoesNotMatch() {
        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(authentication)).thenReturn("testUser");
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn("TestOrg");
            mockedStatic.when(() -> PermissionService.checkRoleAbove("admin", "admin")).thenReturn(false);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(permissionService.isAuthValid(authentication)).thenReturn(true);
            when(permissionService.isSuperUser()).thenReturn(false);
            when(postgresService.getUserRoleInOrg("testUser", "TestOrg")).thenReturn("user");

            boolean result = permissionService.hasRole("admin");

            assertFalse(result);
        }
    }

    @Test
    void testHasRoleWhenNoOrganizationAndQualifiedOrgListIsNotEmpty() {
        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(authentication)).thenReturn("testUser");
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn(null);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(permissionService.isAuthValid(authentication)).thenReturn(true);
            when(permissionService.isSuperUser()).thenReturn(false);
            when(postgresService.getQualifiedOrgList("testUser", "admin"))
                    .thenReturn(Collections.singletonList("TestOrg"));

            boolean result = permissionService.hasRole("admin");

            assertTrue(result);
        }
    }

    @Test
    void testHasRoleWhenNoOrganizationAndQualifiedOrgListIsEmpty() {
        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(authentication)).thenReturn("testUser");
            mockedStatic.when(PermissionService::getOrganizationFromHeader).thenReturn(null);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(permissionService.isAuthValid(authentication)).thenReturn(true);
            when(permissionService.isSuperUser()).thenReturn(false);
            when(postgresService.getQualifiedOrgList("testUser", "admin")).thenReturn(Collections.emptyList());

            // Act
            boolean result = permissionService.hasRole("admin");

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void testCheckRoleAboveAdminUser() {
        // Check all combinations of requires and user roles
        assertFalse(PermissionService.checkRoleAbove(null, "user"));
        assertTrue(PermissionService.checkRoleAbove("user", "user"));
        assertTrue(PermissionService.checkRoleAbove("operator", "user"));
        assertTrue(PermissionService.checkRoleAbove("admin", "user"));
        assertFalse(PermissionService.checkRoleAbove("user", "operator"));
        assertTrue(PermissionService.checkRoleAbove("operator", "operator"));
        assertTrue(PermissionService.checkRoleAbove("admin", "operator"));
        assertFalse(PermissionService.checkRoleAbove("user", "admin"));
        assertFalse(PermissionService.checkRoleAbove("operator", "admin"));
        assertTrue(PermissionService.checkRoleAbove("admin", "admin"));
    }
}