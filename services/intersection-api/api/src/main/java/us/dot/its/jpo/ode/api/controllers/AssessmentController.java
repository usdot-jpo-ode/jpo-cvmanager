package us.dot.its.jpo.ode.api.controllers;

import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessment;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment.StopLineStopAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment.SignalStateEventAssessmentRepository;
import us.dot.its.jpo.ode.mockdata.MockAssessmentGenerator;

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
public class AssessmentController {

    @Autowired
    LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo;

    @Autowired
    ConnectionOfTravelAssessmentRepository connectionOfTravelAssessmentRepo;

    @Autowired
    StopLineStopAssessmentRepository stopLineStopAssessmentRepo;

    @Autowired
    SignalStateEventAssessmentRepository signalStateEventAssessmentRepo;

    @Autowired
    ConflictMonitorApiProperties props;

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    public AssessmentController(){
        System.out.println("Enabling Assessment API Endpoints");
    }

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @Operation(summary = "Get Connection of Travel Assessments", description = "Get Connection of Travel Assessments, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/assessments/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<List<ConnectionOfTravelAssessment>> findConnectionOfTravelAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<ConnectionOfTravelAssessment> list = new ArrayList<>();
            list.add(MockAssessmentGenerator.getConnectionOfTravelAssessment());
            return ResponseEntity.ok(list);
        } else {
            Query query = connectionOfTravelAssessmentRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = connectionOfTravelAssessmentRepo.getQueryResultCount(query);
           
             
            logger.info("Returning ProcessedMap Response with Size: " + count);
            return ResponseEntity.ok(connectionOfTravelAssessmentRepo.find(query));
        }
    }

    @Operation(summary = "Get Connection of Travel Assessments Count", description = "Get Connection of Travel Assessments count, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/assessments/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<Long> countConnectionOfTravelAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = connectionOfTravelAssessmentRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = connectionOfTravelAssessmentRepo.getQueryFullCount(query);
            }else{
                count = connectionOfTravelAssessmentRepo.getQueryResultCount(query);
            }
            
            logger.info("Found: " + count + " Connection of Travel Assessments");
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Get Lane Direction of Travel Assessments", description = "Get Lane Direction of Travel Assessments, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/assessments/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<List<LaneDirectionOfTravelAssessment>> findLaneDirectionOfTravelAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<LaneDirectionOfTravelAssessment> list = new ArrayList<>();
            list.add(MockAssessmentGenerator.getLaneDirectionOfTravelAssessment());
            return ResponseEntity.ok(list);
        } else {
            Query query = laneDirectionOfTravelAssessmentRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = laneDirectionOfTravelAssessmentRepo.getQueryResultCount(query);
            logger.info("Returning LaneDirectionOfTravelAssessment Response with Size: " + count);
            return ResponseEntity.ok(laneDirectionOfTravelAssessmentRepo.find(query));
        }

    }

    @Operation(summary = "Get Lane Direction of Travel Assessment Counts", description = "Get Lane Direction of Travel Assessment counts, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/assessments/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<Long> countLaneDirectionOfTravelAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = laneDirectionOfTravelAssessmentRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = laneDirectionOfTravelAssessmentRepo.getQueryFullCount(query);
            }else{
                count = laneDirectionOfTravelAssessmentRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Lane Direction of Travel Assessments");
            return ResponseEntity.ok(count);
        }

    }

    @Operation(summary = "Get Signal State Assessments", description = "Get Signal State Assessments, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/assessments/signal_state_assessment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<List<StopLineStopAssessment>> findSignalStateAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<StopLineStopAssessment> list = new ArrayList<>();
            list.add(MockAssessmentGenerator.getStopLineStopAssessment());
            return ResponseEntity.ok(list);
        } else {
            
            Query query = stopLineStopAssessmentRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = stopLineStopAssessmentRepo.getQueryResultCount(query);
            logger.info("Returning SignalStateAssessment Response with Size: " + count);
            return ResponseEntity.ok(stopLineStopAssessmentRepo.find(query));
        }
    }

    @Operation(summary = "Get Signal State Assessment Counts", description = "Get Signal State Assessment counts, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/assessments/signal_state_assessment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<Long> countSignalStateAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            
            Query query = stopLineStopAssessmentRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = stopLineStopAssessmentRepo.getQueryFullCount(query);
            }else{
                count = stopLineStopAssessmentRepo.getQueryResultCount(query);
            }


            logger.info("Found: " + count + " Lane Direction of Travel Assessments");
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Get Signal State Event Assessments", description = "Get Signal State Event Assessments, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/assessments/signal_state_event_assessment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<List<StopLinePassageAssessment>> findSignalStateEventAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<StopLinePassageAssessment> list = new ArrayList<>();
            list.add(MockAssessmentGenerator.getStopLinePassageAssessment());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateEventAssessmentRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalStateEventAssessmentRepo.getQueryResultCount(query);
            logger.info("Returning SignalStateEventAssessment Response with Size: " + count);
            return ResponseEntity.ok(signalStateEventAssessmentRepo.find(query));
        }
    }

    @Operation(summary = "Get Signal State Event Assessment Counts", description = "Get Signal State Event Assessment counts, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/assessments/signal_state_event_assessment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER'))")
    public ResponseEntity<Long> countSignalStateEventAssessment(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateEventAssessmentRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = 0;
            if(fullCount){
                count = signalStateEventAssessmentRepo.getQueryFullCount(query);
            }else{
                count = signalStateEventAssessmentRepo.getQueryResultCount(query);
            }
            logger.info("Found: " + count + " Signal State Event Assessments");
            return ResponseEntity.ok(count);
        }
    }
}