package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void testHasRSU_ValidAuth_AllowedRSU() {
        List<String> organizations = Arrays.asList("org1");
        when(postgresService.getQualifiedOrgList("user@example.com", "USER")).thenReturn(organizations);
        when(postgresService.checkRsuWithOrg("192.168.1.1", organizations)).thenReturn(true);

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
}