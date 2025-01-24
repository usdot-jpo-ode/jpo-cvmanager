package us.dot.its.jpo.ode.api.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.mockdata.MockMapGenerator;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
public class MapController {

    @Autowired
    ProcessedMapRepository processedMapRepo;

    @RequestMapping(value = "/map/json", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<ProcessedMap<LineString>>> findMaps(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData,
            @RequestParam(name = "compact", required = false, defaultValue = "false") boolean compact) {

        if (testData) {
            return ResponseEntity.ok(MockMapGenerator.getProcessedMaps());
        } else {
            Query query = processedMapRepo.getQuery(intersectionID, startTime, endTime, latest, compact);
            return ResponseEntity.ok(processedMapRepo.findProcessedMaps(query));

        }
    }

    @RequestMapping(value = "/map/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<Long> countMaps(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(5L);
        } else {
            Query query = processedMapRepo.getQuery(intersectionID, startTime, endTime, false, true);
            long count = processedMapRepo.getQueryResultCount(query);

            log.debug("Found: {} ProcessedMap Messages", count);
            return ResponseEntity.ok(count);

        }
    }
}