package us.dot.its.jpo.ode.api.controllers;

import java.time.Instant;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.accessors.events.BsmEvent.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.BsmMessageCountProgressionEventRepository.BsmMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapMessageCountProgressionEventRepository.MapMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapMinimumDataEvent.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatBroadcastRateEvent.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatMessageCountProgressionEvent.SpatMessageCountProgressionEventRepository;
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
    SpatMessageCountProgressionEventRepository spatMessageCountProgressionEventRepo;

    @Autowired
    MapMessageCountProgressionEventRepository mapMessageCountProgressionEventRepo;

    @Autowired
    BsmMessageCountProgressionEventRepository bsmMessageCountProgressionEventRepo;

    @Autowired
    BsmEventRepository bsmEventRepo;

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    @RequestMapping(value = "/events/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning IntersectionReferenceAlignmentEvent Response with Size: {}", count);
            return ResponseEntity.ok(intersectionReferenceAlignmentEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/intersection_reference_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = intersectionReferenceAlignmentEventRepo.getQueryFullCount(query);
            }else{
                count = intersectionReferenceAlignmentEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} IntersectionReferenceAlignmentEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning ConnectionOfTravelEvent Response with Size: {}", count);
            return ResponseEntity.ok(connectionOfTravelEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = connectionOfTravelEventRepo.getQueryFullCount(query);
            }else{
                count = connectionOfTravelEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} ConnectionOfTravelEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/connection_of_travel/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getDailyConnectionOfTravelEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(connectionOfTravelEventRepo.getConnectionOfTravelEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @RequestMapping(value = "/events/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning LaneDirectionOfTravelEvent Response with Size: {}", count);
            return ResponseEntity.ok(laneDirectionOfTravelEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = laneDirectionOfTravelEventRepo.getQueryFullCount(query);
            }else{
                count = laneDirectionOfTravelEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} LaneDirectionOfTravelEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/lane_direction_of_travel/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getDailyLaneDirectionOfTravelEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(laneDirectionOfTravelEventRepo.getLaneDirectionOfTravelEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @RequestMapping(value = "/events/signal_group_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning SignalGroupAlignmentEvent Response with Size: {}", count);
            return ResponseEntity.ok(signalGroupAlignmentEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/signal_group_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = signalGroupAlignmentEventRepo.getQueryFullCount(query);
            }else{
                count = signalGroupAlignmentEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SignalGroupAlignmentEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/signal_group_alignment/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getDailySignalGroupAlignmentEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalGroupAlignmentEventRepo.getSignalGroupAlignmentEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @RequestMapping(value = "/events/signal_state_conflict", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning SignalStateConflictEvent Response with Size: {}", count);
            return ResponseEntity.ok(signalStateConflictEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/signal_state_conflict/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = signalStateConflictEventRepo.getQueryFullCount(query);
            }else{
                count = signalStateConflictEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SignalStateConflictEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/signal_state_conflict/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getDailySignalStateConflictEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalStateConflictEventRepo.getSignalStateConflictEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @RequestMapping(value = "/events/signal_state", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning SignalStateEvent Response with Size: {}", count);
            return ResponseEntity.ok(signalStateEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/signal_state/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = signalStateEventRepo.getQueryFullCount(query);
            }else{
                count = signalStateEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SignalStateEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/signal_state/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getDailySignalStateEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalStateEventRepo.getSignalStateEventsByDay(intersectionID, startTime, endTime));
        }
    }

    

    @RequestMapping(value = "/events/signal_state_stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning SignalStateStopEvent Response with Size: {}", count);
            return ResponseEntity.ok(signalStateStopEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/signal_state_stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;
            if(fullCount){
                count = signalStateStopEventRepo.getQueryFullCount(query);
            }else{
                count = signalStateStopEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SignalStateStopEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/signal_state_stop/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getDailySignalStateStopEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(signalStateStopEventRepo.getSignalStateStopEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @RequestMapping(value = "/events/time_change_details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning TimeChangeDetailsEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(timeChangeDetailsEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/time_change_details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            long count;

            if(fullCount){
                count = timeChangeDetailsEventRepo.getQueryFullCount(query);
            }else{
                count = timeChangeDetailsEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} Time Change Detail Events", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/time_change_details/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public ResponseEntity<List<IDCount>> getTimeChangeDetailsEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(timeChangeDetailsEventRepo.getTimeChangeDetailsEventsByDay(intersectionID, startTime, endTime));
        }
    }

    @RequestMapping(value = "/events/spat_minimum_data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning SpatMinimumDataEvent Response with Size: {}", count);
            return ResponseEntity.ok(spatMinimumDataEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/spat_minimum_data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            long count;

            if(fullCount){
                count = spatMinimumDataEventRepo.getQueryFullCount(query);
            }else{
                count = spatMinimumDataEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SpatMinimumDataEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/map_minimum_data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning MapMinimumDataEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(mapMinimumDataEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/map_minimum_data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            long count;

            if(fullCount){
                count = mapMinimumDataEventRepo.getQueryFullCount(query);
            }else{
                count = mapMinimumDataEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} MapMinimumDataEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/map_broadcast_rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            logger.debug("Returning MapBroadcastRateEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(mapBroadcastRateEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/map_broadcast_rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            long count;
            
            if(fullCount){
                count = mapBroadcastRateEventRepo.getQueryFullCount(query);
            }else{
                count = mapBroadcastRateEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} MapBroadcastRates", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/spat_broadcast_rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning SpatBroadcastRateEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(spatBroadcastRateEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/spat_broadcast_rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;

            if(fullCount){
                count = spatBroadcastRateEventRepo.getQueryFullCount(query);
            }else{
                count = spatBroadcastRateEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SpatBroadcastRateEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/spat_message_count_progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<SpatMessageCountProgressionEvent>> findSpatMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SpatMessageCountProgressionEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSpatMessageCountProgressionEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = spatMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = spatMessageCountProgressionEventRepo.getQueryResultCount(query);
            logger.debug("Returning SpatMessageCountProgressionEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(spatMessageCountProgressionEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/spat_message_count_progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countSpatMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if(fullCount){
                count = spatMessageCountProgressionEventRepo.getQueryFullCount(query);
            }else{
                count = spatMessageCountProgressionEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} SpatMessageCountProgressionEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/map_message_count_progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<MapMessageCountProgressionEvent>> findMapMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MapMessageCountProgressionEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getMapMessageCountProgressionEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = mapMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = mapMessageCountProgressionEventRepo.getQueryResultCount(query);
            logger.debug("Returning MapMessageCountProgressionEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(mapMessageCountProgressionEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/map_message_count_progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countMapMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if(fullCount){
                count = mapMessageCountProgressionEventRepo.getQueryFullCount(query);
            }else{
                count = mapMessageCountProgressionEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} MapMessageCountProgressionEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/bsm_message_count_progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<BsmMessageCountProgressionEvent>> findBsmMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<BsmMessageCountProgressionEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getBsmMessageCountProgressionEvent());
            return ResponseEntity.ok(list);
        } else {
            Query query = bsmMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = bsmMessageCountProgressionEventRepo.getQueryResultCount(query);
            logger.debug("Returning BsmMinimumDataEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(bsmMessageCountProgressionEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/bsm_message_count_progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Long> countBsmMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = bsmMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if(fullCount){
                count = bsmMessageCountProgressionEventRepo.getQueryFullCount(query);
            }else{
                count = bsmMessageCountProgressionEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} BsmMessageCountProgressionEvents", count);
            return ResponseEntity.ok(count);
        }
    }



    @RequestMapping(value = "/events/bsm_events", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
            logger.debug("Returning BsmEventRepo Response with Size: {}", count);
            return ResponseEntity.ok(bsmEventRepo.find(query));
        }
    }

    @RequestMapping(value = "/events/bsm_events/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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

            long count;

            if(fullCount){
                count = bsmEventRepo.getQueryFullCount(query);
            }else{
                count = bsmEventRepo.getQueryResultCount(query);
            }

            logger.debug("Found: {} BsmEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @RequestMapping(value = "/events/bsm_events_by_minute", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
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
                count.setMinute(((long)Math.round((float) (startTime + offset) / 60000)) * 60000L);
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
                long eventStartMinute = Instant.from(formatter.parse(event.getStartingBsm().getMetadata().getOdeReceivedAt())).toEpochMilli() / (60 * 1000);
                long eventEndMinute = eventStartMinute;
                
                if(event.getEndingBsm() != null){
                    eventEndMinute = Instant.from(formatter.parse(event.getEndingBsm().getMetadata().getOdeReceivedAt())).toEpochMilli() / (60 * 1000);
                }

                for (Long i = eventStartMinute; i <= eventEndMinute; i++) {
                    String bsmID = bsm.getCoreData().getId();
                    if (bsmEventMap.get(i) != null) {
                        bsmEventMap.get(i).add(bsmID);
                    } else {
                        Set<String> newSet = new HashSet<>();
                        newSet.add(bsmID);
                        bsmEventMap.put(i, newSet);
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
