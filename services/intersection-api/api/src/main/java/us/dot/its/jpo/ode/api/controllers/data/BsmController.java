package us.dot.its.jpo.ode.api.controllers.data;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepository;
import us.dot.its.jpo.ode.mockdata.MockBsmGenerator;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/data/ode-bsm-json")
public class BsmController {

    private final OdeBsmJsonRepository odeBsmJsonRepo;

    @Autowired
    public BsmController(OdeBsmJsonRepository odeBsmJsonRepo) {
        this.odeBsmJsonRepo = odeBsmJsonRepo;
    }

    @Operation(summary = "Find BSMs", description = "Returns a list of BSMs based on the provided parameters. Use latitude, longitude, and distance to find BSMs within a certain \"radius\" of a point (rectangle)")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<Page<OdeBsmData>> findBSMs(
            @RequestParam(name = "origin_ip", required = false) String originIp,
            @RequestParam(name = "vehicle_id", required = false) String vehicleId,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latitude", required = false) Double latitude,
            @RequestParam(name = "longitude", required = false) Double longitude,
            @RequestParam(name = "distance", required = false) Double distanceInMeters,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<OdeBsmData> list = MockBsmGenerator.getJsonBsms();
            return ResponseEntity
                    .ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            Page<OdeBsmData> response = odeBsmJsonRepo.find(originIp, vehicleId, startTime, endTime,
                    longitude, latitude, distanceInMeters, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count BSMs", description = "Returns the count of BSMs based on the provided parameters. Use latitude, longitude, and distance to find BSMs within a certain \"radius\" of a point (rectangle)")
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<Long> countBSMs(
            @RequestParam(name = "origin_ip", required = false) String originIp,
            @RequestParam(name = "vehicle_id", required = false) String vehicleId,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latitude", required = false) Double latitude,
            @RequestParam(name = "longitude", required = false) Double longitude,
            @RequestParam(name = "distance", required = false) Double distanceInMeters,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(10L);
        } else {
            long counts = odeBsmJsonRepo.count(originIp, vehicleId, startTime, endTime, longitude,
                    latitude, distanceInMeters);
            log.debug("Found {} BSM counts", counts);
            return ResponseEntity.ok(counts);
        }
    }
}
