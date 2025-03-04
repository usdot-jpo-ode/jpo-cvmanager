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

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.EventStateProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
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
import us.dot.its.jpo.ode.api.accessors.events.aggregations.bsmmessagecountprogression.BsmMessageCountProgressionEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.eventstateprogressionevent.EventStateProgressionEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.intersectionreferencealignment.IntersectionReferenceAlignmentEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.mapmessagecountprogression.MapMessageCountProgressionEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.mapminimumdata.MapMinimumDataEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.signalgroupalignment.SignalGroupAlignmentEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.signalstateconflict.SignalStateConflictEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.spatmessagecountprogression.SpatMessageCountProgressionEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.spatminimumdata.SpatMinimumDataEventAggregationRepository;
import us.dot.its.jpo.ode.api.accessors.events.aggregations.timechangedetails.TimeChangeDetailsEventAggregationRepository;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.MinuteCount;
import us.dot.its.jpo.ode.mockdata.MockAggregatedEventGenerator;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;
import us.dot.its.jpo.ode.mockdata.MockIDCountGenerator;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class EventController {

    private final ConnectionOfTravelEventRepository connectionOfTravelEventRepo;
    private final IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;
    private final LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;
    private final SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo;
    private final SignalStateConflictEventRepository signalStateConflictEventRepo;
    private final SignalStateStopEventRepository signalStateStopEventRepo;
    private final SignalStateEventRepository signalStateEventRepo;
    private final TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;
    private final SpatMinimumDataEventRepository spatMinimumDataEventRepo;
    private final MapMinimumDataEventRepository mapMinimumDataEventRepo;
    private final SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;
    private final MapBroadcastRateEventRepository mapBroadcastRateEventRepo;
    private final SpatMessageCountProgressionEventRepository spatMessageCountProgressionEventRepo;
    private final MapMessageCountProgressionEventRepository mapMessageCountProgressionEventRepo;
    private final BsmMessageCountProgressionEventRepository bsmMessageCountProgressionEventRepo;
    private final SpatMinimumDataEventAggregationRepository spatMinimumDataEventAggregationRepo;
    private final MapMinimumDataEventAggregationRepository mapMinimumDataEventAggregationRepo;
    private final IntersectionReferenceAlignmentEventAggregationRepository intersectionReferenceAlignmentEventAggregationRepo;
    private final SignalGroupAlignmentEventAggregationRepository signalGroupAlignmentEventAggregationRepo;
    private final SignalStateConflictEventAggregationRepository signalStateConflictEventAggregationRepo;
    private final TimeChangeDetailsEventAggregationRepository timeChangeDetailsEventAggregationRepo;
    private final EventStateProgressionEventAggregationRepository eventStateProgressionEventAggregationRepo;
    private final BsmMessageCountProgressionEventAggregationRepository bsmMessageCountProgressionEventAggregationRepo;
    private final MapMessageCountProgressionEventAggregationRepository mapMessageCountProgressionEventAggregationRepo;
    private final SpatMessageCountProgressionEventAggregationRepository spatMessageCountProgressionEventAggregationRepo;
    private final BsmEventRepository bsmEventRepo;

    @Value("${maximumResponseSize}")
    int maximumResponseSize;

    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    int MILLISECONDS_PER_MINUTE = 60 * 1000;

    @Autowired
    public EventController(
            ConnectionOfTravelEventRepository connectionOfTravelEventRepo,
            IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo,
            LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo,
            SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo,
            SignalStateConflictEventRepository signalStateConflictEventRepo,
            SignalStateStopEventRepository signalStateStopEventRepo,
            SignalStateEventRepository signalStateEventRepo,
            TimeChangeDetailsEventRepository timeChangeDetailsEventRepo,
            SpatMinimumDataEventRepository spatMinimumDataEventRepo,
            MapMinimumDataEventRepository mapMinimumDataEventRepo,
            SpatBroadcastRateEventRepository spatBroadcastRateEventRepo,
            MapBroadcastRateEventRepository mapBroadcastRateEventRepo,
            SpatMessageCountProgressionEventRepository spatMessageCountProgressionEventRepo,
            MapMessageCountProgressionEventRepository mapMessageCountProgressionEventRepo,
            BsmMessageCountProgressionEventRepository bsmMessageCountProgressionEventRepo,
            BsmEventRepository bsmEventRepo,
            SpatMinimumDataEventAggregationRepository spatMinimumDataEventAggregationRepo,
            MapMinimumDataEventAggregationRepository mapMinimumDataEventAggregationRepo,
            IntersectionReferenceAlignmentEventAggregationRepository intersectionReferenceAlignmentEventAggregationRepo,
            SignalGroupAlignmentEventAggregationRepository signalGroupAlignmentEventAggregationRepo,
            SignalStateConflictEventAggregationRepository signalStateConflictEventAggregationRepo,
            TimeChangeDetailsEventAggregationRepository timeChangeDetailsEventAggregationRepo,
            EventStateProgressionEventAggregationRepository eventStateProgressionEventAggregationRepo,
            BsmMessageCountProgressionEventAggregationRepository bsmMessageCountProgressionEventAggregationRepo,
            MapMessageCountProgressionEventAggregationRepository mapMessageCountProgressionEventAggregationRepo,
            SpatMessageCountProgressionEventAggregationRepository spatMessageCountProgressionEventAggregationRepo) {
        this.connectionOfTravelEventRepo = connectionOfTravelEventRepo;
        this.intersectionReferenceAlignmentEventRepo = intersectionReferenceAlignmentEventRepo;
        this.laneDirectionOfTravelEventRepo = laneDirectionOfTravelEventRepo;
        this.signalGroupAlignmentEventRepo = signalGroupAlignmentEventRepo;
        this.signalStateConflictEventRepo = signalStateConflictEventRepo;
        this.signalStateStopEventRepo = signalStateStopEventRepo;
        this.signalStateEventRepo = signalStateEventRepo;
        this.timeChangeDetailsEventRepo = timeChangeDetailsEventRepo;
        this.spatMinimumDataEventRepo = spatMinimumDataEventRepo;
        this.mapMinimumDataEventRepo = mapMinimumDataEventRepo;
        this.spatBroadcastRateEventRepo = spatBroadcastRateEventRepo;
        this.mapBroadcastRateEventRepo = mapBroadcastRateEventRepo;
        this.spatMessageCountProgressionEventRepo = spatMessageCountProgressionEventRepo;
        this.mapMessageCountProgressionEventRepo = mapMessageCountProgressionEventRepo;
        this.bsmMessageCountProgressionEventRepo = bsmMessageCountProgressionEventRepo;
        this.bsmEventRepo = bsmEventRepo;
        this.spatMinimumDataEventAggregationRepo = spatMinimumDataEventAggregationRepo;
        this.mapMinimumDataEventAggregationRepo = mapMinimumDataEventAggregationRepo;
        this.intersectionReferenceAlignmentEventAggregationRepo = intersectionReferenceAlignmentEventAggregationRepo;
        this.signalGroupAlignmentEventAggregationRepo = signalGroupAlignmentEventAggregationRepo;
        this.signalStateConflictEventAggregationRepo = signalStateConflictEventAggregationRepo;
        this.timeChangeDetailsEventAggregationRepo = timeChangeDetailsEventAggregationRepo;
        this.eventStateProgressionEventAggregationRepo = eventStateProgressionEventAggregationRepo;
        this.bsmMessageCountProgressionEventAggregationRepo = bsmMessageCountProgressionEventAggregationRepo;
        this.mapMessageCountProgressionEventAggregationRepo = mapMessageCountProgressionEventAggregationRepo;
        this.spatMessageCountProgressionEventAggregationRepo = spatMessageCountProgressionEventAggregationRepo;

    }

    @Operation(summary = "Retrieve Intersection Reference Alignment Events", description = "Get Intersection Reference Alignment Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IntersectionReferenceAlignmentEvent>> findIntersectionReferenceAlignmentEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<IntersectionReferenceAlignmentEvent> results = intersectionReferenceAlignmentEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Intersection Reference Alignment Events", description = "Get the count of Intersection Reference Alignment Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/intersection_reference_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countIntersectionReferenceAlignmentEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = intersectionReferenceAlignmentEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = intersectionReferenceAlignmentEventRepo.getQueryFullCount(query);
            } else {
                count = intersectionReferenceAlignmentEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} IntersectionReferenceAlignmentEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Daily Counts of Intersection Reference Alignment Events", description = "Get the daily counts of Intersection Reference Alignment Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<ConnectionOfTravelEvent>> findConnectionOfTravelEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<ConnectionOfTravelEvent> results = connectionOfTravelEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Connection of Travel Events", description = "Get the count of Connection of Travel Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countConnectionOfTravelEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = connectionOfTravelEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = connectionOfTravelEventRepo.getQueryFullCount(query);
            } else {
                count = connectionOfTravelEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} ConnectionOfTravelEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Connection of Travel Events", description = "Get the aggregated daily counts of Connection of Travel Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/connection_of_travel/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailyConnectionOfTravelEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(
                    connectionOfTravelEventRepo.getAggregatedDailyConnectionOfTravelEventCounts(intersectionID,
                            startTime, endTime));
        }
    }

    @Operation(summary = "Retrieve Lane Direction of Travel Events", description = "Get Lane Direction of Travel Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<LaneDirectionOfTravelEvent>> findLaneDirectionOfTravelEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<LaneDirectionOfTravelEvent> results = laneDirectionOfTravelEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Lane Direction of Travel Events", description = "Get the count of Lane Direction of Travel Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countLaneDirectionOfTravelEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = laneDirectionOfTravelEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = laneDirectionOfTravelEventRepo.getQueryFullCount(query);
            } else {
                count = laneDirectionOfTravelEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} LaneDirectionOfTravelEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Lane Direction of Travel Events", description = "Get the aggregated daily counts of Lane Direction of Travel Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/lane_direction_of_travel/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailyLaneDirectionOfTravelEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(
                    laneDirectionOfTravelEventRepo.getAggregatedDailyLaneDirectionOfTravelEventCounts(intersectionID,
                            startTime, endTime));
        }
    }

    @Operation(summary = "Retrieve Signal Group Alignment Events", description = "Get Signal Group Alignment Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/signal_group_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<SignalGroupAlignmentEvent>> findSignalGroupAlignmentEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<SignalGroupAlignmentEvent> results = signalGroupAlignmentEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Signal Group Alignment Events", description = "Get the count of Signal Group Alignment Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/signal_group_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalGroupAlignmentEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalGroupAlignmentEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = signalGroupAlignmentEventRepo.getQueryFullCount(query);
            } else {
                count = signalGroupAlignmentEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SignalGroupAlignmentEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Signal Group Alignment Events", description = "Get the aggregated daily counts of Signal Group Alignment Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/signal_group_alignment/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailySignalGroupAlignmentEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity
                    .ok(signalGroupAlignmentEventRepo.getAggregatedDailySignalGroupAlignmentEventCounts(intersectionID,
                            startTime, endTime));
        }
    }

    @Operation(summary = "Retrieve Signal State Conflict Events", description = "Get Signal State Conflict Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/signal_state_conflict", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<SignalStateConflictEvent>> findSignalStateConflictEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<SignalStateConflictEvent> results = signalStateConflictEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Signal State Conflict Events", description = "Get the count of Signal State Conflict Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/signal_state_conflict/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateConflictEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateConflictEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = signalStateConflictEventRepo.getQueryFullCount(query);
            } else {
                count = signalStateConflictEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SignalStateConflictEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Signal State Conflict Events", description = "Get the aggregated daily counts of Signal State Conflict Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/signal_state_conflict/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailySignalStateConflictEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity.ok(
                    signalStateConflictEventRepo.getAggregatedDailySignalStateConflictEventCounts(intersectionID,
                            startTime, endTime));
        }
    }

    @Operation(summary = "Retrieve Signal State Events", description = "Get Signal State Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/signal_state", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<StopLinePassageEvent>> findSignalStateEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<StopLinePassageEvent> results = signalStateEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Signal State Events", description = "Get the count of Signal State Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/signal_state/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = signalStateEventRepo.getQueryFullCount(query);
            } else {
                count = signalStateEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SignalStateEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Signal State Events", description = "Get the aggregated daily counts of Signal State Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/signal_state/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailySignalStateEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity
                    .ok(signalStateEventRepo.getAggregatedDailySignalStateEventCounts(intersectionID, startTime,
                            endTime));
        }
    }

    @Operation(summary = "Retrieve Signal State Stop Events", description = "Get Signal State Stop Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/signal_state_stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<StopLineStopEvent>> findSignalStateStopEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<StopLineStopEvent> results = signalStateStopEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Signal State Stop Events", description = "Get the count of Signal State Stop Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/signal_state_stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateStopEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateStopEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;
            if (fullCount) {
                count = signalStateStopEventRepo.getQueryFullCount(query);
            } else {
                count = signalStateStopEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SignalStateStopEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Signal State Stop Events", description = "Get the aggregated daily counts of Signal State Stop Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/signal_state_stop/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailySignalStateStopEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity
                    .ok(signalStateStopEventRepo.getAggregatedDailySignalStateStopEventCounts(intersectionID, startTime,
                            endTime));
        }
    }

    @Operation(summary = "Retrieve Time Change Details Events", description = "Get Time Change Details Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/time_change_details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<TimeChangeDetailsEvent>> findTimeChangeDetailsEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<TimeChangeDetailsEvent> results = timeChangeDetailsEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count Time Change Details Events", description = "Get the count of Time Change Details Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/time_change_details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countTimeChangeDetailsEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = timeChangeDetailsEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count;

            if (fullCount) {
                count = timeChangeDetailsEventRepo.getQueryFullCount(query);
            } else {
                count = timeChangeDetailsEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} Time Change Detail Events", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Time Change Details Events", description = "Get the aggregated daily counts of Time Change Details Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/time_change_details/daily_counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getTimeChangeDetailsEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity
                    .ok(timeChangeDetailsEventRepo.getAggregatedDailyTimeChangeDetailsEventCounts(intersectionID,
                            startTime, endTime));
        }
    }

    @Operation(summary = "Retrieve SPaT Minimum Data Events", description = "Get SPaT Minimum Data Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/spat_minimum_data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<SpatMinimumDataEvent>> findSpatMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SpatMinimumDataEvent> list = new ArrayList<>();
            return ResponseEntity.ok(list);
        } else {
            Query query = spatMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            List<SpatMinimumDataEvent> results = spatMinimumDataEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count SPaT Minimum Data Events", description = "Get the count of SPaT Minimum Data Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/spat_minimum_data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count;

            if (fullCount) {
                count = spatMinimumDataEventRepo.getQueryFullCount(query);
            } else {
                count = spatMinimumDataEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SpatMinimumDataEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve MAP Minimum Data Events", description = "Get MAP Minimum Data Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/map_minimum_data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<MapMinimumDataEvent>> findMapMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MapMinimumDataEvent> list = new ArrayList<>();
            return ResponseEntity.ok(list);
        } else {
            Query query = mapMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, latest);
            List<MapMinimumDataEvent> results = mapMinimumDataEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count MAP Minimum Data Events", description = "Get the count of MAP Minimum Data Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/map_minimum_data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count;

            if (fullCount) {
                count = mapMinimumDataEventRepo.getQueryFullCount(query);
            } else {
                count = mapMinimumDataEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} MapMinimumDataEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve MAP Broadcast Rate Events", description = "Get MAP Broadcast Rate Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/map_broadcast_rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<MapBroadcastRateEvent>> findMapBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<MapBroadcastRateEvent> results = mapBroadcastRateEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count MAP Broadcast Rate Events", description = "Get the count of MAP Broadcast Rate Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/map_broadcast_rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapBroadcastRateEventRepo.getQuery(intersectionID, startTime, endTime, false);
            long count;

            if (fullCount) {
                count = mapBroadcastRateEventRepo.getQueryFullCount(query);
            } else {
                count = mapBroadcastRateEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} MapBroadcastRates", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve SPaT Broadcast Rate Events", description = "Get SPaT Broadcast Rate Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/spat_broadcast_rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<SpatBroadcastRateEvent>> findSpatBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<SpatBroadcastRateEvent> results = spatBroadcastRateEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count SPaT Broadcast Rate Events", description = "Get the count of SPaT Broadcast Rate Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/spat_broadcast_rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatBroadcastRateEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if (fullCount) {
                count = spatBroadcastRateEventRepo.getQueryFullCount(query);
            } else {
                count = spatBroadcastRateEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SpatBroadcastRateEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve SPaT Message Count Progression Events", description = "Get SPaT Message Count Progression Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/spat_message_count_progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<SpatMessageCountProgressionEvent>> findSpatMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<SpatMessageCountProgressionEvent> results = spatMessageCountProgressionEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count SPaT Message Count Progression Events", description = "Get the count of SPaT Message Count Progression Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/spat_message_count_progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if (fullCount) {
                count = spatMessageCountProgressionEventRepo.getQueryFullCount(query);
            } else {
                count = spatMessageCountProgressionEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} SpatMessageCountProgressionEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve MAP Message Count Progression Events", description = "Get MAP Message Count Progression Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/map_message_count_progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<MapMessageCountProgressionEvent>> findMapMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<MapMessageCountProgressionEvent> results = mapMessageCountProgressionEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count MAP Message Count Progression Events", description = "Get the count of MAP Message Count Progression Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/map_message_count_progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if (fullCount) {
                count = mapMessageCountProgressionEventRepo.getQueryFullCount(query);
            } else {
                count = mapMessageCountProgressionEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} MapMessageCountProgressionEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve BSM Message Count Progression Events", description = "Get BSM Message Count Progression Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/bsm_message_count_progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<BsmMessageCountProgressionEvent>> findBsmMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<BsmMessageCountProgressionEvent> results = bsmMessageCountProgressionEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count BSM Message Count Progression Events", description = "Get the count of BSM Message Count Progression Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/bsm_message_count_progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countBsmMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = bsmMessageCountProgressionEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if (fullCount) {
                count = bsmMessageCountProgressionEventRepo.getQueryFullCount(query);
            } else {
                count = bsmMessageCountProgressionEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} BsmMessageCountProgressionEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve BSM Events", description = "Get BSM Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/events/bsm_events", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<BsmEvent>> findBsmEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
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
            List<BsmEvent> results = bsmEventRepo.find(query);
            return new ResponseEntity<>(results, new HttpHeaders(),
                    results.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Count BSM Events", description = "Get the count of BSM Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/events/bsm_events/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countBsmEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "full_count", required = false, defaultValue = "true") boolean fullCount,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = bsmEventRepo.getQuery(intersectionID, startTime, endTime, false);

            long count;

            if (fullCount) {
                count = bsmEventRepo.getQueryFullCount(query);
            } else {
                count = bsmEventRepo.getQueryResultCount(query);
            }

            log.debug("Found: {} BsmEvents", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Counts of BSM Events By Minute", description = "Get the aggregated counts of BSM Events over each minute, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/events/bsm_events_by_minute", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<MinuteCount>> getBsmActivityByMinuteInRange(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MinuteCount> list = new ArrayList<>();
            Random rand = new Random();
            for (int i = 0; i < 10; i++) {
                int offset = rand.nextInt((int) (endTime - startTime));
                MinuteCount count = new MinuteCount();
                count.setMinute(((long) Math.round((float) (startTime + offset) / MILLISECONDS_PER_MINUTE))
                        * MILLISECONDS_PER_MINUTE);
                count.setCount(rand.nextInt(10) + 1);
                list.add(count);
            }

            return ResponseEntity.ok(list);
        } else {
            Query query = bsmEventRepo.getQuery(intersectionID, startTime, endTime, latest);

            List<BsmEvent> events = bsmEventRepo.find(query);

            Map<Long, Set<String>> bsmEventMap = new HashMap<>();

            for (BsmEvent event : events) {
                J2735Bsm bsm = ((J2735Bsm) event.getStartingBsm().getPayload().getData());
                long eventStartMinute = Instant
                        .from(formatter.parse(event.getStartingBsm().getMetadata().getOdeReceivedAt())).toEpochMilli()
                        / MILLISECONDS_PER_MINUTE;
                long eventEndMinute = eventStartMinute;

                if (event.getEndingBsm() != null) {
                    eventEndMinute = Instant
                            .from(formatter.parse(event.getEndingBsm().getMetadata().getOdeReceivedAt())).toEpochMilli()
                            / MILLISECONDS_PER_MINUTE;
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
            for (Long key : bsmEventMap.keySet()) {
                MinuteCount count = new MinuteCount();
                count.setMinute(key * MILLISECONDS_PER_MINUTE);
                count.setCount(bsmEventMap.get(key).size());
                outputEvents.add(count);
            }

            return new ResponseEntity<>(outputEvents, new HttpHeaders(),
                    outputEvents.size() == maximumResponseSize ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }
    }

    @Operation(summary = "Find Spat Minimum Data Event Aggregations", description = "Returns a list of Spat Minimum Data Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/spat_minimum_data_event_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SpatMinimumDataEventAggregation>> findSpatMinimumDataEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<SpatMinimumDataEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getSpatMinimumDataEventAggregation());
            Page<SpatMinimumDataEventAggregation> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(spatMinimumDataEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SpatMinimumDataEventAggregation> response = spatMinimumDataEventAggregationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            log.debug("Returning SpatMinimumDataEventAggregation Page with Size: {}", response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Spat Minimum Data Event Aggregations", description = "Returns the count of Spat Minimum Data Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/spat_minimum_data_event_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatMinimumDataEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = spatMinimumDataEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} SpatMinimumDataEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Map Minimum Data Event Aggregations", description = "Returns a list of Map Minimum Data Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/map_minimum_data_event_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<MapMinimumDataEventAggregation>> findMapMinimumDataEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<MapMinimumDataEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getMapMinimumDataEventAggregation());
            Page<MapMinimumDataEventAggregation> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(mapMinimumDataEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<MapMinimumDataEventAggregation> response = mapMinimumDataEventAggregationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            log.debug("Returning MapMinimumDataEventAggregation Page with Size: {}", response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Map Minimum Data Event Aggregations", description = "Returns the count of Map Minimum Data Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/map_minimum_data_event_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapMinimumDataEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = mapMinimumDataEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} MapMinimumDataEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Intersection Reference Alignment Event Aggregations", description = "Returns a list of Intersection Reference Alignment Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/intersection_reference_alignment_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<IntersectionReferenceAlignmentEventAggregation>> findIntersectionReferenceAlignmentEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<IntersectionReferenceAlignmentEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getIntersectionReferenceAlignmentEventAggregation());
            Page<IntersectionReferenceAlignmentEventAggregation> mockPage = new PageImpl<>(mockList,
                    PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(intersectionReferenceAlignmentEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<IntersectionReferenceAlignmentEventAggregation> response = intersectionReferenceAlignmentEventAggregationRepo
                    .find(intersectionID,
                            startTime, endTime, pageable);
            log.debug("Returning IntersectionReferenceAlignmentEventAggregation Page with Size: {}",
                    response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Intersection Reference Alignment Event Aggregations", description = "Returns the count of Intersection Reference Alignment Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/intersection_reference_alignment_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countIntersectionReferenceAlignmentEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = intersectionReferenceAlignmentEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} IntersectionReferenceAlignmentEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Signal Group Alignment Event Aggregations", description = "Returns a list of Signal Group Alignment Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/signal_group_alignment_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SignalGroupAlignmentEventAggregation>> findSignalGroupAlignmentEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<SignalGroupAlignmentEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getSignalGroupAlignmentEventAggregation());
            Page<SignalGroupAlignmentEventAggregation> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(signalGroupAlignmentEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SignalGroupAlignmentEventAggregation> response = signalGroupAlignmentEventAggregationRepo.find(
                    intersectionID,
                    startTime, endTime, pageable);
            log.debug("Returning SignalGroupAlignmentEventAggregation Page with Size: {}",
                    response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Signal Group Alignment Event Aggregations", description = "Returns the count of Signal Group Alignment Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/signal_group_alignment_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalGroupAlignmentEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = signalGroupAlignmentEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} SignalGroupAlignmentEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Signal State Alignment Event Aggregations", description = "Returns a list of Signal State Alignment Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/signal_state_alignment_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SignalStateConflictEventAggregation>> findSignalStateConflictEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<SignalStateConflictEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getSignalStateConflictEventAggregation());
            Page<SignalStateConflictEventAggregation> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(signalStateConflictEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SignalStateConflictEventAggregation> response = signalStateConflictEventAggregationRepo.find(
                    intersectionID,
                    startTime, endTime, pageable);
            log.debug("Returning SignalStateConflictEventAggregation Page with Size: {}", response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Signal State Alignment Event Aggregations", description = "Returns the count of Signal State Alignment Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/signal_state_alignment_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateConflictEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = signalStateConflictEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} SignalStateConflictEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Time Change Details Event Aggregations", description = "Returns a list of Time Change Details Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/time_change_details_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<TimeChangeDetailsEventAggregation>> findTimeChangeDetailsEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<TimeChangeDetailsEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getTimeChangeDetailsEventAggregation());
            Page<TimeChangeDetailsEventAggregation> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(timeChangeDetailsEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<TimeChangeDetailsEventAggregation> response = timeChangeDetailsEventAggregationRepo.find(
                    intersectionID,
                    startTime, endTime, pageable);
            log.debug("Returning TimeChangeDetailsEventAggregation Page with Size: {}", response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Time Change Details Event Aggregations", description = "Returns the count of Time Change Details Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/time_change_details_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countTimeChangeDetailsEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = timeChangeDetailsEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} TimeChangeDetailsEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Event State Progression Event Aggregations", description = "Returns a list of Event State Progression Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/event_state_progression_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<EventStateProgressionEventAggregation>> findEventStateProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<EventStateProgressionEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getEventStateProgressionEventAggregation());
            Page<EventStateProgressionEventAggregation> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(eventStateProgressionEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<EventStateProgressionEventAggregation> response = eventStateProgressionEventAggregationRepo.find(
                    intersectionID,
                    startTime, endTime, pageable);
            log.debug("Returning EventStateProgressionEventAggregation Page with Size: {}",
                    response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Event State Progression Event Aggregations", description = "Returns the count of Event State Progression Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/event_state_progression_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countEventStateProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = eventStateProgressionEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} EventStateProgressionEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Bsm Message Count Progression Event Aggregations", description = "Returns a list of Bsm Message Count Progression Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/bsm_message_count_progression_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<BsmMessageCountProgressionEventAggregation>> findBsmMessageCountProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<BsmMessageCountProgressionEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getBsmMessageCountProgressionEventAggregation());
            Page<BsmMessageCountProgressionEventAggregation> mockPage = new PageImpl<>(mockList,
                    PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(bsmMessageCountProgressionEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<BsmMessageCountProgressionEventAggregation> response = bsmMessageCountProgressionEventAggregationRepo
                    .find(
                            intersectionID,
                            startTime, endTime, pageable);
            log.debug("Returning BsmMessageCountProgressionEventAggregation Page with Size: {}",
                    response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Bsm Message Count Progression Event Aggregations", description = "Returns the count of Bsm Message Count Progression Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/bsm_message_count_progression_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countBsmMessageCountProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = bsmMessageCountProgressionEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} BsmMessageCountProgressionEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Map Message Count Progression Event Aggregations", description = "Returns a list of Map Message Count Progression Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/map_message_count_progression_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<MapMessageCountProgressionEventAggregation>> findMapMessageCountProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<MapMessageCountProgressionEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getMapMessageCountProgressionEventAggregation());
            Page<MapMessageCountProgressionEventAggregation> mockPage = new PageImpl<>(mockList,
                    PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(mapMessageCountProgressionEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<MapMessageCountProgressionEventAggregation> response = mapMessageCountProgressionEventAggregationRepo
                    .find(
                            intersectionID,
                            startTime, endTime, pageable);
            log.debug("Returning MapMessageCountProgressionEventAggregation Page with Size: {}",
                    response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Map Message Count Progression Event Aggregations", description = "Returns the count of Map Message Count Progression Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/map_message_count_progression_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapMessageCountProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = mapMessageCountProgressionEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} MapMessageCountProgressionEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Spat Message Count Progression Event Aggregations", description = "Returns a list of Spat Message Count Progression Event Aggregations, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/events/spat_message_count_progression_aggregation", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SpatMessageCountProgressionEventAggregation>> findSpatMessageCountProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<SpatMessageCountProgressionEventAggregation> mockList = List
                    .of(MockAggregatedEventGenerator.getSpatMessageCountProgressionEventAggregation());
            Page<SpatMessageCountProgressionEventAggregation> mockPage = new PageImpl<>(mockList,
                    PageRequest.of(page, size),
                    mockList.size());
            return ResponseEntity.ok(mockPage);
        } else if (latest) {
            return ResponseEntity.ok(spatMessageCountProgressionEventAggregationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SpatMessageCountProgressionEventAggregation> response = spatMessageCountProgressionEventAggregationRepo
                    .find(
                            intersectionID,
                            startTime, endTime, pageable);
            log.debug("Returning SpatMessageCountProgressionEventAggregation Page with Size: {}",
                    response.getContent().size());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Spat Message Count Progression Event Aggregations", description = "Returns the count of Spat Message Count Progression Event Aggregations, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/events/spat_message_count_progression_aggregation/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatMessageCountProgressionEventAggregation(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            PageRequest pageable = PageRequest.of(page, size);
            long count = spatMessageCountProgressionEventAggregationRepo.count(intersectionID, startTime, endTime,
                    pageable);

            log.debug("Found: {} SpatMessageCountProgressionEventAggregations", count);
            return ResponseEntity.ok(count);
        }
    }

}
