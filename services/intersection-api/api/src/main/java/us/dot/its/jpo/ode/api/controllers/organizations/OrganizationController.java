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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.mappers.RsuInfoMapper;
import us.dot.its.jpo.ode.api.mappers.UserMapper;
import us.dot.its.jpo.ode.api.models.devices.RsuInfoDto;
import us.dot.its.jpo.ode.api.models.users.UserDto;
import us.dot.its.jpo.ode.api.repositories.RsuOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.UserOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;

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
    final RsuInfoMapper rsuInfoMapper;
    final RsuRepository rsuRepository;
    final UserMapper userMapper;
    final UserRepository userRepository;
    final RsuOrganizationRepository rsuOrganizationRepository;
    final UserOrganizationRepository userOrganizationRepository;

    @Operation(summary = "Get RSU IPs by Organization", description = "Retrieves a list of IP addresses for all RSUs belonging to the specified organization.")
    @RequestMapping(path = "rsus", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(path = "rsus/{rsuIp}", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRsu(#rsuIp, 'ADMIN') and @PermissionService.hasRole('ADMIN'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid RSU IP address format"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the RSU requested"),
    })
    public List<String> getRsuOrganizationAssignments(
            @Parameter(description = "RSU IP address", example = "192.168.1.1", required = true) @PathVariable(name = "rsuIp") String rsuIp) {
        try {
            return rsuRepository.findAllOrganizationNamesByIpv4Address(InetAddress.getByName(rsuIp));
        } catch (UnknownHostException e) {
            log.error("Invalid RSU IP address: {}", rsuIp, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSU IP address: " + rsuIp, e);
        }
    }

    @Operation(summary = "Get RSU IPs not in Organization", description = "Retrieves a list of IP addresses for all RSUs not belonging to the specified organization.")
    @RequestMapping(path = "rsus/available", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role"),
    })
    public List<RsuInfoDto> getRsuIpsNotInOrganization(
            @RequestHeader(name = "Organization", required = true) String organization) {
        return rsuOrganizationRepository.findAllRsusNotInOrganizationName(organization).stream()
                .map(rsuInfoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get User Emails by Organization", description = "Retrieves a list of user emails for all users belonging to the specified organization.")
    @RequestMapping(path = "users", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role"),
    })
    public List<String> getUserEmailsByOrganization(
            @RequestHeader(name = "Organization", required = true) String organization) {
        return userOrganizationRepository.findAllUserEmailsByOrganizationName(organization);
    }

    @Operation(summary = "Get User Organization Assignments", description = "Retrieves a list of organization names that the specified user is assigned to.")
    @RequestMapping(path = "users/{email}", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasUser(#email, 'ADMIN') and @PermissionService.hasRole('ADMIN'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the user requested"),
    })
    public List<String> getUserOrganizationAssignments(
            @Parameter(description = "User email address", example = "user@example.com", required = true) @PathVariable(name = "email") String email) {
        return userRepository.findAllOrganizationNamesByEmail(email);
    }

    @Operation(summary = "Get Users Not In Organization", description = "Retrieves a list of user emails for all users not belonging to the specified organization.")
    @RequestMapping(path = "users/available", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role"),
    })
    public List<UserDto> getUserEmailsNotInOrganization(
            @RequestHeader(name = "Organization", required = true) String organization) {
        return userOrganizationRepository.findAllUserEmailsNotInOrganizationName(organization).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}