package us.dot.its.jpo.ode.api.controllers.devices.rsus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoDto;
import us.dot.its.jpo.ode.api.services.RsuInfoService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/devices/rsus/info")
@RequiredArgsConstructor
public class InfoController {

    private final RsuInfoService rsuInfoService;

    @Operation(summary = "Get RSU Geographic Info", description = "Returns a GeoJSON Feature list of all RSUs the user has access to within their specified organisation.")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(description = "Map with rsuList containing GeoJSON Feature objects for each RSU"))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires membership in the specified organisation"),
    })
    public ResponseEntity<Map<String, List<RsuGeoInfoDto>>> getRsuInfo(
            @RequestHeader(name = "Organization", required = true) String organization) {
        log.debug("GET /devices/rsus/info requested for organisation '{}'", organization);
        List<RsuGeoInfoDto> rsuList = rsuInfoService.getRsuGeoInfoByOrganization(organization);
        return ResponseEntity.ok(Map.of("rsuList", rsuList));
    }
}
