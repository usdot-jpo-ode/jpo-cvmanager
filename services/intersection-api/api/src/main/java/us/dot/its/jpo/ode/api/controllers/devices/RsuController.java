package us.dot.its.jpo.ode.api.controllers.devices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.models.devices.management.ModifyRsuAllowedSelections;
import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuInfoDto;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.RsuManagementService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/devices/rsus")
@RequiredArgsConstructor
public class RsuController {
    private final RsuManagementService rsuManagementService;

    private static final Map<String, String> SORT_FIELD_MAPPING = Map.of(
            "ip", "ipv4Address",
            "primary_route", "primaryRoute",
            "serial_number", "serialNumber",
            "scms_id", "issScmsId");

    @Operation(summary = "Get All RSUs for Organization", description = "Get summary data for all RSUs the user has access to in the specified organization.")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json", params = "!rsu_ip")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public Page<RsuInfoDto> getAllRsus(
            @RequestHeader(name = "Organization", required = true) String organization,
            @RequestParam(name = "search", required = false) String search,
                    @PageableDefault(size = 100) Pageable pageable) {
        Pageable mappedPageable = mapSortFields(pageable);

        Page<RsuInfoDto> allRsuInfo = rsuManagementService.getAllRsuInfo(organization, search, mappedPageable);
        return allRsuInfo;
    }

    @Operation(summary = "Get Single RSU Management Data", description = "Get RSU data required for RSU modification page. "
            + "Returns detailed data for the specified RSU along with allowed selections for modification.")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json", params = "rsu_ip")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRsu(#rsuIp, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the RSU requested"),
    })
    public RsuInfoDto getSingleRsuData(
            @RequestParam(name = "rsu_ip", required = true) String rsuIp) {
        RsuInfoDto rsuInfo = rsuManagementService.getRsuInfo(rsuIp);
        if (rsuInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found");
        }

        return rsuInfo;
    }

    @Operation(summary = "Get Allowed Selections for RSU Management", description = "Get RSU data required for RSU modification page. "
            + "Returns detailed data for the specified RSU along with allowed selections for modification.")
    @RequestMapping(method = RequestMethod.GET, path = "/allowed-selections", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('OPERATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or OPERATOR role with access to the RSU requested"),
    })
    public ModifyRsuAllowedSelections getAllowedSelections() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = PermissionService.getUsername(auth);
        ModifyRsuAllowedSelections allowedSelections = rsuManagementService.getAllowedSelections(username);

        return allowedSelections;
    }

    @Operation(summary = "Modify RSU", description = "Modify RSU information")
    @RequestMapping(method = RequestMethod.PATCH, produces = "application/json", params = "rsu_ip")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRsu(#rsuIp, 'OPERATOR') and @PermissionService.hasRole('OPERATOR'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or OPERATOR role with access to the RSU requested"),
    })
    public ResponseEntity<Void> modifyRsu(@RequestParam(name = "rsu_ip", required = true) String rsuIp,
            @Validated @RequestBody RsuPatch body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = PermissionService.getUsername(auth);
        rsuManagementService.modifyRsu(rsuIp, body, username);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete RSU", description = "Delete RSU from management system")
    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json", params = "rsu_ip")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRsu(#rsuIp, 'OPERATOR') and @PermissionService.hasRole('OPERATOR'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or OPERATOR role with access to the RSU requested"),
    })
    public ResponseEntity<Void> deleteRsu(@RequestParam(name = "rsu_ip", required = true) String rsuIp) {
        rsuManagementService.deleteRsuByIpv4Address(rsuIp);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Multiple RSUs", description = "Delete Multiple RSUs from management system")
    @RequestMapping(method = RequestMethod.DELETE, path = "/batch", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasRsus(#rsuIps, 'OPERATOR') && @PermissionService.hasRole('OPERATOR'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or OPERATOR role with access to the RSU requested"),
    })
    public ResponseEntity<Void> deleteRsus(@RequestBody List<String> rsuIps) {
        rsuManagementService.deleteMultipleRsusByIpv4Address(rsuIps);

        return ResponseEntity.noContent().build();
    }

    /*
     * Maps API sort fields to database fields. If no mapping is found, the original
     * field is used. This allows the API to use more user-friendly field names
     * while still supporting sorting on database fields.
     */
    private Pageable mapSortFields(Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return pageable;
        }

        Sort mappedSort = Sort.unsorted();

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            String mappedProperty = SORT_FIELD_MAPPING.getOrDefault(property, property);
            mappedSort = mappedSort.and(Sort.by(order.getDirection(), mappedProperty));
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                mappedSort);
    }
}