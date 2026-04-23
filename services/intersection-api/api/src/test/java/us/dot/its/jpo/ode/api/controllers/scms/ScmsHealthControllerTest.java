package us.dot.its.jpo.ode.api.controllers.scms;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;

import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjection;
import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjectionImpl;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.ScmsHealthService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ScmsHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScmsHealthService scmsHealthService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("Retrieved successfully")
    void testGetScmsStatus_SUCCESS() throws Exception {
        // Arrange
        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(
                InetAddress.getByName("10.0.0.1"), true, Instant.now());
        List<ScmsHealthRsuProjection> queryResults = List.of(projection);

        when(permissionService.isSuperUser()).thenReturn(false);
        when(permissionService.hasRoleInOrg("TestOrg", "USER")).thenReturn(true);
        when(scmsHealthService.getScmsStatuses(anyString())).thenReturn(queryResults);

        // Act & Assert
        mockMvc.perform(get("/devices/scms/status")
                        .header("Organization", "TestOrg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scmsHealthByIp['10.0.0.1'].health").value(true));

        verify(scmsHealthService).getScmsStatuses(anyString());
    }

    @Test
    @DisplayName("Retrieval fails when Organization header is missing")
    void testGetScmsStatus_FAILURE_OrganizationHeaderMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/devices/scms/status"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Retrieval succeeds when no results are returned")
    void testGetScmsStatus_SUCCESS_EmptyResults() throws Exception {
        // Arrange
        when(permissionService.isSuperUser()).thenReturn(false);
        when(permissionService.hasRoleInOrg("TestOrg", "USER")).thenReturn(true);
        when(scmsHealthService.getScmsStatuses(anyString())).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/devices/scms/status")
                        .header("Organization", "TestOrg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scmsHealthByIp").isMap())
                .andExpect(jsonPath("$.scmsHealthByIp.*").isEmpty());

        verify(scmsHealthService).getScmsStatuses(anyString());
    }

    @Test
    @DisplayName("Retrieval fails when Organization is not found")
    void testGetScmsStatus_FAILURE_OrganizationNotFound() throws Exception {
        // Arrange
        when(permissionService.isSuperUser()).thenReturn(false);
        when(permissionService.hasRoleInOrg("TestOrg", "USER")).thenReturn(true);
        when(scmsHealthService.getScmsStatuses(anyString())).thenThrow(new EntityNotFoundException("Organization not found"));

        // Act & Assert
        mockMvc.perform(get("/devices/scms/status")
                        .header("Organization", "TestOrg"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Retrieval succeeds when user is a super user")
    void testGetScmsStatus_SUCCESS_AsSuperUser() throws Exception {
        // Arrange - super user can access any organization
        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(
                InetAddress.getByName("10.0.0.1"), true, Instant.now());
        List<ScmsHealthRsuProjection> queryResults = List.of(projection);

        when(permissionService.isSuperUser()).thenReturn(true);
        when(scmsHealthService.getScmsStatuses(anyString())).thenReturn(queryResults);

        // Act & Assert
        mockMvc.perform(get("/devices/scms/status")
                        .header("Organization", "AnyOrg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scmsHealthByIp['10.0.0.1'].health").value(true));

        verify(scmsHealthService).getScmsStatuses(anyString());
        // hasRoleInOrg should NOT be called since isSuperUser returns true
        verify(permissionService, never()).hasRoleInOrg(anyString(), eq("USER"));
    }

    @Test
    @DisplayName("Retrieval fails when user is not authorized to access the requested organization")
    void testGetScmsStatus_FORBIDDEN_UserNotInOrganization() throws Exception {
        // Arrange - user does not have access to the requested organization
        when(permissionService.isSuperUser()).thenReturn(false);
        when(permissionService.hasRoleInOrg("UnauthorizedOrg", "USER")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/devices/scms/status")
                        .header("Organization", "UnauthorizedOrg"))
            .andExpect(status().isForbidden());

        // Service should NOT be called since authorization failed
        verify(scmsHealthService, never()).getScmsStatuses(anyString());
    }
}