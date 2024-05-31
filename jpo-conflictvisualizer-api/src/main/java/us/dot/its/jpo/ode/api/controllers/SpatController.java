package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.mockdata.MockSpatGenerator;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@RestController
public class SpatController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ProcessedSpatRepository processedSpatRepo;

    @Autowired
    ConflictMonitorApiProperties props;

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/spat/json", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<ProcessedSpat>> findSpats(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "compact", required = false, defaultValue = "false") boolean compact,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockSpatGenerator.getProcessedSpats());
        } else {
            Query query = processedSpatRepo.getQuery(intersectionID, startTime, endTime, latest, compact);
            long count = processedSpatRepo.getQueryResultCount(query);
            logger.info("Returning Processed Spat Response with Size: " + count);
            return ResponseEntity.ok(processedSpatRepo.findProcessedSpats(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/spat/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSpats(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(80L);
        } else {
            Query query = processedSpatRepo.getQuery(intersectionID, startTime, endTime,false, true);
            long count = processedSpatRepo.getQueryResultCount(query);
            logger.info("Found: " + count + "Processed Spat Messages");
            return ResponseEntity.ok(count);
        }
    }
}
