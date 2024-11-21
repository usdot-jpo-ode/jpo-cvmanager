package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.mockdata.MockMapGenerator;

@RestController
public class MapController {

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ProcessedMapRepository processedMapRepo;

    @Autowired
    ConflictMonitorApiProperties props;

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/map/json", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            long count = processedMapRepo.getQueryResultCount(query);
            
            logger.info("Returning ProcessedMap Response with Size: " + count);
            return ResponseEntity.ok(processedMapRepo.findProcessedMaps(query));
            
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/map/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            
            logger.info("Found: " + count + "Processed Map Messages");
            return ResponseEntity.ok(count);
            
        }
    }
}