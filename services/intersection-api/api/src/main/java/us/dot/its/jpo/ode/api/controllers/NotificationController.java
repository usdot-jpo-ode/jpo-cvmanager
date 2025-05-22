package us.dot.its.jpo.ode.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLineStopNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification.ConnectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification.IntersectionReferenceAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo.LaneDirectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.MapBroadcastRateNotification.MapBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalGroupAlignmentNotificationRepo.SignalGroupAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalStateConflictNotification.SignalStateConflictNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SpatBroadcastRateNotification.SpatBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.StopLinePassageNotification.StopLinePassageNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.StopLineStopNotification.StopLineStopNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.TimeChangeDetailsNotification.TimeChangeDetailsNotificationRepository;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class NotificationController {

    private final IntersectionReferenceAlignmentNotificationRepository intersectionReferenceAlignmentNotificationRepo;
    private final LaneDirectionOfTravelNotificationRepository laneDirectionOfTravelNotificationRepo;
    private final MapBroadcastRateNotificationRepository mapBroadcastRateNotificationRepo;
    private final SignalGroupAlignmentNotificationRepository signalGroupAlignmentNotificationRepo;
    private final SignalStateConflictNotificationRepository signalStateConflictNotificationRepo;
    private final SpatBroadcastRateNotificationRepository spatBroadcastRateNotificationRepo;
    private final ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo;
    private final TimeChangeDetailsNotificationRepository timeChangeDetailsNotificationRepo;
    private final StopLineStopNotificationRepository stopLineStopNotificationRepo;
    private final StopLinePassageNotificationRepository stopLinePassageNotificationRepo;
    private final ActiveNotificationRepository activeNotificationRepo;

    @Autowired
    public NotificationController(
            IntersectionReferenceAlignmentNotificationRepository intersectionReferenceAlignmentNotificationRepo,
            LaneDirectionOfTravelNotificationRepository laneDirectionOfTravelNotificationRepo,
            MapBroadcastRateNotificationRepository mapBroadcastRateNotificationRepo,
            SignalGroupAlignmentNotificationRepository signalGroupAlignmentNotificationRepo,
            SignalStateConflictNotificationRepository signalStateConflictNotificationRepo,
            SpatBroadcastRateNotificationRepository spatBroadcastRateNotificationRepo,
            ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo,
            TimeChangeDetailsNotificationRepository timeChangeDetailsNotificationRepo,
            StopLineStopNotificationRepository stopLineStopNotificationRepo,
            StopLinePassageNotificationRepository stopLinePassageNotificationRepo,
            ActiveNotificationRepository activeNotificationRepo) {

        this.intersectionReferenceAlignmentNotificationRepo = intersectionReferenceAlignmentNotificationRepo;
        this.laneDirectionOfTravelNotificationRepo = laneDirectionOfTravelNotificationRepo;
        this.mapBroadcastRateNotificationRepo = mapBroadcastRateNotificationRepo;
        this.signalGroupAlignmentNotificationRepo = signalGroupAlignmentNotificationRepo;
        this.signalStateConflictNotificationRepo = signalStateConflictNotificationRepo;
        this.spatBroadcastRateNotificationRepo = spatBroadcastRateNotificationRepo;
        this.connectionOfTravelNotificationRepo = connectionOfTravelNotificationRepo;
        this.timeChangeDetailsNotificationRepo = timeChangeDetailsNotificationRepo;
        this.stopLineStopNotificationRepo = stopLineStopNotificationRepo;
        this.stopLinePassageNotificationRepo = stopLinePassageNotificationRepo;
        this.activeNotificationRepo = activeNotificationRepo;

    }

    @Operation(summary = "Find Active Notifications", description = "Returns a list of Active Notifications, filtered by intersection ID, start time, end time, and key")
    @RequestMapping(value = "/notifications/active", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<Notification>> findActiveNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<Notification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getConnectionOfTravelNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        // Retrieve a paginated result from the repository
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> response = activeNotificationRepo.find(intersectionID, notificationType, key, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Count Active Notifications", description = "Returns the count of Active Notifications, filtered by intersection ID, start time, end time, and key")
    @RequestMapping(value = "/notifications/active/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Long> countActiveNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = activeNotificationRepo.count(intersectionID, notificationType, key);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Delete Active Notification", description = "Deletes a specific Active Notification by key")
    @DeleteMapping(value = "/notifications/active", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('OPERATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or OPERATOR role"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public @ResponseBody ResponseEntity<String> deleteActiveNotification(@RequestBody String key) {
        try {
            long count = activeNotificationRepo.delete(key.replace("\"", ""));
            if (count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Active Notification with key " + key + " not found");
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete Active Notification: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "Find Connection of Travel Notifications", description = "Returns a list of Connection of Travel Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<ConnectionOfTravelNotification>> findConnectionOfTravelNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            // Mock response for test data
            List<ConnectionOfTravelNotification> list = List
                    .of(MockNotificationGenerator.getConnectionOfTravelNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        } else if (latest) {
            return ResponseEntity.ok(connectionOfTravelNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<ConnectionOfTravelNotification> response = connectionOfTravelNotificationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Connection of Travel Notifications", description = "Returns the count of Connection of Travel Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countConnectionOfTravelNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = connectionOfTravelNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Intersection Reference Alignment Notifications", description = "Returns a list of Intersection Reference Alignment Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<IntersectionReferenceAlignmentNotification>> findIntersectionReferenceAlignmentNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<IntersectionReferenceAlignmentNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getIntersectionReferenceAlignmentNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(intersectionReferenceAlignmentNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<IntersectionReferenceAlignmentNotification> response = intersectionReferenceAlignmentNotificationRepo
                    .find(intersectionID, startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Intersection Reference Alignment Notifications", description = "Returns the count of Intersection Reference Alignment Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/intersection_reference_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countIntersectionReferenceAlignmentNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = intersectionReferenceAlignmentNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Lane Direction of Travel Notifications", description = "Returns a list of Lane Direction of Travel Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<LaneDirectionOfTravelNotification>> findLaneDirectionOfTravelNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<LaneDirectionOfTravelNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getLaneDirectionOfTravelNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(laneDirectionOfTravelNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<LaneDirectionOfTravelNotification> response = laneDirectionOfTravelNotificationRepo
                    .find(intersectionID, startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Lane Direction of Travel Notifications", description = "Returns the count of Lane Direction of Travel Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countLaneDirectionOfTravelNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = laneDirectionOfTravelNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Map Broadcast Rate Notifications", description = "Returns a list of Map Broadcast Rate Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<MapBroadcastRateNotification>> findMapBroadcastRateNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<MapBroadcastRateNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getMapBroadcastRateNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(mapBroadcastRateNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<MapBroadcastRateNotification> response = mapBroadcastRateNotificationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Map Broadcast Rate Notifications", description = "Returns the count of Map Broadcast Rate Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapBroadcastRateNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = mapBroadcastRateNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Signal Group Alignment Notifications", description = "Returns a list of Signal Group Alignment Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/signal_group_alignment_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SignalGroupAlignmentNotification>> findSignalGroupAlignmentNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SignalGroupAlignmentNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getSignalGroupAlignmentNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(signalGroupAlignmentNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SignalGroupAlignmentNotification> response = signalGroupAlignmentNotificationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Signal Group Alignment Notifications", description = "Returns the count of Signal Group Alignment Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/signal_group_alignment_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalGroupAlignmentNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = signalGroupAlignmentNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Signal State Conflict Notifications", description = "Returns a list of Signal State Conflict Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/signal_state_conflict_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SignalStateConflictNotification>> findSignalStateConflictNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SignalStateConflictNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getSignalStateConflictNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(signalStateConflictNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SignalStateConflictNotification> response = signalStateConflictNotificationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Signal State Conflict Notifications", description = "Returns the count of Signal State Conflict Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/signal_state_conflict_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateConflictNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = signalStateConflictNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Spat Broadcast Rate Notifications", description = "Returns a list of Spat Broadcast Rate Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<SpatBroadcastRateNotification>> findSpatBroadcastRateNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SpatBroadcastRateNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getSpatBroadcastRateNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(spatBroadcastRateNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<SpatBroadcastRateNotification> response = spatBroadcastRateNotificationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Spat Broadcast Rate Notifications", description = "Returns the count of Spat Broadcast Rate Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatBroadcastRateNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = spatBroadcastRateNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Stop Line Stop Notifications", description = "Returns a list of Stop Line Stop Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/stop_line_stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<StopLineStopNotification>> findStopLineStopNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<StopLineStopNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getStopLineStopNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(stopLineStopNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<StopLineStopNotification> response = stopLineStopNotificationRepo.find(intersectionID, startTime,
                    endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Stop Line Stop Notifications", description = "Returns the count of Stop Line Stop Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/stop_line_stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countStopLineStopNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = stopLineStopNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Stop Line Passage Notifications", description = "Returns a list of Stop Line Passage Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/stop_line_passage", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<StopLinePassageNotification>> findStopLinePassageNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<StopLinePassageNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getStopLinePassageNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(stopLinePassageNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<StopLinePassageNotification> response = stopLinePassageNotificationRepo.find(intersectionID, startTime,
                    endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Stop Line Passage Notifications", description = "Returns the count of Stop Line Passage Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/stop_line_passage/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countStopLinePassageNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = stopLinePassageNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Time Change Details Notifications", description = "Returns a list of Time Change Details Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/time_change_details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<TimeChangeDetailsNotification>> findTimeChangeDetailsNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<TimeChangeDetailsNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getTimeChangeDetailsNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        if (latest) {
            return ResponseEntity.ok(timeChangeDetailsNotificationRepo.findLatest(intersectionID,
                    startTime, endTime));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<TimeChangeDetailsNotification> response = timeChangeDetailsNotificationRepo.find(intersectionID,
                    startTime, endTime, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Count Time Change Details Notifications", description = "Returns the count of Time Change Details Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/time_change_details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countTimeChangeDetailsNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = timeChangeDetailsNotificationRepo.count(intersectionID, startTime, endTime);

            return ResponseEntity.ok(count);
        }
    }
}