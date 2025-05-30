package us.dot.its.jpo.ode.api.controllers.data;

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
import us.dot.its.jpo.ode.api.accessors.events.bsm_event.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.bsm_message_count_progression_event.BsmMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.connection_of_travel_event.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.intersection_reference_alignment_event.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.lane_direction_of_travel_event.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_broadcast_rate_event.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_message_count_progression_event.MapMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_minimum_data_event.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_group_alignment_event.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_state_conflict_event.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_broadcast_rate_event.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_message_count_progression_event.SpatMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_minimum_data_event.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_passage_event.StopLinePassageEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_stop_event.StopLineStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.time_change_details_event.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.MinuteCount;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;
import us.dot.its.jpo.ode.mockdata.MockIDCountGenerator;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/data/cm-events")
public class CmEventController {

    private final ConnectionOfTravelEventRepository connectionOfTravelEventRepo;
    private final IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;
    private final LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;
    private final SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo;
    private final SignalStateConflictEventRepository signalStateConflictEventRepo;
    private final StopLineStopEventRepository stopLineStopEventRepo;
    private final StopLinePassageEventRepository stopLinePassageEventRepo;
    private final TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;
    private final SpatMinimumDataEventRepository spatMinimumDataEventRepo;
    private final MapMinimumDataEventRepository mapMinimumDataEventRepo;
    private final SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;
    private final MapBroadcastRateEventRepository mapBroadcastRateEventRepo;
    private final SpatMessageCountProgressionEventRepository spatMessageCountProgressionEventRepo;
    private final MapMessageCountProgressionEventRepository mapMessageCountProgressionEventRepo;
    private final BsmMessageCountProgressionEventRepository bsmMessageCountProgressionEventRepo;
    private final BsmEventRepository bsmEventRepo;

    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    int MILLISECONDS_PER_MINUTE = 60 * 1000;

    @Autowired
    public CmEventController(
            ConnectionOfTravelEventRepository connectionOfTravelEventRepo,
            IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo,
            LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo,
            SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo,
            SignalStateConflictEventRepository signalStateConflictEventRepo,
            StopLineStopEventRepository stopLineStopEventRepo,
            StopLinePassageEventRepository stopLinePassageEventRepo,
            TimeChangeDetailsEventRepository timeChangeDetailsEventRepo,
            SpatMinimumDataEventRepository spatMinimumDataEventRepo,
            MapMinimumDataEventRepository mapMinimumDataEventRepo,
            SpatBroadcastRateEventRepository spatBroadcastRateEventRepo,
            MapBroadcastRateEventRepository mapBroadcastRateEventRepo,
            SpatMessageCountProgressionEventRepository spatMessageCountProgressionEventRepo,
            MapMessageCountProgressionEventRepository mapMessageCountProgressionEventRepo,
            BsmMessageCountProgressionEventRepository bsmMessageCountProgressionEventRepo,
            BsmEventRepository bsmEventRepo) {
        this.connectionOfTravelEventRepo = connectionOfTravelEventRepo;
        this.intersectionReferenceAlignmentEventRepo = intersectionReferenceAlignmentEventRepo;
        this.laneDirectionOfTravelEventRepo = laneDirectionOfTravelEventRepo;
        this.signalGroupAlignmentEventRepo = signalGroupAlignmentEventRepo;
        this.signalStateConflictEventRepo = signalStateConflictEventRepo;
        this.stopLineStopEventRepo = stopLineStopEventRepo;
        this.stopLinePassageEventRepo = stopLinePassageEventRepo;
        this.timeChangeDetailsEventRepo = timeChangeDetailsEventRepo;
        this.spatMinimumDataEventRepo = spatMinimumDataEventRepo;
        this.mapMinimumDataEventRepo = mapMinimumDataEventRepo;
        this.spatBroadcastRateEventRepo = spatBroadcastRateEventRepo;
        this.mapBroadcastRateEventRepo = mapBroadcastRateEventRepo;
        this.spatMessageCountProgressionEventRepo = spatMessageCountProgressionEventRepo;
        this.mapMessageCountProgressionEventRepo = mapMessageCountProgressionEventRepo;
        this.bsmMessageCountProgressionEventRepo = bsmMessageCountProgressionEventRepo;
        this.bsmEventRepo = bsmEventRepo;
    }

    @Operation(summary = "Retrieve Intersection Reference Alignment Events", description = "Get Intersection Reference Alignment Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/intersection-reference-alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<IntersectionReferenceAlignmentEvent>> findIntersectionReferenceAlignmentEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,

            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<IntersectionReferenceAlignmentEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getIntersectionReferenceAlignmentEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(intersectionReferenceAlignmentEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<IntersectionReferenceAlignmentEvent> response = intersectionReferenceAlignmentEventRepo
                    .find(intersectionID, startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Intersection Reference Alignment Events", description = "Get the count of Intersection Reference Alignment Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/intersection-reference-alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countIntersectionReferenceAlignmentEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = intersectionReferenceAlignmentEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Daily Counts of Intersection Reference Alignment Events", description = "Get the daily counts of Intersection Reference Alignment Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/connection-of-travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<ConnectionOfTravelEvent>> findConnectionOfTravelEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<ConnectionOfTravelEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getConnectionOfTravelEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(connectionOfTravelEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<ConnectionOfTravelEvent> response = connectionOfTravelEventRepo.find(intersectionID, startTime,
                    endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Connection of Travel Events", description = "Get the count of Connection of Travel Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/connection-of-travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countConnectionOfTravelEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = connectionOfTravelEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Connection of Travel Events", description = "Get the aggregated daily counts of Connection of Travel Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/connection-of-travel/daily-counts", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/lane-direction-of-travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<LaneDirectionOfTravelEvent>> findLaneDirectionOfTravelEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<LaneDirectionOfTravelEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getLaneDirectionOfTravelEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(laneDirectionOfTravelEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<LaneDirectionOfTravelEvent> response = laneDirectionOfTravelEventRepo.find(intersectionID, startTime,
                    endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Lane Direction of Travel Events", description = "Get the count of Lane Direction of Travel Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/lane-direction-of-travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countLaneDirectionOfTravelEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = laneDirectionOfTravelEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Lane Direction of Travel Events", description = "Get the aggregated daily counts of Lane Direction of Travel Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/lane-direction-of-travel/daily-counts", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/signal-group-alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<SignalGroupAlignmentEvent>> findSignalGroupAlignmentEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SignalGroupAlignmentEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSignalGroupAlignmentEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(signalGroupAlignmentEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SignalGroupAlignmentEvent> response = signalGroupAlignmentEventRepo.find(intersectionID, startTime,
                    endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Signal Group Alignment Events", description = "Get the count of Signal Group Alignment Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/signal-group-alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalGroupAlignmentEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = signalGroupAlignmentEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Signal Group Alignment Events", description = "Get the aggregated daily counts of Signal Group Alignment Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/signal-group-alignment/daily-counts", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/signal-state-conflict", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<SignalStateConflictEvent>> findSignalStateConflictEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SignalStateConflictEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSignalStateConflictEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(signalStateConflictEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SignalStateConflictEvent> response = signalStateConflictEventRepo.find(intersectionID, startTime,
                    endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Signal State Conflict Events", description = "Get the count of Signal State Conflict Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/signal-state-conflict/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateConflictEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = signalStateConflictEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Signal State Conflict Events", description = "Get the aggregated daily counts of Signal State Conflict Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/signal-state-conflict/daily-counts", method = RequestMethod.GET, produces = "application/json")
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

    @Operation(summary = "Retrieve Stop Line Passage Events", description = "Get Stop Line Passage Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/stop-line-passage", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<StopLinePassageEvent>> findStopLinePassageEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<StopLinePassageEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getStopLinePassageEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(stopLinePassageEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<StopLinePassageEvent> response = stopLinePassageEventRepo.find(intersectionID, startTime, endTime,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Stop Line Passage Events", description = "Get the count of Stop Line Passage Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/stop-line-passage/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })

    public ResponseEntity<Long> countStopLinePassageEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = stopLinePassageEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Stop Line Passage Events", description = "Get the aggregated daily counts of Stop Line Passage Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/stop-line-passage/daily-counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailyStopLinePassageEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity
                    .ok(stopLinePassageEventRepo.getAggregatedDailyStopLinePassageEventCounts(intersectionID, startTime,
                            endTime));
        }
    }

    @Operation(summary = "Retrieve Stop Line Stop Events", description = "Get Stop Line Stop Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/stop-line-stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })

    public ResponseEntity<Page<StopLineStopEvent>> findStopLineStopEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<StopLineStopEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getStopLineStopEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(stopLineStopEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<StopLineStopEvent> response = stopLineStopEventRepo.find(intersectionID, startTime, endTime,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Stop Line Stop Events", description = "Get the count of Stop Line Stop Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/stop-line-stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })

    public ResponseEntity<Long> countStopLineStopEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = stopLineStopEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Stop Line Stop Events", description = "Get the aggregated daily counts of Stop Line Stop Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/stop-line-stop/daily-counts", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<List<IDCount>> getDailyStopLineStopEventCounts(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis") Long startTime,
            @RequestParam(name = "end_time_utc_millis") Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(MockIDCountGenerator.getDateIDCounts());
        } else {
            return ResponseEntity
                    .ok(stopLineStopEventRepo.getAggregatedDailyStopLineStopEventCounts(intersectionID, startTime,
                            endTime));
        }
    }

    @Operation(summary = "Retrieve Time Change Details Events", description = "Get Time Change Details Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/time-change-details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<TimeChangeDetailsEvent>> findTimeChangeDetailsEvent(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<TimeChangeDetailsEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getTimeChangeDetailsEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(timeChangeDetailsEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<TimeChangeDetailsEvent> response = timeChangeDetailsEventRepo.find(intersectionID, startTime, endTime,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Time Change Details Events", description = "Get the count of Time Change Details Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/time-change-details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countTimeChangeDetailsEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = timeChangeDetailsEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Daily Counts of Time Change Details Events", description = "Get the aggregated daily counts of Time Change Details Events, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/time-change-details/daily-counts", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/spat-minimum-data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<SpatMinimumDataEvent>> findSpatMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SpatMinimumDataEvent> list = new ArrayList<>();
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(spatMinimumDataEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SpatMinimumDataEvent> response = spatMinimumDataEventRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count SPaT Minimum Data Events", description = "Get the count of SPaT Minimum Data Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/spat-minimum-data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = spatMinimumDataEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve MAP Minimum Data Events", description = "Get MAP Minimum Data Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/map-minimum-data", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<MapMinimumDataEvent>> findMapMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MapMinimumDataEvent> list = new ArrayList<>();
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(mapMinimumDataEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<MapMinimumDataEvent> response = mapMinimumDataEventRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count MAP Minimum Data Events", description = "Get the count of MAP Minimum Data Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/map-minimum-data/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapMinimumDataEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = mapMinimumDataEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve MAP Broadcast Rate Events", description = "Get MAP Broadcast Rate Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/map-broadcast-rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<MapBroadcastRateEvent>> findMapBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<MapBroadcastRateEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getMapBroadcastRateEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(mapBroadcastRateEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<MapBroadcastRateEvent> response = mapBroadcastRateEventRepo.find(intersectionID, startTime, endTime,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count MAP Broadcast Rate Events", description = "Get the count of MAP Broadcast Rate Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/map-broadcast-rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = mapBroadcastRateEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve SPaT Broadcast Rate Events", description = "Get SPaT Broadcast Rate Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/spat-broadcast-rate", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<SpatBroadcastRateEvent>> findSpatBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SpatBroadcastRateEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSpatBroadcastRateEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(spatBroadcastRateEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SpatBroadcastRateEvent> response = spatBroadcastRateEventRepo.find(intersectionID, startTime, endTime,
                    pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count SPaT Broadcast Rate Events", description = "Get the count of SPaT Broadcast Rate Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/spat-broadcast-rate/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatBroadcastRateEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = spatBroadcastRateEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve SPaT Message Count Progression Events", description = "Get SPaT Message Count Progression Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/spat-message-count-progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<SpatMessageCountProgressionEvent>> findSpatMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SpatMessageCountProgressionEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getSpatMessageCountProgressionEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(spatMessageCountProgressionEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SpatMessageCountProgressionEvent> response = spatMessageCountProgressionEventRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count SPaT Message Count Progression Events", description = "Get the count of SPaT Message Count Progression Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/spat-message-count-progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = spatMessageCountProgressionEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve MAP Message Count Progression Events", description = "Get MAP Message Count Progression Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/map-message-count-progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<MapMessageCountProgressionEvent>> findMapMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<MapMessageCountProgressionEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getMapMessageCountProgressionEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(mapMessageCountProgressionEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<MapMessageCountProgressionEvent> response = mapMessageCountProgressionEventRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count MAP Message Count Progression Events", description = "Get the count of MAP Message Count Progression Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/map-message-count-progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = mapMessageCountProgressionEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve BSM Message Count Progression Events", description = "Get BSM Message Count Progression Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/bsm-message-count-progression", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<BsmMessageCountProgressionEvent>> findBsmMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<BsmMessageCountProgressionEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getBsmMessageCountProgressionEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(bsmMessageCountProgressionEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<BsmMessageCountProgressionEvent> response = bsmMessageCountProgressionEventRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count BSM Message Count Progression Events", description = "Get the count of BSM Message Count Progression Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/bsm-message-count-progression/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countBsmMessageCountProgressionEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = bsmMessageCountProgressionEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve BSM Events", description = "Get BSM Events, filtered by intersection ID, start time, and end time. The latest flag will only return the latest message satisfying the query.")
    @RequestMapping(value = "/bsm-events", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<BsmEvent>> findBsmEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<BsmEvent> list = new ArrayList<>();
            list.add(MockEventGenerator.getBsmEvent());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(bsmEventRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<BsmEvent> response = bsmEventRepo.find(intersectionID, startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count BSM Events", description = "Get the count of BSM Events, filtered by intersection ID, start time, and end time. The full count flag will disable the MongoDB default response limit")
    @RequestMapping(value = "/bsm-events/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countBsmEvents(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = bsmEventRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Retrieve Aggregated Counts of BSM Events By Minute", description = "Get the aggregated counts of BSM Events over each minute, filtered by intersection ID, start time, and end time.")
    @RequestMapping(value = "/bsm-events-by-minute", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Page<MinuteCount>> getBsmActivityByMinuteInRange(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
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

            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        Page<BsmEvent> pagedEvents;

        if (latest) {
            pagedEvents = bsmEventRepo.findLatest(intersectionID, startTime, endTime);
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            pagedEvents = bsmEventRepo.find(intersectionID, startTime, endTime, pageable);
        }

        List<BsmEvent> events = pagedEvents.getContent();

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
        return ResponseEntity
                .ok(new PageImpl<>(outputEvents, PageRequest.of(page, size), pagedEvents.getTotalElements()));
    }
}
