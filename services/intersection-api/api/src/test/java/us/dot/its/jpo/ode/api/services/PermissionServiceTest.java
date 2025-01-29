package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PermissionServiceTest {

    @Mock
    private PostgresService postgresService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testHasIntersection_ValidAuth_AllowedIntersection() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        List<Integer> allowedIntersections = Arrays.asList(1, 2, 3);
        when(postgresService.getAllowedIntersectionIdsByEmail("user@example.com")).thenReturn(allowedIntersections);

        boolean result = permissionService.hasIntersection(2, "USER");

        assertTrue(result);
    }

    @Test
    public void testHasIntersection_ValidAuth_NotAllowedIntersection() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        List<Integer> allowedIntersections = Arrays.asList(1, 2, 3);
        when(postgresService.getAllowedIntersectionIdsByEmail("user@example.com")).thenReturn(allowedIntersections);

        boolean result = permissionService.hasIntersection(4, "USER");

        assertFalse(result);
    }

    @Test
    public void testHasIntersection_InvalidAuth() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = permissionService.hasIntersection(1, "USER");

        assertFalse(result);
    }

    @Test
    public void testHasRSU_ValidAuth_AllowedRSU() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        List<String> allowedRSUs = Arrays.asList("192.168.1.1", "192.168.1.2");
        when(postgresService.getAllowedRsuIpByEmail("user@example.com")).thenReturn(allowedRSUs);

        boolean result = permissionService.hasRSU("192.168.1.1", "USER");

        assertTrue(result);
    }

    @Test
    public void testHasRSU_ValidAuth_NotAllowedRSU() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        List<String> allowedRSUs = Arrays.asList("192.168.1.1", "192.168.1.2");
        when(postgresService.getAllowedRsuIpByEmail("user@example.com")).thenReturn(allowedRSUs);

        boolean result = permissionService.hasRSU("192.168.1.3", "USER");

        assertFalse(result);
    }

    @Test
    public void testHasRSU_InvalidAuth() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = permissionService.hasRSU("192.168.1.1", "USER");

        assertFalse(result);
    }
}