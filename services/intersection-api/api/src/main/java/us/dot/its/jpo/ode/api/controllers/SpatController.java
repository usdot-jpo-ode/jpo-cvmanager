package us.dot.its.jpo.ode.api.controllers;

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
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.mockdata.MockSpatGenerator;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class SpatController {

    @Autowired
    ProcessedSpatRepository processedSpatRepo;

    @Operation(summary = "Find SPATs", description = "Returns a list of SPATs based on the provided parameters. The latest parameter will return the most recent SPAT message. The compact flag will omit the \"recordGeneratedAt\", \"validationMessages\" fields.")
    @RequestMapping(value = "/spat/json", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<ProcessedSpat>> findSpats(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "compact", required = false, defaultValue = "false") boolean compact,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockSpatGenerator.getProcessedSpats());
        } else {
            Query query = processedSpatRepo.getQuery(intersectionID, startTime, endTime, latest, compact);
            return ResponseEntity.ok(processedSpatRepo.findProcessedSpats(query));
        }
    }

    @Operation(summary = "Count SPATs", description = "Returns the count of SPATs based on the provided parameters.")
    @RequestMapping(value = "/spat/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpats(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(80L);
        } else {
            Query query = processedSpatRepo.getQuery(intersectionID, startTime, endTime, false, true);
            long count = processedSpatRepo.getQueryResultCount(query);
            log.info("Found: {} ProcessedSpat Messages", count);
            return ResponseEntity.ok(count);
        }
    }

}
