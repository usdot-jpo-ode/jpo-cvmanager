package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepository;
import us.dot.its.jpo.ode.mockdata.MockBsmGenerator;
import us.dot.its.jpo.ode.model.OdeBsmData;

@RestController
@ConditionalOnProperty(
    name = "enable.api",
    havingValue = "true",
    matchIfMissing = false
)
@ApiResponses(
    value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }
)
public class BsmController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    OdeBsmJsonRepository odeBsmJsonRepo;

    @Autowired
    ConflictMonitorApiProperties props;

    @Operation(summary = "Find BSMs", description = "Returns a list of BSMs based on the provided parameters. Use latitude, longitude, and distance to find BSMs within a certain \"radius\" of a point (rectangle)")
    @RequestMapping(value = "/bsm/json", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    public ResponseEntity<List<OdeBsmData>> findBSMs(
            @RequestParam(name = "origin_ip", required = false) String originIp,
            @RequestParam(name = "vehicle_id", required = false) String vehicleId,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latitude", required = false) Double latitude,
            @RequestParam(name = "longitude", required = false) Double longitude,
            @RequestParam(name = "distance", required = false) Double distanceInMeters,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockBsmGenerator.getJsonBsms());
        } else {
            List<OdeBsmData> geoData = odeBsmJsonRepo.findOdeBsmDataGeo(originIp, vehicleId, startTime, endTime, longitude, latitude, distanceInMeters);
            logger.info("Found " + geoData.size() + " BSMs");
            return ResponseEntity.ok(geoData);
        }
    }

    @Operation(summary = "Count BSMs", description = "Returns the count of BSMs based on the provided parameters. Use latitude, longitude, and distance to find BSMs within a certain \"radius\" of a point (rectangle)")
    @RequestMapping(value = "/bsm/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
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
            long counts =  odeBsmJsonRepo.countOdeBsmDataGeo(originIp, vehicleId, startTime, endTime, longitude, latitude, distanceInMeters);
            logger.info("Found " + counts + " BSMs");
            return ResponseEntity.ok(counts);
        }
    }
}
