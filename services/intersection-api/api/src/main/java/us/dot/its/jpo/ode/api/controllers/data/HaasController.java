package us.dot.its.jpo.ode.api.controllers.data;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;

import us.dot.its.jpo.ode.api.accessors.haas.HaasLocationDataRepository;
import us.dot.its.jpo.ode.api.converters.HaasLocationConverter;
import us.dot.its.jpo.ode.api.models.LimitedGeoJsonResponse;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.models.haas.HaasLocationResult;

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

        public HaasController(HaasLocationDataRepository haasLocationDataRepository) {
                this.haasLocationDataRepository = haasLocationDataRepository;
        }

        @Operation(summary = "HAAS Alert Locations", description = "Returns HAAS alert locations in GeoJSON format with a limit on the number of features")
        @GetMapping(value = "/locations", produces = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Success"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role")
        })
        public ResponseEntity<LimitedGeoJsonResponse> getLocations(
                        @RequestParam(name = "active_only", required = false, defaultValue = "true") boolean activeOnly,
                        @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
                        @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
                        @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
                        @RequestParam(name = "limit", required = false, defaultValue = "1000") int limit) {

                HaasLocationResult result = haasLocationDataRepository.findWithLimit(
                                activeOnly,
                                startTime,
                                endTime,
                                limit);

                return ResponseEntity.ok(LimitedGeoJsonResponse.fromList(result.getLocations(), limit,
                                result.isHasMoreResults()));
        }

}
