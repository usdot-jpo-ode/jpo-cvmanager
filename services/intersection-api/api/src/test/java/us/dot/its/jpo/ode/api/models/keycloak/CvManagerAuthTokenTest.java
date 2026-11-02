package us.dot.its.jpo.ode.api.models.keycloak;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    private Jwt createMockJwtWithEmail(Map<String, Object> cvmanagerData, String email, String preferredUsername) {
        Jwt.Builder builder = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .claim("sub", "test-user-id")
                .claim("cvmanager_data", cvmanagerData)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));

        if (email != null) {
            builder.claim("email", email);
        }
        if (preferredUsername != null) {
            builder.claim("preferred_username", preferredUsername);
        }

        return builder.build();
    }

    private Map<String, Object> createCvManagerData(Object superUser, List<Map<String, Object>> organizations) {
        Map<String, Object> cvmanagerData = new HashMap<>();
        cvmanagerData.put("super_user", superUser);
        cvmanagerData.put("organizations", organizations);
        cvmanagerData.put("user_created_timestamp", System.currentTimeMillis());
        return cvmanagerData;
    }

    private Map<String, Object> createOrganizationMap(Integer orgId, String orgName, String orgEmail, String role) {
        Map<String, Object> org = new HashMap<>();
        org.put("org_id", orgId);
        org.put("org_name", orgName);
        org.put("org_email", orgEmail);
        org.put("role", role);
        return org;
    }

    private Organization createOrganization(Integer orgId, String orgName, String orgEmail) {
        Organization org = new Organization();
        org.setId(orgId);
        org.setName(orgName);
        org.setEmail(orgEmail);
        return org;
    }

    @Test
    @DisplayName("Should default to no orgs and non-super-user when cvmanager_data claim is missing")
    void shouldHandleMissingCvManagerDataClaim() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "test-user-id")
                .claim("email", "user@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

        assertFalse(token.isSuperUser());
        assertTrue(token.getAllOrgs().isEmpty());
        assertEquals("user@example.com", token.getEmail());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create token with super user flag set to true")
        void shouldCreateTokenWithSuperUserTrue() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(true, orgs);
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
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
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
            List<Map<String, Object>> orgs = List.of(
                    createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"),
                    createOrganizationMap(2, "AnotherOrg", "another@example.com", "operator"),
                    createOrganizationMap(3, "YetAnotherOrg", "yetanother@example.com", "user"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // Act
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, authorities, "testuser");

            // Assert
            assertEquals(3, token.getAllOrgs().size());
            assertTrue(token.hasRoleInOrg(1, "admin"));
            assertTrue(token.hasRoleInOrg(2, "operator"));
            assertTrue(token.hasRoleInOrg(3, "user"));
        }
    }

    @Nested
    @DisplayName("getEmail Tests")
    class GetEmailTests {

        @Test
        @DisplayName("Should return email from 'email' claim")
        void shouldReturnEmailFromEmailClaim() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, "user@example.com", null);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertEquals("user@example.com", email);
        }

        @Test
        @DisplayName("Should return email from 'preferred_username' when 'email' is null")
        void shouldReturnEmailFromPreferredUsername() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, null, "preferred@example.com");
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertEquals("preferred@example.com", email);
        }

        @Test
        @DisplayName("Should prefer 'email' claim over 'preferred_username'")
        void shouldPreferEmailClaimOverPreferredUsername() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, "email@example.com", "preferred@example.com");
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertEquals("email@example.com", email);
        }

        @Test
        @DisplayName("Should return null when no email claims are present")
        void shouldReturnNullWhenNoEmailClaims() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, null, null);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertNull(email);
        }

        @Test
        @DisplayName("Should return null when email claim is empty string")
        void shouldReturnNullWhenEmailIsEmpty() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, "", "preferred@example.com");
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertEquals("preferred@example.com", email);
        }

        @Test
        @DisplayName("Should return null when both email and preferred_username are empty")
        void shouldReturnNullWhenBothEmailsAreEmpty() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, "", "");
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertNull(email);
        }

        @ParameterizedTest(name = "Should handle email format: {0}")
        @ValueSource(strings = {
                "simple@example.com",
                "user.name@example.com",
                "user_name@example.com",
                "user-name@example.com",
                "user123@example.co.uk",
                "user+test@sub-domain.example.com",
                "123@example.com"
        })
        void shouldHandleEmailInVariousFormats(String testEmail) {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwtWithEmail(cvmanagerData, testEmail, null);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            String email = token.getEmail();

            // Assert
            assertEquals(testEmail, email);
        }
    }

    @Nested
    @DisplayName("Email Integration Tests")
    class EmailIntegrationTests {

        @Test
        @DisplayName("Should extract email correctly with full token structure")
        void shouldExtractEmailWithFullTokenStructure() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(
                    createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"),
                    createOrganizationMap(2, "WYDOT", "wydot@example.com", "user"));
            Map<String, Object> cvmanagerData = createCvManagerData(true, orgs);
            Jwt jwt = Jwt.withTokenValue("mock-token")
                    .header("alg", "RS256")
                    .header("typ", "JWT")
                    .claim("sub", "123e4567-e89b-12d3-a456-426614174000")
                    .claim("email", "admin@example.com")
                    .claim("preferred_username", "adminuser")
                    .claim("name", "Admin User")
                    .claim("given_name", "Admin")
                    .claim("family_name", "User")
                    .claim("cvmanager_data", cvmanagerData)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "adminuser");

            // Assert
            assertEquals("admin@example.com", token.getEmail());
            assertTrue(token.isSuperUser());
            assertEquals(2, token.getAllOrgs().size());
        }

        @Test
        @DisplayName("Should handle token with only preferred_username and no email")
        void shouldHandleTokenWithOnlyPreferredUsername() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "operator"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = Jwt.withTokenValue("mock-token")
                    .header("alg", "RS256")
                    .claim("sub", "user-123")
                    .claim("preferred_username", "operator@example.com")
                    .claim("cvmanager_data", cvmanagerData)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "operator");

            // Assert
            assertEquals("operator@example.com", token.getEmail());
            assertFalse(token.isSuperUser());
            assertTrue(token.hasRoleInOrg(1, "operator"));
        }
    }

    @Nested
    @DisplayName("isSuperUser Tests")
    class IsSuperUserTests {

        @Test
        @DisplayName("Should return true when super_user is boolean true")
        void shouldReturnTrueWhenSuperUserIsBooleanTrue() {
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(true, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            assertTrue(token.isSuperUser());
        }

        @Test
        @DisplayName("Should return false when super_user is boolean false")
        void shouldReturnFalseWhenSuperUserIsBooleanFalse() {
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            assertFalse(token.isSuperUser());
        }

        @Test
        @DisplayName("Should return false when super_user is the pre-boolean string '1'")
        void shouldReturnFalseWhenSuperUserIsString1() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData("1", orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act & Assert
            assertFalse(token.isSuperUser());
        }

        @Test
        @DisplayName("Should return false when super_user is the pre-boolean string '0'")
        void shouldReturnFalseWhenSuperUserIs0() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
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
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
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
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData("yes", orgs);
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
            List<Map<String, Object>> orgs = List.of(
                    createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"),
                    createOrganizationMap(2, "WYDOT", "wydot@example.com", "operator"),
                    createOrganizationMap(3, "VDOT", "vdot@example.com", "user"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");
        }

        @Test
        @DisplayName("Should return true when user has exact role in org")
        void shouldReturnTrueWhenUserHasExactRole() {
            assertTrue(token.hasRoleInOrg(1, "admin"));
            assertTrue(token.hasRoleInOrg(2, "operator"));
            assertTrue(token.hasRoleInOrg(3, "user"));
        }

        @Test
        @DisplayName("Should return false when user has different role in org")
        void shouldReturnFalseWhenUserHasDifferentRole() {
            assertFalse(token.hasRoleInOrg(1, "operator"));
            assertFalse(token.hasRoleInOrg(2, "admin"));
            assertFalse(token.hasRoleInOrg(3, "operator"));
        }

        @Test
        @DisplayName("Should return false when org does not exist")
        void shouldReturnFalseWhenOrgDoesNotExist() {
            assertFalse(token.hasRoleInOrg(999, "admin"));
        }

        @Test
        @DisplayName("Should be case-insensitive for role comparison")
        void shouldBeCaseInsensitiveForRole() {
            assertTrue(token.hasRoleInOrg(1, "admin"));
            assertTrue(token.hasRoleInOrg(1, "Admin"));
            assertTrue(token.hasRoleInOrg(1, "aDmIn"));
        }
    }

    @Nested
    @DisplayName("getAllOrgs Tests")
    class GetAllOrgsTests {

        @Test
        @DisplayName("Should return all organization names")
        void shouldReturnAllOrganizationNames() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(
                    createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"),
                    createOrganizationMap(2, "WYDOT", "wydot@example.com", "operator"),
                    createOrganizationMap(3, "VDOT", "vdot@example.com", "user"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            Set<Integer> allOrgIds = token.getAllOrgs().stream().map(o -> o.getId()).collect(Collectors.toSet());

            // Assert
            assertEquals(3, allOrgIds.size());
            assertTrue(allOrgIds.contains(1));
            assertTrue(allOrgIds.contains(2));
            assertTrue(allOrgIds.contains(3));
        }

        @Test
        @DisplayName("Should return empty set when user has no organizations")
        void shouldReturnEmptySetWhenNoOrganizations() {
            // Arrange
            List<Map<String, Object>> orgs = List.of();
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            Set<Organization> allOrgs = token.getAllOrgs();

            // Assert
            assertTrue(allOrgs.isEmpty());
        }
    }

    @Nested
    @DisplayName("getQualifiedOrgList Tests")
    class GetQualifiedOrgListTests {

        private CvManagerAuthToken token;
        private Organization cdotOrg;
        private Organization wydotOrg;
        private Organization vdotOrg;
        private Organization txdotOrg;

        @BeforeEach
        void setUp() {
            cdotOrg = createOrganization(1, "CDOT", "email1");
            wydotOrg = createOrganization(2, "WYDOT", "email2");
            vdotOrg = createOrganization(3, "VDOT", "email3");
            txdotOrg = createOrganization(4, "TXDOT", "email4");
            List<Map<String, Object>> tokenOrgs = List.of(
                    createOrganizationMap(1, "CDOT", "email1", "admin"),
                    createOrganizationMap(2, "WYDOT", "email2", "operator"),
                    createOrganizationMap(3, "VDOT", "email3", "user"),
                    createOrganizationMap(4, "TXDOT", "email4", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, tokenOrgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");
        }

        @Test
        @DisplayName("Should return all orgs when required role is 'user'")
        void shouldReturnAllOrgsForUserRole() {
            // Act
            List<Organization> qualifiedOrgs = token.getQualifiedOrgList(UserRole.USER);

            // Assert
            assertEquals(4, qualifiedOrgs.size());
            assertTrue(qualifiedOrgs.containsAll(List.of(cdotOrg, wydotOrg, vdotOrg, txdotOrg)));
        }

        @Test
        @DisplayName("Should return orgs with operator or higher when required role is 'operator'")
        void shouldReturnOperatorAndAboveOrgs() {
            // Act
            List<Organization> qualifiedOrgs = token.getQualifiedOrgList(UserRole.OPERATOR);

            // Assert
            assertEquals(3, qualifiedOrgs.size());
            assertTrue(qualifiedOrgs.containsAll(List.of(cdotOrg, wydotOrg, txdotOrg)));
            assertFalse(qualifiedOrgs.contains(vdotOrg));
        }

        @Test
        @DisplayName("Should return only admin orgs when required role is 'admin'")
        void shouldReturnOnlyAdminOrgs() {
            // Act
            List<Organization> qualifiedOrgs = token.getQualifiedOrgList(UserRole.ADMIN);

            // Assert
            assertEquals(2, qualifiedOrgs.size());
            assertTrue(qualifiedOrgs.containsAll(List.of(cdotOrg, txdotOrg)));
            assertFalse(qualifiedOrgs.contains(wydotOrg));
            assertFalse(qualifiedOrgs.contains(vdotOrg));
        }

        @Test
        @DisplayName("Should return empty list when no orgs meet the requirement")
        void shouldReturnEmptyListWhenNoOrgsMeetRequirement() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "email1", "user"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken tokenWithOnlyUser = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Act
            List<Organization> qualifiedOrgs = tokenWithOnlyUser.getQualifiedOrgList(UserRole.ADMIN);

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
            List<Map<String, Object>> orgs = List.of(
                    createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"),
                    createOrganizationMap(2, "WYDOT", "wydot@example.com", "operator"),
                    createOrganizationMap(3, "VDOT", "vdot@example.com", "user"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");
        }

        @Test
        @DisplayName("Should return role when org exists")
        void shouldReturnRoleWhenOrgExists() {
            // Act & Assert
            assertEquals(Optional.of(UserRole.ADMIN), token.findRoleInOrg("CDOT"));
            assertEquals(Optional.of(UserRole.OPERATOR), token.findRoleInOrg("WYDOT"));
            assertEquals(Optional.of(UserRole.USER), token.findRoleInOrg("VDOT"));
        }

        @Test
        @DisplayName("Should return empty when org does not exist")
        void shouldReturnEmptyWhenOrgDoesNotExist() {
            // Act
            Optional<UserRole> role = token.findRoleInOrg("NONEXISTENT");

            // Assert
            assertTrue(role.isEmpty());
        }

        @Test
        @DisplayName("Should be case-sensitive for org name")
        void shouldBeCaseSensitiveForOrgName() {
            // Act & Assert
            assertEquals(Optional.of(UserRole.ADMIN), token.findRoleInOrg("CDOT"));
            assertTrue(token.findRoleInOrg("cdot").isEmpty());
            assertTrue(token.findRoleInOrg("CdOt").isEmpty());
            assertEquals(Optional.of(UserRole.OPERATOR), token.findRoleInOrg("WYDOT"));
            assertTrue(token.findRoleInOrg("Wydot").isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty organizations list")
        void shouldHandleEmptyOrganizationsList() {
            // Arrange
            List<Map<String, Object>> orgs = List.of();
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Assert
            assertTrue(token.getAllOrgs().isEmpty());
            assertTrue(token.getQualifiedOrgList(UserRole.USER).isEmpty());
            assertTrue(token.findRoleInOrg("CDOT").isEmpty());
            assertFalse(token.hasRoleInOrg(1, "admin"));
        }

        @Test
        @DisplayName("Should handle single organization")
        void shouldHandleSingleOrganization() {
            // Arrange
            List<Map<String, Object>> orgs = List.of(createOrganizationMap(1, "CDOT", "cdot@example.com", "admin"));
            Map<String, Object> cvmanagerData = createCvManagerData(false, orgs);
            Jwt jwt = createMockJwt(cvmanagerData);
            CvManagerAuthToken token = new CvManagerAuthToken(jwt, Collections.emptyList(), "testuser");

            // Assert
            assertEquals(1, token.getAllOrgs().size());
            assertTrue(token.hasRoleInOrg(1, "admin"));
            assertEquals(List.of(createOrganization(1, "CDOT", "cdot@example.com")),
                    token.getQualifiedOrgList(UserRole.ADMIN));
        }
    }
}
