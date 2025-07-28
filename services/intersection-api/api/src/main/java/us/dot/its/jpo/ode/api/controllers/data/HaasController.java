package us.dot.its.jpo.ode.api.controllers.data;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;

import us.dot.its.jpo.ode.api.accessors.haas.HaasLocationDataRepository;
import us.dot.its.jpo.ode.api.models.PaginatedGeoJsonResponse;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

@Slf4j
@RestController
@ConditionalOnProperty(value = { "enable.api", "enable.haas" }, havingValue = "true", matchIfMissing = false)
@RequestMapping(value = "/data/haas/")
@ApiResponses(value = {
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class HaasController {

        private final HaasLocationDataRepository haasLocationDataRepository;

        @Autowired
        public HaasController(HaasLocationDataRepository haasLocationDataRepository) {
                this.haasLocationDataRepository = haasLocationDataRepository;
        }

        @Operation(summary = "HAAS Alert Locations", description = "Returns HAAS alert locations in GeoJSON format with pagination")
        @GetMapping(value = "/locations", produces = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Success"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role")
        })
        public ResponseEntity<PaginatedGeoJsonResponse> getLocations(
                        @RequestParam(name = "active_only", required = false, defaultValue = "true") boolean activeOnly,
                        @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
                        @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
                        @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
                        @PageableDefault(size = 10000, page = 0) Pageable pageable) {

                Page<HaasLocation> locations = haasLocationDataRepository.find(
                                activeOnly,
                                startTime,
                                endTime,
                                pageable);

                return ResponseEntity.ok(PaginatedGeoJsonResponse.from(locations));
        }

}
