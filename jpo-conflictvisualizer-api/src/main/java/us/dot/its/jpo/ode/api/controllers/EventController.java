package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.ode.api.Properties;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;

@RestController
public class EventController {

    @Autowired
    ConnectionOfTravelEventRepository connectionOfTravelEventRepo;

    @Autowired
    IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;

    @Autowired
    LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;

    @Autowired
    SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo;

    @Autowired
    SignalStateConflictEventRepository signalStateConflictEventRepo;

    @Autowired
    SignalStateStopEventRepository signalStateStopEventRepo;

    @Autowired
    SignalStateEventRepository signalStateEventRepo;

    @Autowired
    TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;

    @Autowired
    Properties props;

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<IntersectionReferenceAlignmentEvent>> findIntersectionReferenceAlignmentEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<IntersectionReferenceAlignmentEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getIntersectionReferenceAlignmentEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = intersectionReferenceAlignmentEventRepo.getQuery(null, startTime, endTime, latest);
            long count = intersectionReferenceAlignmentEventRepo.getQueryResultCount(query);
            System.out.println("Count" + count);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning IntersectionReferenceAlignmentEvent Response with Size: " + count);
                return ResponseEntity.ok(intersectionReferenceAlignmentEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<ConnectionOfTravelEvent>> findConnectionOfTravelEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<ConnectionOfTravelEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getConnectionOfTravelEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = connectionOfTravelEventRepo.getQuery(null, startTime, endTime, latest);
            long count = connectionOfTravelEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning ConnectionOfTravelEvent Response with Size: " + count);
                return ResponseEntity.ok(connectionOfTravelEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<LaneDirectionOfTravelEvent>> findLaneDirectionOfTravelEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<LaneDirectionOfTravelEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getLaneDirectionOfTravelEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = laneDirectionOfTravelEventRepo.getQuery(null, startTime, endTime, latest);
            long count = laneDirectionOfTravelEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning LaneDirectionOfTravelEvent Response with Size: " + count);
                return ResponseEntity.ok(laneDirectionOfTravelEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_group_alignment", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SignalGroupAlignmentEvent>> findSignalGroupAlignmentEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SignalGroupAlignmentEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSignalGroupAlignmentEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalGroupAlignmentEventRepo.getQuery(null, startTime, endTime, latest);
            long count = signalGroupAlignmentEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning LaneDirectionOfTravelEvent Response with Size: " + count);
                return ResponseEntity.ok(signalGroupAlignmentEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_conflict", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SignalStateConflictEvent>> findSignalStateConflictEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SignalStateConflictEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSignalStateConflictEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateConflictEventRepo.getQuery(null, startTime, endTime, latest);
            long count = signalStateConflictEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning SignalStateConflictEvent Response with Size: " + count);
                return ResponseEntity.ok(signalStateConflictEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SignalStateEvent>> findSignalStateEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SignalStateEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSignalStateEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateEventRepo.getQuery(null, startTime, endTime, latest);
            long count = signalStateEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning SignalStateEvent Response with Size: " + count);
                return ResponseEntity.ok(signalStateEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_stop", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SignalStateStopEvent>> findSignalStateStopEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SignalStateStopEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSignalStateStopEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateStopEventRepo.getQuery(null, startTime, endTime, latest);
            long count = signalStateStopEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning SignalStateStopEvent Response with Size: " + count);
                return ResponseEntity.ok(signalStateStopEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/time_change_details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<TimeChangeDetailsEvent>> findTimeChangeDetailsEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<TimeChangeDetailsEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getTimeChangeDetailsEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = timeChangeDetailsEventRepo.getQuery(null, startTime, endTime, latest);
            long count = timeChangeDetailsEventRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                logger.info("Returning TimeChangeDetailsEventRepo Response with Size: " + count);
                return ResponseEntity.ok(timeChangeDetailsEventRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }
}
