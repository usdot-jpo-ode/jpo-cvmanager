package us.dot.its.jpo.ode.api.controllers.organizations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.repositories.RsuOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    final RsuRepository rsuRepository;
    final RsuOrganizationRepository rsuOrganizationRepository;

    @Operation(summary = "Get RSU IPs by Organization", description = "Retrieves a list of IP addresses for all RSUs belonging to the specified organization.")
    @RequestMapping(path = "rsus", method = RequestMethod.GET, produces = "application/json", params = "!rsu_ip")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role"),
    })
    public List<String> getRsuIpsByOrganization(
            @RequestHeader(name = "Organization", required = true) String organization) {
        return rsuOrganizationRepository.findAllRsuIpsByOrganizationName(organization).stream()
                .map(InetAddress::getHostAddress)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get RSU Organization Assignments", description = "Retrieves a list of organization names that the specified RSU is assigned to.")
    @RequestMapping(path = "rsus", method = RequestMethod.GET, produces = "application/json", params = "rsu_ip")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRsu(#rsuIp, 'ADMIN') and @PermissionService.hasRole('ADMIN'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid RSU IP address format"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the RSU requested"),
    })
    public List<String> getRsuOrganizationAssignments(
            @RequestParam(name = "rsu_ip", required = true) String rsuIp) {
        try {
            return rsuRepository.findAllOrganizationNamesByIpv4Address(InetAddress.getByName(rsuIp));
        } catch (UnknownHostException e) {
            log.error("Invalid RSU IP address: {}", rsuIp, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSU IP address: " + rsuIp, e);
        }
    }
}