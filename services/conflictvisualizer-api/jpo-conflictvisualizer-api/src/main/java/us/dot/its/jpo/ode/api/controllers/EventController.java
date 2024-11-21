package us.dot.its.jpo.ode.api.controllers;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.events.BsmEvent.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapMinimumDataEvent.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatBroadcastRateEvent.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.MinuteCount;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;
import us.dot.its.jpo.ode.mockdata.MockIDCountGenerator;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;

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
    SpatMinimumDataEventRepository spatMinimumDataEventRepo;

    @Autowired
    MapMinimumDataEventRepository mapMinimumDataEventRepo;

    @Autowired
    SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;

    @Autowired
    MapBroadcastRateEventRepository mapBroadcastRateEventRepo;

    @Autowired
    BsmEventRepository bsmEventRepo;



    @Autowired
    ConflictMonitorApiProperties props;

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    ObjectMapper objectMapper = new ObjectMapper();
    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            Query query = intersectionReferenceAlignmentEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = intersectionReferenceAlignmentEventRepo.getQueryResultCount(query);
            logger.info("Returning IntersectionReferenceAlignmentEvent Response with Size: " + count);
            return ResponseEntity.ok(intersectionReferenceAlignmentEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/intersection_reference_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countIntersectionReferenceAlignmentEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = intersectionReferenceAlignmentEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = intersectionReferenceAlignmentEventRepo.getQueryFullCount(query);
            }else{
                count = intersectionReferenceAlignmentEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Intersection Reference Alignment Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            Query query = connectionOfTravelEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = connectionOfTravelEventRepo.getQueryResultCount(query);
            logger.info("Returning ConnectionOfTravelEvent Response with Size: " + count);
            return ResponseEntity.ok(connectionOfTravelEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countConnectionOfTravelEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = connectionOfTravelEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = connectionOfTravelEventRepo.getQueryFullCount(query);
            }else{
                count = connectionOfTravelEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Connection of Travel Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/connection_of_travel/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailyConnectionOfTravelEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(connectionOfTravelEventRepo.getConnectionOfTravelEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            Query query = laneDirectionOfTravelEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = laneDirectionOfTravelEventRepo.getQueryResultCount(query);
            logger.info("Returning LaneDirectionOfTravelEvent Response with Size: " + count);
            return ResponseEntity.ok(laneDirectionOfTravelEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countLaneDirectionOfTravelEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = laneDirectionOfTravelEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = laneDirectionOfTravelEventRepo.getQueryFullCount(query);
            }else{
                count = laneDirectionOfTravelEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Lane Direction of Travel Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/lane_direction_of_travel/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailyLaneDirectionOfTravelEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(laneDirectionOfTravelEventRepo.getLaneDirectionOfTravelEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_group_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            Query query = signalGroupAlignmentEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalGroupAlignmentEventRepo.getQueryResultCount(query);
            logger.info("Returning LaneDirectionOfTravelEvent Response with Size: " + count);
            return ResponseEntity.ok(signalGroupAlignmentEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_group_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSignalGroupAlignmentEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalGroupAlignmentEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = signalGroupAlignmentEventRepo.getQueryFullCount(query);
            }else{
                count = signalGroupAlignmentEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Signal Group Alignment Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_group_alignment/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailySignalGroupAlignmentEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalGroupAlignmentEventRepo.getSignalGroupAlignmentEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_conflict", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            Query query = signalStateConflictEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalStateConflictEventRepo.getQueryResultCount(query);
            logger.info("Returning SignalStateConflictEvent Response with Size: " + count);
            return ResponseEntity.ok(signalStateConflictEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_conflict/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSignalStateConflictEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateConflictEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = signalStateConflictEventRepo.getQueryFullCount(query);
            }else{
                count = signalStateConflictEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Signal Group Alignment Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_conflict/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailySignalStateConflictEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalStateConflictEventRepo.getSignalStateConflictEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<StopLinePassageEvent>> findSignalStateEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<StopLinePassageEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getStopLinePassageEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalStateEventRepo.getQueryResultCount(query);
            logger.info("Returning SignalStateEvent Response with Size: " + count);
            return ResponseEntity.ok(signalStateEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSignalStateEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = signalStateEventRepo.getQueryFullCount(query);
            }else{
                count = signalStateEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Signal State Count");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailySignalStateEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalStateEventRepo.getSignalStateEventsByDay(intersectionID, startTime, endTime));
        }
    }

    

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<StopLineStopEvent>> findSignalStateStopEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<StopLineStopEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getStopLineStopEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateStopEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalStateStopEventRepo.getQueryResultCount(query);
            logger.info("Returning SignalStateStopEvent Response with Size: " + count);
            return ResponseEntity.ok(signalStateStopEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSignalStateStopEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateStopEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;
            if(fullCount){
                count = signalStateStopEventRepo.getQueryFullCount(query);
            }else{
                count = signalStateStopEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Signal State Stop Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/signal_state_stop/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailySignalStateStopEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalStateStopEventRepo.getSignalStateStopEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/time_change_details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            Query query = timeChangeDetailsEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = timeChangeDetailsEventRepo.getQueryResultCount(query);
            logger.info("Returning TimeChangeDetailsEventRepo Response with Size: " + count);
            return ResponseEntity.ok(timeChangeDetailsEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/time_change_details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countTimeChangeDetailsEvent(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = timeChangeDetailsEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = 0;

            if(fullCount){
                count = timeChangeDetailsEventRepo.getQueryFullCount(query);
            }else{
                count = timeChangeDetailsEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Time Change Detail Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/time_change_details/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getTimeChangeDetailsEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(timeChangeDetailsEventRepo.getTimeChangeDetailsEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/spat_minimum_data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<SpatMinimumDataEvent>> findSpatMinimumDataEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SpatMinimumDataEvent> list = new ArrayList<>();
            return ResponseEntity.ok(list);
        } else {
            Query query = spatMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = spatMinimumDataEventRepo.getQueryResultCount(query);
            logger.info("Returning SpatMinimumdataEvent Response with Size: " + count);
            return ResponseEntity.ok(spatMinimumDataEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/spat_minimum_data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSpatMinimumDataEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = 0;

            if(fullCount){
                count = spatMinimumDataEventRepo.getQueryFullCount(query);
            }else{
                count = spatMinimumDataEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Spat Minimum Data Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/map_minimum_data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<MapMinimumDataEvent>> findMapMinimumDataEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MapMinimumDataEvent> list = new ArrayList<>();
            return ResponseEntity.ok(list);
        } else {
            Query query = mapMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = mapMinimumDataEventRepo.getQueryResultCount(query);
            logger.info("Returning MapMinimumDataEventRepo Response with Size: " + count);
            return ResponseEntity.ok(mapMinimumDataEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/map_minimum_data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countMapMinimumDataEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = 0;

            if(fullCount){
                count = mapMinimumDataEventRepo.getQueryFullCount(query);
            }else{
                count = mapMinimumDataEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Map Minimum Data Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/map_broadcast_rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<MapBroadcastRateEvent>> findMapBroadcastRateEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MapBroadcastRateEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getMapBroadcastRateEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = mapBroadcastRateEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = mapBroadcastRateEventRepo.getQueryResultCount(query);

            logger.info("Returning MapMinimumDataEventRepo Response with Size: " + count);
            return ResponseEntity.ok(mapBroadcastRateEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/map_broadcast_rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countMapBroadcastRateEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapBroadcastRateEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = 0;
            
            if(fullCount){
                count = mapBroadcastRateEventRepo.getQueryFullCount(query);
            }else{
                count = mapBroadcastRateEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Map Broadcast Rates");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/spat_broadcast_rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<SpatBroadcastRateEvent>> findSpatBroadcastRateEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SpatBroadcastRateEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSpatBroadcastRateEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = spatBroadcastRateEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = spatBroadcastRateEventRepo.getQueryResultCount(query);
            logger.info("Returning SpatMinimumDataEventRepo Response with Size: " + count);
            System.out.println("Spat Broadcast Data Event");
            return ResponseEntity.ok(spatBroadcastRateEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/spat_broadcast_rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSpatBroadcastRateEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatBroadcastRateEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;

            if(fullCount){
                count = spatBroadcastRateEventRepo.getQueryFullCount(query);
            }else{
                count = spatBroadcastRateEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " Spat Broadcast Rate Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/bsm_events", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<BsmEvent>> findBsmEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<BsmEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getBsmEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = bsmEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = bsmEventRepo.getQueryResultCount(query);
            logger.info("Returning Bsm Event Repo Response with Size: " + count);
            return ResponseEntity.ok(bsmEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/bsm_events/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countBsmEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = bsmEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count = 0;

            if(fullCount){
                count = bsmEventRepo.getQueryFullCount(query);
            }else{
                count = bsmEventRepo.getQueryResultCount(query);
            }

            logger.info("Found: " + count + " BSM Events");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/bsm_events_by_minute", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<MinuteCount>> getBsmActivityByMinuteInRange(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MinuteCount> list = new ArrayList<>();
            Random rand = new Random();
            for(int i=0; i< 10; i++){
                int offset = rand.nextInt((int)(endTime - startTime));
                MinuteCount count = new MinuteCount();
                count.setMinute(((long)Math.round((startTime + offset) / 60000)) * 60000L);
                count.setCount(rand.nextInt(10) + 1);
                list.add(count);
            }
            
            return ResponseEntity.ok(list);
        } else {
            Query query = bsmEventRepo.getQuery(intersectionID, startTime, endTime, latest);

            List<BsmEvent> events = bsmEventRepo.find(query);

            Map<Long, Set<String>> bsmEventMap = new HashMap<>();

            for(BsmEvent event: events){
                J2735Bsm bsm = ((J2735Bsm)event.getStartingBsm().getPayload().getData());
                Long eventStartMinute = Instant.from(formatter.parse(event.getStartingBsm().getMetadata().getOdeReceivedAt())).toEpochMilli() / (60 * 1000);
                Long eventEndMinute = eventStartMinute;
                
                if(event.getEndingBsm() != null){
                    eventEndMinute = Instant.from(formatter.parse(event.getEndingBsm().getMetadata().getOdeReceivedAt())).toEpochMilli() / (60 * 1000);
                }

                if(eventStartMinute != null && eventEndMinute != null){
                    for (Long i = eventStartMinute; i<= eventEndMinute; i++){
                        String bsmID = bsm.getCoreData().getId();
                        if(bsmEventMap.get(i) != null){
                            bsmEventMap.get(i).add(bsmID);
                        }else{
                            Set<String> newSet = new HashSet<>();
                            newSet.add(bsmID);
                            bsmEventMap.put(i, newSet);
                        }
                    }
                }
            }

            List<MinuteCount> outputEvents = new ArrayList<>();
            for(Long key: bsmEventMap.keySet()){
                MinuteCount count = new MinuteCount();
                count.setMinute(key * 60000);
                count.setCount(bsmEventMap.get(key).size());
                outputEvents.add(count);
            }

            return ResponseEntity.ok(outputEvents);
        }
    }

    

    
}
