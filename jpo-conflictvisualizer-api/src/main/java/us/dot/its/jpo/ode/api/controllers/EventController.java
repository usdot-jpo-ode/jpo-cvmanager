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
import org.springframework.security.access.prepost.PreAuthorize;
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
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
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
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;
import us.dot.its.jpo.ode.mockdata.MockIDCountGenerator;

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
    ConflictMonitorApiProperties props;

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    ObjectMapper objectMapper = new ObjectMapper();

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
    @RequestMapping(value = "/events/signal_group_alignment/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<IDCount>> getDailySignalGroupAlignmentEventCounts(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) Long endTime,
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
    @RequestMapping(value = "/events/spat_minimum_data_event", method = RequestMethod.GET, produces = "application/json")
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
            logger.info("Returning SpatTimeChangeDetails Response with Size: " + count);
            return ResponseEntity.ok(spatMinimumDataEventRepo.find(query));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/events/map_minimum_data_event", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/events/map_broadcast_rate_event", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/events/spat_broadcast_rate_event", method = RequestMethod.GET, produces = "application/json")
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
            return ResponseEntity.ok(spatBroadcastRateEventRepo.find(query));
        }
    }

    

    
}
