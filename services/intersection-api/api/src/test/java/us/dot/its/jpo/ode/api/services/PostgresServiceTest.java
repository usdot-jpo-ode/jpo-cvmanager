package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PostgresServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<UserOrgRole> userOrgRoleQuery;

    @Mock
    private TypedQuery<Users> usersQuery;

    @Mock
    private TypedQuery<String> stringQuery;

    @InjectMocks
    private PostgresService postgresService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserOrgRoles() {
        // Arrange
        String email = "test@example.com";
        List<UserOrgRole> expectedRoles = Arrays.asList(
                new UserOrgRole("test@example.com", "Org1", "admin"),
                new UserOrgRole("test@example.com", "Org2", "user"));

        when(entityManager.createQuery(anyString(), eq(UserOrgRole.class))).thenReturn(userOrgRoleQuery);
        when(userOrgRoleQuery.setParameter("email", email)).thenReturn(userOrgRoleQuery);
        when(userOrgRoleQuery.getResultList()).thenReturn(expectedRoles);

        // Act
        List<UserOrgRole> result = postgresService.findUserOrgRoles(email);

        // Assert
        assertEquals(expectedRoles, result);
        verify(entityManager, times(1)).createQuery(anyString(), eq(UserOrgRole.class));
    }

    @Test
    void testIsUserRoleAboveRequired() {
        // Arrange
        String email = "test@example.com";
        String organization = "TestOrg";
        String requiredRole = "admin";

        List<UserOrgRole> userOrgRoles = Arrays.asList(
                new UserOrgRole(email, organization, "admin"),
                new UserOrgRole(email, organization, "user"));

        when(entityManager.createQuery(anyString(), eq(UserOrgRole.class))).thenReturn(userOrgRoleQuery);
        when(userOrgRoleQuery.setParameter("email", email)).thenReturn(userOrgRoleQuery);
        when(userOrgRoleQuery.getResultList()).thenReturn(userOrgRoles);

        // Act
        List<UserOrgRole> result = postgresService.isUserRoleAboveRequired(email, organization, requiredRole);

        // Assert
        assertEquals(userOrgRoles, result);
        verify(entityManager, times(1)).createQuery(anyString(), eq(UserOrgRole.class));
    }

    @Test
    void testFindUser() {
        // Arrange
        String email = "test@example.com";
        Users expectedUser = new Users();
        expectedUser.setEmail(email);

        when(entityManager.createQuery(anyString(), eq(Users.class))).thenReturn(usersQuery);
        when(usersQuery.setParameter("email", email)).thenReturn(usersQuery);
        when(usersQuery.setMaxResults(1)).thenReturn(usersQuery);
        when(usersQuery.getResultList()).thenReturn(Collections.singletonList(expectedUser));

        // Act
        Users result = postgresService.findUser(email);

        // Assert
        assertEquals(expectedUser, result);
        verify(entityManager, times(1)).createQuery(anyString(), eq(Users.class));
    }

    @Test
    void testFindUserWhenNoResults() {
        // Arrange
        String email = "test@example.com";

        when(entityManager.createQuery(anyString(), eq(Users.class))).thenReturn(usersQuery);
        when(usersQuery.setParameter("email", email)).thenReturn(usersQuery);
        when(usersQuery.setMaxResults(1)).thenReturn(usersQuery);
        when(usersQuery.getResultList()).thenReturn(Collections.emptyList());

        // Act
        Users result = postgresService.findUser(email);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetUserRoleInOrg() {
        // Arrange
        String email = "test@example.com";
        String organization = "Org1";
        String expectedRole = "admin";

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("email", email)).thenReturn(stringQuery);
        when(stringQuery.setParameter("organization", organization)).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(Collections.singletonList(expectedRole));

        // Act
        String result = postgresService.getUserRoleInOrg(email, organization);

        // Assert
        assertEquals(expectedRole, result);
    }

    @Test
    void testGetAllowedRsuIpByEmail() {
        // Arrange
        String email = "test@example.com";
        List<String> expectedIps = Arrays.asList("192.168.1.1", "192.168.1.2");

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("email", email)).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(expectedIps);

        // Act
        List<String> result = postgresService.getAllowedRsuIpByEmail(email);

        // Assert
        assertEquals(expectedIps, result);
    }

    @Test
    void testGetAllowedIntersectionIdsByEmail() {
        // Arrange
        String email = "test@example.com";
        List<String> intersectionNumbers = Arrays.asList("1", "2");
        List<Integer> expectedIds = Arrays.asList(1, 2);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("email", email)).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(intersectionNumbers);

        // Act
        List<Integer> result = postgresService.getAllowedIntersectionIdsByEmail(email);

        // Assert
        assertEquals(expectedIds, result);
    }

    @Test
    void testGetAllowedIntersectionIdsByOrganization() {
        // Arrange
        String organization = "TestOrg";
        List<String> intersectionNumbers = Arrays.asList("1", "2", "3");

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("orgName", organization)).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(intersectionNumbers);

        // Act
        List<Integer> result = postgresService.getAllowedIntersectionIdsByOrganization(organization);

        // Assert
        assertEquals(Arrays.asList(1, 2, 3), result);
        verify(entityManager, times(1)).createQuery(anyString(), eq(String.class));
    }

    @Test
    void testCheckRsuWithOrg() {
        // Arrange
        String rsuIp = "192.168.1.1";
        List<String> organizations = Arrays.asList("Org1", "Org2");

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("allowedOrgs", organizations)).thenReturn(stringQuery);
        when(stringQuery.setParameter("rsuIp", rsuIp)).thenReturn(stringQuery);
        when(stringQuery.setMaxResults(1)).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(Collections.singletonList(rsuIp));

        // Act
        boolean result = postgresService.checkRsuWithOrg(rsuIp, organizations);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCheckIntersectionWithOrg() {
        // Arrange
        String intersectionId = "1";
        List<String> organizations = Arrays.asList("Org1", "Org2");

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("allowedOrgs", organizations)).thenReturn(stringQuery);
        when(stringQuery.setParameter("intersectionId", intersectionId)).thenReturn(stringQuery);
        when(stringQuery.setMaxResults(1)).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(Collections.singletonList(intersectionId));

        // Act
        boolean result = postgresService.checkIntersectionWithOrg(intersectionId, organizations);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetQualifiedOrgList() {
        // Arrange
        String email = "test@example.com";
        String requiredRole = "admin";

        List<UserOrgRole> userOrgRoles = Arrays.asList(
                new UserOrgRole(email, "Org1", "admin"),
                new UserOrgRole(email, "Org2", "user"));

        when(entityManager.createQuery(anyString(), eq(UserOrgRole.class))).thenReturn(userOrgRoleQuery);
        when(userOrgRoleQuery.setParameter("email", email)).thenReturn(userOrgRoleQuery);
        when(userOrgRoleQuery.getResultList()).thenReturn(userOrgRoles);

        // Mock PermissionService.checkRoleAbove
        try (MockedStatic<PermissionService> mockedStatic = mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.checkRoleAbove("admin", requiredRole)).thenReturn(true);
            mockedStatic.when(() -> PermissionService.checkRoleAbove("user", requiredRole)).thenReturn(false);

            // Act
            List<String> result = postgresService.getQualifiedOrgList(email, requiredRole);

            // Assert
            assertEquals(Collections.singletonList("Org1"), result);
            verify(entityManager, times(1)).createQuery(anyString(), eq(UserOrgRole.class));
        }
    }
}