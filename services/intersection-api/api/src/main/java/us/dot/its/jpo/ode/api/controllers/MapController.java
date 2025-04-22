package us.dot.its.jpo.ode.api.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.mockdata.MockMapGenerator;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class MapController implements PageableQuery {

    private final ProcessedMapRepository processedMapRepo;

    @Value("${maximumResponseSize}")
    int maximumResponseSize;

    @Autowired
    public MapController(
            ProcessedMapRepository processedMapRepo) {
        this.processedMapRepo = processedMapRepo;
    }

    @Operation(summary = "Find Processed Map Messages", description = "Returns a list of Processed Map Messages based on the provided parameters. The latest parameter will return the most recent map message. The compact flag will omit the \"recordGeneratedAt\", \"properties.validationMessages\" fields.")
    @RequestMapping(value = "/map/json", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<ProcessedMap<LineString>>> findMaps(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "compact", required = false, defaultValue = "false") boolean compact,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<ProcessedMap<LineString>> list = MockMapGenerator.getProcessedMaps();
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(processedMapRepo.findLatest(intersectionID, startTime, endTime, compact));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<ProcessedMap<LineString>> response = processedMapRepo.find(intersectionID, startTime, endTime, compact,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Processed Map Messages", description = "Returns the count of Processed Map Messages based on the provided parameters.")
    @RequestMapping(value = "/map/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMaps(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(5L);
        } else {
            long count = processedMapRepo.count(intersectionID, startTime, endTime,
                    createNullablePage(page, size));
            return ResponseEntity.ok(count);

        }
    }
}