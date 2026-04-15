package us.dot.its.jpo.ode.api.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionListResponse;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionPatch;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionSingleResponse;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.services.AdminIntersectionService;
import us.dot.its.jpo.ode.api.services.PermissionService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * REST controller for admin intersection management.
 * Migrated from the Python Flask AdminIntersection resource at
 * /admin-intersection.
 *
 * All authorization is handled in this layer (controller/auth), not in the
 * service:
 * - Role checks and intersection resource access are enforced via @PreAuthorize
 * expressions.
 * - Org restriction enforcement on PATCH (organizations_to_add/remove must be
 * within the
 * user's qualified orgs) is enforced in the method body via PermissionService.
 * - AdminIntersectionService is responsible only for database operations.
 */
@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true")
@RequestMapping("/admin/intersections")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin Intersection", description = "Manage traffic intersections and their organization/RSU relationships")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error"),
})
public class AdminIntersectionController {

    private final AdminIntersectionService adminIntersectionService;
    private final PermissionService permissionService;

    /**
     * Returns all intersections for the specified organization.
     * The Organization header is required for all users, including super users.
     * Authorization (outer check) runs before query parameter validation.
     */
    @Operation(summary = "List all intersections", description = """
            Returns all intersections for the specified organization.
            The Organization header is required for all users, including super users.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Missing Organization header"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role"),
            @ApiResponse(responseCode = "404", description = "No intersections found for the specified organization"),
    })
    @GetMapping(produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    public IntersectionListResponse getAllIntersections(
            @Parameter(description = "Organization to scope results to", required = true) @RequestHeader(name = "Organization") String organization) {

        log.info("GET /admin/intersections. organization={}", organization);
        return adminIntersectionService.getAllIntersections(organization);
    }

    /**
     * Returns a single intersection and allowed_selections for UI dropdown
     * population.
     * Authorization: USER role + intersection access enforced by @PreAuthorize.
     * allowed_selections is computed by PermissionService for UI dropdown
     * population.
     */
    @Operation(summary = "Get a single intersection", description = """
            Returns a single intersection by number, plus allowed_selections for UI dropdown population.
            Role check: USER required.
            Intersection access check: user must have access to the specified intersection.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role or no access to this intersection"),
            @ApiResponse(responseCode = "404", description = "Intersection not found"),
    })
    @GetMapping(value = "/{intersectionId}", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRole('USER') && @PermissionService.hasIntersection(#intersectionId, 'USER'))")
    public IntersectionSingleResponse getIntersection(
            @Parameter(description = "Intersection number to retrieve", example = "12109") @PathVariable Integer intersectionId,
            @Parameter(description = "Scope results to a specific organization") @RequestHeader(name = "Organization", required = false) String organization) {

        log.info("GET /admin/intersections/{}. organization={}", intersectionId, organization);
        return adminIntersectionService.getIntersection(intersectionId);
    }

    /**
     * Updates an intersection's properties and modifies its organization/RSU
     * relationships.
     * Request body validation runs after the permission checks.
     * Authorization (all enforced in this layer):
     * 1. @PreAuthorize: OPERATOR role AND access to the specific intersection.
     * 2. Method body: each org in organizations_to_add/remove must be in the user's
     * qualified orgs (superusers exempt). Returns 403 if any org is not allowed.
     */
    @Operation(summary = "Update an intersection", description = """
            Updates an existing intersection record and its organization/RSU associations.
            Role check: OPERATOR required.
            Intersection access check: user must have access to the specified intersection.
            Org enforcement: organizations_to_add and organizations_to_remove must each be
            within the user's qualified organizations (superusers exempt).
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intersection successfully modified"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid required fields"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires OPERATOR role, intersection access, or org restriction violation"),
            @ApiResponse(responseCode = "404", description = "Intersection not found"),
    })
    @PatchMapping(produces = "application/json", consumes = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRole('OPERATOR') && @PermissionService.hasIntersection(#patch.origIntersectionId, 'OPERATOR'))")
    public void patchIntersection(@RequestBody @Validated IntersectionPatch patch) {

        log.info("PATCH /admin/intersections. origIntersectionId={}", patch.getOrigIntersectionId());
        if (!permissionService.isSuperUser()) {
            CvManagerAuthToken token = permissionService.getCvManagerAuthToken();
            List<String> qualifiedOrgs = token != null
                    ? token.getQualifiedOrgList(UserRole.OPERATOR)
                    : Collections.emptyList();
            Set<String> qualifiedOrgSet = new HashSet<>(qualifiedOrgs);
            boolean allOrgsAllowed = qualifiedOrgSet.containsAll(patch.getOrganizationsToAdd())
                    && qualifiedOrgSet.containsAll(patch.getOrganizationsToRemove());
            if (!allOrgsAllowed) {
                log.warn("Org enforcement rejected PATCH on intersection {}. Requested orgs not in qualified set.",
                        patch.getOrigIntersectionId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Not authorized to modify one or more of the specified organizations");
            }

            // Verify RSU accessibility
            if (!permissionService.hasRsus(patch.getRsusToAdd(), "OPERATOR") ||
                    !permissionService.hasRsus(patch.getRsusToRemove(), "OPERATOR")) {
                log.warn("RSU enforcement rejected PATCH on intersection {}. Requested RSUs not in qualified set.",
                        patch.getOrigIntersectionId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Not authorized to modify one or more of the specified RSUs");
            }
        }

        adminIntersectionService.patchIntersection(patch);
    }

    /**
     * Removes an intersection and all its relationship records in dependency order.
     * Request parameter validation runs after the permission check.
     * <p>
     * Authorization (enforced in this layer):
     * 1. @PreAuthorize: OPERATOR role AND access to the specific intersection.
     */
    @Operation(summary = "Delete an intersection", description = """
            Removes an intersection and its intersection_organization and rsu_intersection records.
            Role check: OPERATOR required.
            Intersection access check: user must have access to the specified intersection.
            Returns 404 if the intersection does not exist.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intersection successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Missing or blank intersection_id parameter"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires OPERATOR role or no access to this intersection"),
            @ApiResponse(responseCode = "404", description = "Intersection not found"),
    })
    @DeleteMapping(value = "/{intersectionId}", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRole('OPERATOR') && @PermissionService.hasIntersection(#intersectionId, 'OPERATOR'))")
    public void deleteIntersection(
            @Parameter(description = "Intersection number to delete", example = "12109") @PathVariable String intersectionId) {

        log.info("DELETE /admin/intersections/{}. intersectionId={}", intersectionId, intersectionId);
        adminIntersectionService.deleteIntersection(intersectionId);
    }
}
