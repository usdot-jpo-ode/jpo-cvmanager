package us.dot.its.jpo.ode.api.controllers.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.models.snmp.RsuState;
import us.dot.its.jpo.ode.api.accessors.rsuState.RsuStateRepository;

import java.util.List;

@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/data/rsu-status")
public class RsuController {

    private final RsuStateRepository rsuStateRepository;

    @Autowired
    public RsuController(RsuStateRepository rsuStateRepository) {
        this.rsuStateRepository = rsuStateRepository;
    }

    @Operation(summary = "Get historical RSU Status", description = "Returns all RSU status records for the given RSU IP and time range (UTC ms)")
    @GetMapping(value = "/historical", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<RsuState>> getHistoricalRsuStatus(
            @RequestParam String rsuIp,
            @RequestParam long startTime,
            @RequestParam long endTime) {
        if (startTime > endTime) {
            return ResponseEntity.badRequest().build();
        }
        List<RsuState> results = rsuStateRepository.retrieveRsuStateWithinTimeInterval(rsuIp, startTime, endTime);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get latest RSU Status", description = "Returns the most recent RSU status record for the given RSU IP")
    @GetMapping(value = "/latest", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
            @ApiResponse(responseCode = "404", description = "No RSU status found for this RSU IP")
    })
    public ResponseEntity<RsuState> getLatestRsuStatus(@RequestParam String rsuIp) {
        RsuState latest = rsuStateRepository.findLatestByRsuIP(rsuIp);
        if (latest != null) {
            return ResponseEntity.ok(latest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get aggregated RSU Status", description = "Returns aggregated RSU status records for the given RSU IP and time range (UTC ms)")
    @GetMapping(value = "/aggregated", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<RsuState>> getAggregatedRsuStatus(
            @RequestParam String rsuIp,
            @RequestParam long startTime,
            @RequestParam long endTime,
            @RequestParam int intervalMinutes) {
        if (startTime > endTime || intervalMinutes <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<RsuState> results = rsuStateRepository.retrieveRsuStateWithinTimeInterval(rsuIp, startTime,
                endTime, intervalMinutes);
        return ResponseEntity.ok(results);
    }
}