package us.dot.its.jpo.ode.api.models.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CvManagerAuthToken Tests")
class CvManagerAuthTokenTest {

    private Jwt createMockJwt(Map<String, Object> cvmanagerData) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .claim("sub", "test-user-id")
                .claim("preferred_username", "testuser")
                .claim("cvmanager_data", cvmanagerData)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private Map<String, Object> createCvManagerData(String superUser, List<Map<String, String>> organizations) {
        Map<String, Object> cvmanagerData = new HashMap<>();
        cvmanagerData.put("super_user", superUser);
        cvmanagerData.put("organizations", organizations);
        cvmanagerData.put("user_created_timestamp", System.currentTimeMillis());
        return cvmanagerData;
    }

    private List<Map<String, String>> createOrganizations(String... orgRolePairs) {
        List<Map<String, String>> organizations = new ArrayList<>();
        for (int i = 0; i < orgRolePairs.length; i += 2) {
            Map<String, String> org = new HashMap<>();
            org.put("org", orgRolePairs[i]);
            org.put("role", orgRolePairs[i + 1]);
            organizations.add(org);
        }
        return organizations;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create token with super user flag set to true")
        void shouldCreateTokenWithSuperUserTrue() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("1", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // Act
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, authorities, "testuser");

            // Assert
            assertTrue(token.isSuperUser());
        }

        @Test
        @DisplayName("Should create token with super user flag set to false")
        void shouldCreateTokenWithSuperUserFalse() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // Act
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, authorities, "testuser");

            // Assert
            assertFalse(token.isSuperUser());
        }

        @Test
        @DisplayName("Should parse multiple organizations correctly")
        void shouldParseMultipleOrganizations() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations(
                    "CDOT", "admin",
                    "WYDOT", "operator",
                    "VDOT", "user");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // Act
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, authorities, "testuser");

            // Assert
            assertEquals(3, token.getAllOrgs().size());
            assertTrue(token.hasRoleInOrg("CDOT", "admin"));
            assertTrue(token.hasRoleInOrg("WYDOT", "operator"));
            assertTrue(token.hasRoleInOrg("VDOT", "user"));
        }
    }

    @Nested
    @DisplayName("isSuperUser Tests")
    class IsSuperUserTests {

        @Test
        @DisplayName("Should return true when super_user is '1'")
        void shouldReturnTrueWhenSuperUserIs1() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("1", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act & Assert
            assertTrue(token.isSuperUser());
        }

        @Test
        @DisplayName("Should return false when super_user is '0'")
        void shouldReturnFalseWhenSuperUserIs0() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act & Assert
            assertFalse(token.isSuperUser());
        }

        @Test
        @DisplayName("Should return false when super_user is null")
        void shouldReturnFalseWhenSuperUserIsNull() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData(null, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act & Assert
            assertFalse(token.isSuperUser());
        }

        @Test
        @DisplayName("Should return false when super_user is any other string")
        void shouldReturnFalseWhenSuperUserIsOtherString() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("true", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act & Assert
            assertFalse(token.isSuperUser());
        }
    }

    @Nested
    @DisplayName("hasRoleInOrg Tests")
    class HasRoleInOrgTests {

        private CvManagerAuthToken token;

        @BeforeEach
        void setUp() {
            List<Map<String, String>> orgs = createOrganizations(
                    "CDOT", "admin",
                    "WYDOT", "operator",
                    "VDOT", "user");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");
        }

        @Test
        @DisplayName("Should return true when user has exact role in org")
        void shouldReturnTrueWhenUserHasExactRole() {
            assertTrue(token.hasRoleInOrg("CDOT", "admin"));
            assertTrue(token.hasRoleInOrg("WYDOT", "operator"));
            assertTrue(token.hasRoleInOrg("VDOT", "user"));
        }

        @Test
        @DisplayName("Should return false when user has different role in org")
        void shouldReturnFalseWhenUserHasDifferentRole() {
            assertFalse(token.hasRoleInOrg("CDOT", "operator"));
            assertFalse(token.hasRoleInOrg("WYDOT", "admin"));
            assertFalse(token.hasRoleInOrg("VDOT", "operator"));
        }

        @Test
        @DisplayName("Should return false when org does not exist")
        void shouldReturnFalseWhenOrgDoesNotExist() {
            assertFalse(token.hasRoleInOrg("NONEXISTENT", "admin"));
        }

        @Test
        @DisplayName("Should be case-insensitive for role comparison")
        void shouldBeCaseInsensitiveForRole() {
            assertTrue(token.hasRoleInOrg("CDOT", "ADMIN"));
            assertTrue(token.hasRoleInOrg("CDOT", "Admin"));
            assertTrue(token.hasRoleInOrg("CDOT", "aDmIn"));
        }
    }

    @Nested
    @DisplayName("getAllOrgs Tests")
    class GetAllOrgsTests {

        @Test
        @DisplayName("Should return all organization names")
        void shouldReturnAllOrganizationNames() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations(
                    "CDOT", "admin",
                    "WYDOT", "operator",
                    "VDOT", "user");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            Set<String> allOrgs = token.getAllOrgs();

            // Assert
            assertEquals(3, allOrgs.size());
            assertTrue(allOrgs.contains("CDOT"));
            assertTrue(allOrgs.contains("WYDOT"));
            assertTrue(allOrgs.contains("VDOT"));
        }

        @Test
        @DisplayName("Should return empty set when user has no organizations")
        void shouldReturnEmptySetWhenNoOrganizations() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations();
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            Set<String> allOrgs = token.getAllOrgs();

            // Assert
            assertTrue(allOrgs.isEmpty());
        }
    }

    @Nested
    @DisplayName("getQualifiedOrgList Tests")
    class GetQualifiedOrgListTests {

        private CvManagerAuthToken token;

        @BeforeEach
        void setUp() {
            List<Map<String, String>> orgs = createOrganizations(
                    "CDOT", "admin",
                    "WYDOT", "operator",
                    "VDOT", "user",
                    "TXDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");
        }

        @Test
        @DisplayName("Should return all orgs when required role is 'user'")
        void shouldReturnAllOrgsForUserRole() {
            // Act
            List<String> qualifiedOrgs = token.getQualifiedOrgList("user");

            // Assert
            assertEquals(4, qualifiedOrgs.size());
            assertTrue(qualifiedOrgs.containsAll(List.of("CDOT", "WYDOT", "VDOT", "TXDOT")));
        }

        @Test
        @DisplayName("Should return orgs with operator or higher when required role is 'operator'")
        void shouldReturnOperatorAndAboveOrgs() {
            // Act
            List<String> qualifiedOrgs = token.getQualifiedOrgList("operator");

            // Assert
            assertEquals(3, qualifiedOrgs.size());
            assertTrue(qualifiedOrgs.containsAll(List.of("CDOT", "WYDOT", "TXDOT")));
            assertFalse(qualifiedOrgs.contains("VDOT"));
        }

        @Test
        @DisplayName("Should return only admin orgs when required role is 'admin'")
        void shouldReturnOnlyAdminOrgs() {
            // Act
            List<String> qualifiedOrgs = token.getQualifiedOrgList("admin");

            // Assert
            assertEquals(2, qualifiedOrgs.size());
            assertTrue(qualifiedOrgs.containsAll(List.of("CDOT", "TXDOT")));
            assertFalse(qualifiedOrgs.contains("WYDOT"));
            assertFalse(qualifiedOrgs.contains("VDOT"));
        }

        @Test
        @DisplayName("Should return empty list when no orgs meet the requirement")
        void shouldReturnEmptyListWhenNoOrgsMeetRequirement() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "user");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken tokenWithOnlyUser = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            List<String> qualifiedOrgs = tokenWithOnlyUser.getQualifiedOrgList("admin");

            // Assert
            assertTrue(qualifiedOrgs.isEmpty());
        }
    }

    @Nested
    @DisplayName("findRoleInOrg Tests")
    class FindRoleInOrgTests {

        private CvManagerAuthToken token;

        @BeforeEach
        void setUp() {
            List<Map<String, String>> orgs = createOrganizations(
                    "CDOT", "admin",
                    "WYDOT", "operator",
                    "VDOT", "user");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");
        }

        @Test
        @DisplayName("Should return role when org exists")
        void shouldReturnRoleWhenOrgExists() {
            // Act & Assert
            assertEquals(Optional.of("admin"), token.findRoleInOrg("CDOT"));
            assertEquals(Optional.of("operator"), token.findRoleInOrg("WYDOT"));
            assertEquals(Optional.of("user"), token.findRoleInOrg("VDOT"));
        }

        @Test
        @DisplayName("Should return empty when org does not exist")
        void shouldReturnEmptyWhenOrgDoesNotExist() {
            // Act
            Optional<String> role = token.findRoleInOrg("NONEXISTENT");

            // Assert
            assertTrue(role.isEmpty());
        }

        @Test
        @DisplayName("Should be case-insensitive for org name")
        void shouldBeCaseInsensitiveForOrgName() {
            // Act & Assert
            assertEquals(Optional.of("admin"), token.findRoleInOrg("cdot"));
            assertEquals(Optional.of("admin"), token.findRoleInOrg("CdOt"));
            assertEquals(Optional.of("operator"), token.findRoleInOrg("wydot"));
        }

        @Test
        @DisplayName("Should return empty when orgName is null")
        void shouldReturnEmptyWhenOrgNameIsNull() {
            // Act
            Optional<String> role = token.findRoleInOrg(null);

            // Assert
            assertTrue(role.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty organizations list")
        void shouldHandleEmptyOrganizationsList() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations();
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Assert
            assertTrue(token.getAllOrgs().isEmpty());
            assertTrue(token.getQualifiedOrgList("user").isEmpty());
            assertTrue(token.findRoleInOrg("CDOT").isEmpty());
            assertFalse(token.hasRoleInOrg("CDOT", "admin"));
        }

        @Test
        @DisplayName("Should handle single organization")
        void shouldHandleSingleOrganization() {
            // Arrange
            List<Map<String, String>> orgs = createOrganizations("CDOT", "admin");
            Map<String, Object> cvmanagerData = createCvManagerData("0", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Assert
            assertEquals(1, token.getAllOrgs().size());
            assertTrue(token.hasRoleInOrg("CDOT", "admin"));
            assertEquals(List.of("CDOT"), token.getQualifiedOrgList("admin"));
        }
    }
}
