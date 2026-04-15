package us.dot.its.jpo.ode.api.controllers.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.admin.intersection.AllowedSelections;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionDto;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionListResponse;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionPatch;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionSingleResponse;
import us.dot.its.jpo.ode.api.models.admin.intersection.RefPt;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.services.AdminIntersectionService;
import us.dot.its.jpo.ode.api.services.PermissionService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Tests the full HTTP request/response cycle including:
 * <ul>
 * <li>Authorization: {@code @PreAuthorize} enforcement via the mocked
 * {@link PermissionService}</li>
 * <li>In-method org restriction enforcement (PATCH endpoint)</li>
 * <li>Bean Validation: {@code @NotNull}/{@code @NotBlank} constraints on
 * request body and params</li>
 * <li>Response shape: JSON field names, types, and values</li>
 * <li>Service delegation: verifies the correct service method is called with
 * the right arguments</li>
 * </ul>
 *
 * <p>
 * <b>Security note:</b>
 * {@link us.dot.its.jpo.ode.api.controllers.advice.GlobalExceptionHandler}
 * intercepts {@link org.springframework.security.access.AccessDeniedException}
 * (thrown by
 * {@code @PreAuthorize} when it evaluates to {@code false}) and returns HTTP
 * 403. As a result,
 * both unauthenticated requests and authenticated-but-unauthorized requests
 * consistently yield 403.
 *
 * <p>
 * <b>Validation ordering note:</b> {@code @RequestBody @Validated} constraints
 * are resolved
 * during Spring MVC argument binding — before the {@code @PreAuthorize} AOP
 * advice runs. Validation
 * failures therefore return 400 regardless of the caller's permissions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminIntersectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private AdminIntersectionService adminIntersectionService;

    @MockitoBean
    IntersectionRepository intersectionRepository;

    @MockitoBean
    RsuRepository rsuRepository;

    /**
     * Concrete-class mock; created fresh per test to avoid cross-test stubbing
     * bleed.
     */
    private CvManagerAuthToken authToken;

    private IntersectionListResponse sampleListResponse;
    private IntersectionSingleResponse sampleSingleResponse;
    private IntersectionPatch validPatch;

    @BeforeEach
    void setUp() {
        authToken = Mockito.mock(CvManagerAuthToken.class);

        RefPt refPt = new RefPt(39.7392, -104.9903);

        IntersectionDto sampleDto = new IntersectionDto(
                12109,
                refPt,
                null,
                "Main St & 1st Ave",
                "192.168.1.1",
                List.of("TestOrg"),
                List.of("10.0.0.1"));

        AllowedSelections allowedSelections = new AllowedSelections(
                List.of("TestOrg", "OtherOrg"),
                List.of("10.0.0.1", "10.0.0.2"));

        sampleListResponse = new IntersectionListResponse(List.of(sampleDto));
        sampleSingleResponse = new IntersectionSingleResponse(sampleDto, allowedSelections);

        validPatch = new IntersectionPatch(
                12109, 12109, refPt,
                null, "Main St & 1st Ave", null,
                List.of(), List.of(), List.of(), List.of());
    }

    @Nested
    @DisplayName("GET /admin/intersections — list all intersections")
    class GetAllIntersections {

        @Test
        @DisplayName("returns 403 when no permissions are granted (unauthenticated)")
        void noPermissions_returns403() throws Exception {
            // Spring Security filter runs before argument binding; unauthenticated → 403
            mockMvc.perform(get("/admin/intersections")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when Organization header is missing")
        void missingOrganizationHeader_returns400() throws Exception {
            // Argument binding runs before @PreAuthorize; missing required header → 400
            mockMvc.perform(get("/admin/intersections"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but neither isSuperUser nor hasRole('USER')")
        void authenticated_insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(false);

            mockMvc.perform(get("/admin/intersections")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with intersection list when isSuperUser returns true")
        void superUser_returns200WithIntersectionList() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            when(adminIntersectionService.getAllIntersections(eq("TestOrg")))
                    .thenReturn(sampleListResponse);

            mockMvc.perform(get("/admin/intersections")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intersection_data").isArray())
                    .andExpect(jsonPath("$.intersection_data[0].intersection_id").value(12109))
                    .andExpect(jsonPath("$.intersection_data[0].intersection_name").value("Main St & 1st Ave"))
                    .andExpect(jsonPath("$.intersection_data[0].organizations[0]").value("TestOrg"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 when hasRole('USER') returns true (non-superuser path)")
        void userWithRole_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(true);
            when(adminIntersectionService.getAllIntersections(eq("TestOrg")))
                    .thenReturn(sampleListResponse);

            mockMvc.perform(get("/admin/intersections")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intersection_data[0].intersection_id").value(12109));
        }

        @Test
        @WithMockUser
        @DisplayName("passes the Organization header value to the service")
        void organizationHeader_isForwardedToService() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            when(adminIntersectionService.getAllIntersections(eq("TestOrg")))
                    .thenReturn(sampleListResponse);

            mockMvc.perform(get("/admin/intersections")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isOk());

            verify(adminIntersectionService).getAllIntersections(eq("TestOrg"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when no intersections are found for the organization")
        void noAccessibleIntersections_returns404() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            doThrow(new EntityNotFoundException("No accessible intersections found for organization 'TestOrg'"))
                    .when(adminIntersectionService).getAllIntersections(any());

            mockMvc.perform(get("/admin/intersections")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /admin/intersections/{intersectionId} — single intersection")
    class GetSingleIntersection {

        @Test
        @DisplayName("returns 403 when no permissions are granted")
        void noPermissions_returns403() throws Exception {
            mockMvc.perform(get("/admin/intersections/12109"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but neither isSuperUser nor hasRole('USER')")
        void authenticated_insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(false);

            mockMvc.perform(get("/admin/intersections/12109"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with intersection_data and allowed_selections for super user")
        void superUser_returns200WithFullResponseShape() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.USER)).thenReturn(Collections.emptyList());
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(Collections.emptyList());
            when(adminIntersectionService.getIntersection(eq(12109))).thenReturn(sampleSingleResponse);

            mockMvc.perform(get("/admin/intersections/12109"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intersection_data.intersection_id").value(12109))
                    .andExpect(jsonPath("$.allowed_selections").exists())
                    .andExpect(jsonPath("$.allowed_selections.organizations").isArray())
                    .andExpect(jsonPath("$.allowed_selections.rsus").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when intersection is not found")
        void intersectionNotFound_returns404() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.USER)).thenReturn(Collections.emptyList());
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(Collections.emptyList());
            doThrow(new EntityNotFoundException("Intersection with id 99999 not found"))
                    .when(adminIntersectionService).getIntersection(99999);

            mockMvc.perform(get("/admin/intersections/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("non-superuser with USER role and intersection access returns 200")
        void organizationHeader_passedToServiceWithCorrectOrgLists() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("USER"))).thenReturn(true);
            when(adminIntersectionService.getIntersection(12109)).thenReturn(sampleSingleResponse);

            mockMvc.perform(get("/admin/intersections/12109")
                    .header("Organization", "TestOrg"))
                    .andExpect(status().isOk());

            verify(adminIntersectionService).getIntersection(eq(12109));
        }
    }

    @Nested
    @DisplayName("PATCH /admin/intersections — update intersection")
    class PatchIntersection {

        @Test
        @DisplayName("returns 403 when no permissions are granted")
        void noPermissions_returns403() throws Exception {
            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatch)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but hasRole('OPERATOR') returns false")
        void noOperatorRole_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(false);

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatch)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when operator role is granted but hasIntersection returns false")
        void operatorWithoutIntersectionAccess_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(false);

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatch)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when organizations_to_add contains an org outside the user's qualified orgs")
        void unqualifiedOrgInOrganizationsToAdd_returns403() throws Exception {
            IntersectionPatch patchWithUnqualifiedOrg = new IntersectionPatch(
                    12109, 12109, new RefPt(39.7392, -104.9903),
                    null, null, null,
                    List.of("UnqualifiedOrg"), List.of(), List.of(), List.of());

            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(List.of("TestOrg"));

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchWithUnqualifiedOrg)))
                    .andExpect(status().isForbidden());

            verify(adminIntersectionService, never()).patchIntersection(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when organizations_to_remove contains an org outside the user's qualified orgs")
        void unqualifiedOrgInOrganizationsToRemove_returns403() throws Exception {
            IntersectionPatch patchWithUnqualifiedOrgRemove = new IntersectionPatch(
                    12109, 12109, new RefPt(39.7392, -104.9903),
                    null, null, null,
                    List.of(), List.of("UnqualifiedOrg"), List.of(), List.of());

            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(List.of("TestOrg"));

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchWithUnqualifiedOrgRemove)))
                    .andExpect(status().isForbidden());

            verify(adminIntersectionService, never()).patchIntersection(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when rsus_to_add contains an RSU outside the user's qualified RSUs")
        void unqualifiedRsuInRsusToAdd_returns403() throws Exception {
            IntersectionPatch patchWithUnqualifiedRsu = new IntersectionPatch(
                    12109, 12109, new RefPt(39.7392, -104.9903),
                    null, null, null,
                    List.of(), List.of(), List.of("192.168.1.99"), List.of());

            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(List.of("TestOrg"));
            when(permissionService.hasRsus(eq(List.of("192.168.1.99")), eq("OPERATOR"))).thenReturn(false);

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchWithUnqualifiedRsu)))
                    .andExpect(status().isForbidden());

            verify(adminIntersectionService, never()).patchIntersection(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when rsus_to_remove contains an RSU outside the user's qualified RSUs")
        void unqualifiedRsuInRsusToRemove_returns403() throws Exception {
            IntersectionPatch patchWithUnqualifiedRsuRemove = new IntersectionPatch(
                    12109, 12109, new RefPt(39.7392, -104.9903),
                    null, null, null,
                    List.of(), List.of(), List.of(), List.of("192.168.1.99"));

            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(List.of("TestOrg"));
            when(permissionService.hasRsus(eq(List.of()), eq("OPERATOR"))).thenReturn(true);
            when(permissionService.hasRsus(eq(List.of("192.168.1.99")), eq("OPERATOR"))).thenReturn(false);

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchWithUnqualifiedRsuRemove)))
                    .andExpect(status().isForbidden());

            verify(adminIntersectionService, never()).patchIntersection(any());
        }

        @Test
        @WithMockUser
        @DisplayName("super user bypasses org restriction check entirely")
        void superUser_bypassesOrgRestriction_returns200() throws Exception {
            IntersectionPatch patchWithAnyOrg = new IntersectionPatch(
                    12109, 12109, new RefPt(39.7392, -104.9903),
                    null, null, null,
                    List.of("AnyOrgNotInQualifiedList"), List.of(), List.of(), List.of());

            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchWithAnyOrg)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 400 when orig_intersection_id is absent from request body")
        void missingOrigIntersectionId_returns400() throws Exception {
            // @RequestBody @Validated fires before @PreAuthorize — no auth stub needed
            String bodyMissingOrigId = """
                    {
                      "intersection_id": 12109,
                      "ref_pt": {"latitude": 39.7392, "longitude": -104.9903},
                      "organizations_to_add": [],
                      "organizations_to_remove": [],
                      "rsus_to_add": [],
                      "rsus_to_remove": []
                    }
                    """;

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bodyMissingOrigId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when ref_pt is absent from request body")
        void missingRefPt_returns400() throws Exception {
            String bodyMissingRefPt = """
                    {
                      "orig_intersection_id": 12109,
                      "intersection_id": 12109,
                      "organizations_to_add": [],
                      "organizations_to_remove": [],
                      "rsus_to_add": [],
                      "rsus_to_remove": []
                    }
                    """;

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bodyMissingRefPt))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when organizations_to_add is explicitly null")
        void nullOrganizationsToAdd_returns400() throws Exception {
            String bodyWithNullOrgs = """
                    {
                      "orig_intersection_id": 12109,
                      "intersection_id": 12109,
                      "ref_pt": {"latitude": 39.7392, "longitude": -104.9903},
                      "organizations_to_add": null,
                      "organizations_to_remove": [],
                      "rsus_to_add": [],
                      "rsus_to_remove": []
                    }
                    """;

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bodyWithNullOrgs))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("qualified operator with intersection access returns 200 with success message")
        void qualifiedOperator_returns200WithSuccessMessage() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(List.of("TestOrg"));
            when(permissionService.hasRsus(anyList(), eq("OPERATOR"))).thenReturn(true);

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatch)))
                    .andExpect(status().isOk());

            verify(adminIntersectionService).patchIntersection(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when service throws ResponseStatusException with NOT_FOUND")
        void serviceThrowsNotFound_returns404() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            doThrow(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Intersection not found: 12109")).when(adminIntersectionService)
                    .patchIntersection(any());

            mockMvc.perform(patch("/admin/intersections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatch)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /admin/intersections — delete intersection")
    class DeleteIntersection {

        @Test
        @DisplayName("returns 403 when no permissions are granted")
        void noPermissions_returns403() throws Exception {
            mockMvc.perform(delete("/admin/intersections/12109"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but hasRole('OPERATOR') returns false")
        void noOperatorRole_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(false);

            mockMvc.perform(delete("/admin/intersections/12109"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when operator role is granted but hasIntersection returns false")
        void operatorWithoutIntersectionAccess_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(false);

            mockMvc.perform(delete("/admin/intersections/12109"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("super user returns 200 with success message")
        void superUser_returns200WithSuccessMessage() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(delete("/admin/intersections/12109"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("qualified operator with intersection access returns 200 with success message")
        void qualifiedOperator_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.OPERATOR)).thenReturn(true);
            when(permissionService.hasIntersection(eq(12109), eq("OPERATOR"))).thenReturn(true);

            mockMvc.perform(delete("/admin/intersections/12109"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when service throws ResponseStatusException with NOT_FOUND")
        void serviceThrowsNotFound_returns404() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            doThrow(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Intersection not found: 99999")).when(adminIntersectionService)
                    .deleteIntersection(any());

            mockMvc.perform(delete("/admin/intersections/99999"))
                    .andExpect(status().isNotFound());
        }
    }
}
