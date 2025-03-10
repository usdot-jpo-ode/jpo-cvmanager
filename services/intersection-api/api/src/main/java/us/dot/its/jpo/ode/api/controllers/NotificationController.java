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
import org.springframework.data.mongodb.core.query.Query;
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
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
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

    @Autowired
    IntersectionReferenceAlignmentNotificationRepository intersectionReferenceAlignmentNotificationRepo;

    @Autowired
    LaneDirectionOfTravelNotificationRepository laneDirectionOfTravelNotificationRepo;

    @Autowired
    MapBroadcastRateNotificationRepository mapBroadcastRateNotificationRepo;

    @Autowired
    SignalGroupAlignmentNotificationRepository signalGroupAlignmentNotificationRepo;

    @Autowired
    SignalStateConflictNotificationRepository signalStateConflictNotificationRepo;

    @Autowired
    SpatBroadcastRateNotificationRepository spatBroadcastRateNotificationRepo;

    @Autowired
    ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo;

    @Autowired
    TimeChangeDetailsNotificationRepository timeChangeDetailsNotificationRepo;

    @Autowired
    StopLineStopNotificationRepository stopLineStopNotificationRepo;

    @Autowired
    StopLinePassageNotificationRepository stopLinePassageNotificationRepo;

    @Autowired
    ActiveNotificationRepository activeNotificationRepo;
    @Autowired
    ConflictMonitorApiProperties props;

    @Operation(summary = "Find Active Notifications", description = "Returns a list of Active Notifications, filtered by intersection ID, start time, end time, and key")
    @RequestMapping(value = "/notifications/active", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<Notification>> findActiveNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<Notification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getConnectionOfTravelNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = activeNotificationRepo.getQuery(intersectionID, roadRegulatorID, notificationType, key);
            long count = activeNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning ActiveNotification Response with Size: {}", count);
                return ResponseEntity.ok(activeNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Active Notifications", description = "Returns the count of Active Notifications, filtered by intersection ID, start time, end time, and key")
    @RequestMapping(value = "/notifications/active/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countActiveNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = activeNotificationRepo.getQuery(intersectionID, roadRegulatorID, notificationType, key);
            long count = activeNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} Active Notifications", count);
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
        Query query = activeNotificationRepo.getQuery(null, null, null, key.replace("\"", ""));

        try {
            activeNotificationRepo.delete(query);
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
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<ConnectionOfTravelNotification>> findConnectionOfTravelNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<ConnectionOfTravelNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getConnectionOfTravelNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = connectionOfTravelNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = connectionOfTravelNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning ConnectionOfTravelNotification Response with Size: {}", count);
                return ResponseEntity.ok(connectionOfTravelNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Connection of Travel Notifications", description = "Returns the count of Connection of Travel Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countConnectionOfTravelNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = connectionOfTravelNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = connectionOfTravelNotificationRepo.getQueryResultCount(query);

            log.debug("Found: {} ConnectionOfTravelNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Intersection Reference Alignment Notifications", description = "Returns a list of Intersection Reference Alignment Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<IntersectionReferenceAlignmentNotification>> findIntersectionReferenceAlignmentNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<IntersectionReferenceAlignmentNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getIntersectionReferenceAlignmentNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = intersectionReferenceAlignmentNotificationRepo.getQuery(intersectionID, startTime, endTime,
                    latest);
            long count = intersectionReferenceAlignmentNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning IntersectionReferenceAlignmentNotification Response with Size: {}", count);
                return ResponseEntity.ok(intersectionReferenceAlignmentNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Intersection Reference Alignment Notifications", description = "Returns the count of Intersection Reference Alignment Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/intersection_reference_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countIntersectionReferenceAlignmentNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = intersectionReferenceAlignmentNotificationRepo.getQuery(intersectionID, startTime, endTime,
                    false);
            long count = intersectionReferenceAlignmentNotificationRepo.getQueryResultCount(query);

            log.debug("Found: {} IntersectionReferenceAlignmentNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Lane Direction of Travel Notifications", description = "Returns a list of Lane Direction of Travel Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<LaneDirectionOfTravelNotification>> findLaneDirectionOfTravelNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<LaneDirectionOfTravelNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getLaneDirectionOfTravelNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = laneDirectionOfTravelNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = laneDirectionOfTravelNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning LaneDirectionOfTravelNotification Response with Size: {}", count);
                return ResponseEntity.ok(laneDirectionOfTravelNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Lane Direction of Travel Notifications", description = "Returns the count of Lane Direction of Travel Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countLaneDirectionOfTravelNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = laneDirectionOfTravelNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = laneDirectionOfTravelNotificationRepo.getQueryResultCount(query);

            log.debug("Found: {} LaneDirectionOfTravelNotifications", count);
            return ResponseEntity.ok(count);

        }
    }

    @Operation(summary = "Find Map Broadcast Rate Notifications", description = "Returns a list of Map Broadcast Rate Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<MapBroadcastRateNotification>> findMapBroadcastRateNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<MapBroadcastRateNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getMapBroadcastRateNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = mapBroadcastRateNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = mapBroadcastRateNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning MapBroadcastRateNotification Response with Size: {}", count);
                return ResponseEntity.ok(mapBroadcastRateNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Map Broadcast Rate Notifications", description = "Returns the count of Map Broadcast Rate Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countMapBroadcastRateNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapBroadcastRateNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = mapBroadcastRateNotificationRepo.getQueryResultCount(query);

            log.debug("Found: {} MapBroadcastRateNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Signal Group Alignment Notifications", description = "Returns a list of Signal Group Alignment Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/signal_group_alignment_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<SignalGroupAlignmentNotification>> findSignalGroupAlignmentNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<SignalGroupAlignmentNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getSignalGroupAlignmentNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalGroupAlignmentNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalGroupAlignmentNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning SignalGroupAlignmentNotification Response with Size: {}", count);
                return ResponseEntity.ok(signalGroupAlignmentNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Signal Group Alignment Notifications", description = "Returns the count of Signal Group Alignment Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/signal_group_alignment_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalGroupAlignmentNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalGroupAlignmentNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = signalGroupAlignmentNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} SignalGroupAlignmentNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Signal State Conflict Notifications", description = "Returns a list of Signal State Conflict Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/signal_state_conflict_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<SignalStateConflictNotification>> findSignalStateConflictNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SignalStateConflictNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getSignalStateConflictNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = signalStateConflictNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = signalStateConflictNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning SignalStateConflictNotification Response with Size: {}", count);
                return ResponseEntity.ok(signalStateConflictNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Signal State Conflict Notifications", description = "Returns the count of Signal State Conflict Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/signal_state_conflict_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSignalStateConflictNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateConflictNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = signalStateConflictNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} SignalStateConflictNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Spat Broadcast Rate Notifications", description = "Returns a list of Spat Broadcast Rate Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<SpatBroadcastRateNotification>> findSpatBroadcastRateNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            List<SpatBroadcastRateNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getSpatBroadcastRateNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = spatBroadcastRateNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = spatBroadcastRateNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning SpatBroadcastRateNotification Response with Size: {}", count);
                return ResponseEntity.ok(spatBroadcastRateNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Spat Broadcast Rate Notifications", description = "Returns the count of Spat Broadcast Rate Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countSpatBroadcastRateNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatBroadcastRateNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = spatBroadcastRateNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} SpatBroadcastRateNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Stop Line Stop Notifications", description = "Returns a list of Stop Line Stop Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/stop_line_stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<StopLineStopNotification>> findStopLineStopNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<StopLineStopNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getStopLineStopNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = stopLineStopNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = stopLineStopNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning StopLineStopNotification Response with Size: {}", count);
                return ResponseEntity.ok(stopLineStopNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Stop Line Stop Notifications", description = "Returns the count of Stop Line Stop Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/stop_line_stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countStopLineStopNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = stopLineStopNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = stopLineStopNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} StopLineStopNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Stop Line Passage Notifications", description = "Returns a list of Stop Line Passage Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/stop_line_passage", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<StopLinePassageNotification>> findStopLinePassageNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<StopLinePassageNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getStopLinePassageNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = stopLinePassageNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = stopLinePassageNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning StopLinePassageNotification Response with Size: {}", count);
                return ResponseEntity.ok(stopLinePassageNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Stop Line Passage Notifications", description = "Returns the count of Stop Line Passage Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/stop_line_passage/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countStopLinePassageNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = stopLinePassageNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = stopLinePassageNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} StopLinePassageNotifications", count);
            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Find Time Change Details Notifications", description = "Returns a list of Time Change Details Notifications, filtered by intersection ID, start time, end time, and latest. The latest parameter will return the most recent message satisfying the query.")
    @RequestMapping(value = "/notifications/time_change_details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large - The requested query has more results than allowed by server. Please reduce the query bounds and try again.")
    })
    public ResponseEntity<List<TimeChangeDetailsNotification>> findTimeChangeDetailsNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<TimeChangeDetailsNotification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getTimeChangeDetailsNotification());
            return ResponseEntity.ok(list);
        } else {
            Query query = timeChangeDetailsNotificationRepo.getQuery(intersectionID, startTime, endTime, latest);
            long count = timeChangeDetailsNotificationRepo.getQueryResultCount(query);
            if (count <= props.getMaximumResponseSize()) {
                log.debug("Returning TimeChangeDetailsNotification Response with Size: {}", count);
                return ResponseEntity.ok(timeChangeDetailsNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @Operation(summary = "Count Time Change Details Notifications", description = "Returns the count of Time Change Details Notifications, filtered by intersection ID, start time, end time")
    @RequestMapping(value = "/notifications/time_change_details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public ResponseEntity<Long> countTimeChangeDetailsNotification(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = timeChangeDetailsNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = timeChangeDetailsNotificationRepo.getQueryResultCount(query);
            log.debug("Found: {} TimeChangeDetailNotifications", count);
            return ResponseEntity.ok(count);
        }
    }
}