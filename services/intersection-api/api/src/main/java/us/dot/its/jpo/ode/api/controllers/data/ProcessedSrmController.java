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
import us.dot.its.jpo.geojsonconverter.pojos.geojson.srm.ProcessedSrm;
import us.dot.its.jpo.ode.api.accessors.srm.ProcessedSrmRepository;
import us.dot.its.jpo.ode.mockdata.MockSrmGenerator;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/data/processed-srm")
public class ProcessedSrmController {

    private final ProcessedSrmRepository processedSrmJsonRepo;

    @Autowired
    public ProcessedSrmController(ProcessedSrmRepository processedSrmJsonRepo) {
        this.processedSrmJsonRepo = processedSrmJsonRepo;
    }

    @Operation(summary = "Find Processed SRMs", description = "Returns a list of Processed SRMs based on the provided parameters. Use latitude, longitude, and distance to find Processed SRMs within a certain \"radius\" of a point (rectangle)")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<Page<ProcessedSrm>> findProcessedSRMs(
            @RequestParam(name = "vehicle_id", required = false) String vehicleId,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "intersection_id", required = false) Integer intersectionId,
                    @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<ProcessedSrm> list = MockSrmGenerator.getProcessedSrms();

            return ResponseEntity
                    .ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            Page<ProcessedSrm> response = processedSrmJsonRepo.find(intersectionId, vehicleId, startTime, endTime,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Processed SRMs", description = "Returns the count of SRMs based on the provided parameters. Use latitude, longitude, and distance to find SRMs within a certain \"radius\" of a point (rectangle)")
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<Long> countProcessedSRMs(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionId,
                    @RequestParam(name = "vehicle_id", required = false) String vehicleId,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
                    @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(10L);
        } else {
            long counts = processedSrmJsonRepo.count(intersectionId, vehicleId, startTime, endTime);
            log.debug("Found {} SRM counts", counts);
            return ResponseEntity.ok(counts);
        }
    }
}
