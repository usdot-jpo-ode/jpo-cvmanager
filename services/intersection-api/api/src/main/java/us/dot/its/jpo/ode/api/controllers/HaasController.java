package us.dot.its.jpo.ode.api.controllers;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.accessors.haas.websocket.HaasWebsocketLocationDataRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.models.haas.websocket.HaasWebsocketLocation;
import us.dot.its.jpo.ode.mockdata.MockSpatGenerator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonFeatureCollection;
import us.dot.its.jpo.ode.api.converters.HaasLocationConverter;
import org.springframework.http.MediaType;
import us.dot.its.jpo.ode.api.models.PaginatedGeoJsonResponse;

@Slf4j
@RestController
@ConditionalOnProperty(value = { "enable.api", "enable.haas" }, havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class HaasController {

        private final HaasWebsocketLocationDataRepository haasWebsocketLocationDataRepository;

        @Autowired
        public HaasController(HaasWebsocketLocationDataRepository haasWebsocketLocationDataRepository) {
                this.haasWebsocketLocationDataRepository = haasWebsocketLocationDataRepository;
        }

        @Operation(summary = "Hello World", description = "Test endpoint")
        @RequestMapping(value = "/haas", method = RequestMethod.GET, produces = "application/json")
        @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Success"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
        })
        public ResponseEntity<String> helloWorld() {
                return ResponseEntity.ok("Hello World");
        }

        @Operation(summary = "HAAS Alert Locations", description = "Returns HAAS alert locations in GeoJSON format with pagination")
        @RequestMapping(value = "/haas/locations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
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
                        @RequestParam(name = "compact", required = false, defaultValue = "false") boolean compact,
                        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                        @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
                        @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

                PageRequest pageable = PageRequest.of(page, size);
                Page<HaasWebsocketLocation> locations = haasWebsocketLocationDataRepository.findLatest(
                                activeOnly,
                                startTime,
                                endTime,
                                pageable);

                return ResponseEntity.ok(PaginatedGeoJsonResponse.from(locations));
        }

}
