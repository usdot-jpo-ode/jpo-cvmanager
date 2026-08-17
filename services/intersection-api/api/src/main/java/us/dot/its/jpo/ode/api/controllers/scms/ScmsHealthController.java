package us.dot.its.jpo.ode.api.controllers.scms;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.dot.its.jpo.ode.api.mappers.ScmsHealthMapper;
import us.dot.its.jpo.ode.api.models.scms.ScmsHealthResponse;
import us.dot.its.jpo.ode.api.services.ScmsHealthService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true")
@RequestMapping("/devices/scms")
@RequiredArgsConstructor
@Tag(name = "SCMS Health Status", description = "Manage SCMS health status for RSUs")
public class ScmsHealthController {

    private final ScmsHealthService scmsHealthService;
    private final ScmsHealthMapper scmsHealthMapper;

    @Operation(
            summary = "Retrieve SCMS health status for RSUs in the given organization",
            description = """
                    Returns a map of RSU IDs to their health status for the specified organization.
                    The Organization header is required for all users, including super users.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Organization header is missing"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role in the specified organization"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
    })
    @GetMapping(value = "/status", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRoleInOrg(#organization, 'USER')")
    public ScmsHealthResponse getAllStatuses(@RequestHeader(name = "Organization") String organization) {
        log.info("GET /devices/scms/status. organization: {}", organization);
        return scmsHealthMapper.toResponse(scmsHealthService.getScmsStatuses(organization));
    }
}
