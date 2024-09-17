package us.dot.its.jpo.ode.api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

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

import org.apache.commons.lang3.exception.ExceptionUtils;

@RestController
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

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/active", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<Notification>> findActiveNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning ProcessedMap Response with Size: " + count);
                return ResponseEntity.ok(activeNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/active/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countActiveNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = activeNotificationRepo.getQuery(intersectionID, roadRegulatorID, notificationType, key);
            long count = activeNotificationRepo.getQueryResultCount(query);
            logger.info("Found: " + count + " Active Notifications");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @DeleteMapping(value = "/notifications/active")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('ADMIN'))")
    public @ResponseBody ResponseEntity<String> deleteActiveNotification(@RequestBody String key) {
        Query query = activeNotificationRepo.getQuery(null, null, null, key.replace("\"", ""));

        try {
            long count = activeNotificationRepo.delete(query);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN)
                    .body(count + " records deleted.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/connection_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<ConnectionOfTravelNotification>> findConnectionOfTravelNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning ProcessedMap Response with Size: " + count);
                return ResponseEntity.ok(connectionOfTravelNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/connection_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countConnectionOfTravelNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = connectionOfTravelNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = connectionOfTravelNotificationRepo.getQueryResultCount(query);
           
            logger.info("Found: " + count + " Connection of Travel Notifications");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<IntersectionReferenceAlignmentNotification>> findIntersectionReferenceAlignmentNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning IntersectionReferenceAlignmentNotification Response with Size: " + count);
                return ResponseEntity.ok(intersectionReferenceAlignmentNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/intersection_reference_alignment/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countIntersectionReferenceAlignmentNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = intersectionReferenceAlignmentNotificationRepo.getQuery(intersectionID, startTime, endTime,
                    false);
            long count = intersectionReferenceAlignmentNotificationRepo.getQueryResultCount(query);
            
            logger.info("Found: " + count + " Intersection Reference Alignment");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<LaneDirectionOfTravelNotification>> findLaneDirectionOfTravelNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning LaneDirectionOfTravelNotification Response with Size: " + count);
                return ResponseEntity.ok(laneDirectionOfTravelNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/lane_direction_of_travel/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countLaneDirectionOfTravelNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = laneDirectionOfTravelNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = laneDirectionOfTravelNotificationRepo.getQueryResultCount(query);
            
            logger.info("Found: " + count + " Lane Direction of Travel");
            return ResponseEntity.ok(count);

        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<MapBroadcastRateNotification>> findMapBroadcastRateNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning MapBroadcastRateNotification Response with Size: " + count);
                return ResponseEntity.ok(mapBroadcastRateNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countMapBroadcastRateNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = mapBroadcastRateNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = mapBroadcastRateNotificationRepo.getQueryResultCount(query);
            
            logger.info("Found: " + count + " Map Broadcast Rate Notifications");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/signal_group_alignment_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<SignalGroupAlignmentNotification>> findSignalGroupAlignmentNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning SignalGroupAlignmentNotification Response with Size: " + count);
                return ResponseEntity.ok(signalGroupAlignmentNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/signal_group_alignment_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countSignalGroupAlignmentNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalGroupAlignmentNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = signalGroupAlignmentNotificationRepo.getQueryResultCount(query);
            logger.info("Found: " + count + " Signal Group Alignment Notifications");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/signal_state_conflict_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<SignalStateConflictNotification>> findSignalStateConflictNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning SignalGroupAlignmentNotification Response with Size: " + count);
                return ResponseEntity.ok(signalStateConflictNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/signal_state_conflict_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countSignalStateConflictNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = signalStateConflictNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = signalStateConflictNotificationRepo.getQueryResultCount(query);
            logger.info("Found: " + count + " Signal State Conflict Noficiations");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<SpatBroadcastRateNotification>> findSpatBroadcastRateNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning SpatBroadcastRateNotification Response with Size: " + count);
                return ResponseEntity.ok(spatBroadcastRateNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countSpatBroadcastRateNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = spatBroadcastRateNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = spatBroadcastRateNotificationRepo.getQueryResultCount(query);
            logger.info("Found: " + count + " SPaT Broadcast Rate Notifications");
            return ResponseEntity.ok(count);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/stop_line_stop", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<StopLineStopNotification>> findStopLineStopNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning Stop Line Stop Notification Response with Size: " + count);
                return ResponseEntity.ok(stopLineStopNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/stop_line_stop/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countStopLineStopNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = stopLineStopNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = stopLineStopNotificationRepo.getQueryResultCount(query);
            logger.info("Found: " + count + " Stop Line Stop Notifications");
            return ResponseEntity.ok(count);
        }
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/stop_line_passage", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<StopLinePassageNotification>> findStopLinePassageNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning Stop Line Passage Notification Response with Size: " + count);
                return ResponseEntity.ok(stopLinePassageNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/stop_line_passage/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countStopLinePassageNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = stopLinePassageNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = stopLinePassageNotificationRepo.getQueryResultCount(query);
            logger.info("Found: " + count + " Stop Line Passage Notifications");
            return ResponseEntity.ok(count);
        }
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/time_change_details", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<List<TimeChangeDetailsNotification>> findTimeChangeDetailsNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
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
                logger.info("Returning Time Change Details Notification Response with Size: " + count);
                return ResponseEntity.ok(timeChangeDetailsNotificationRepo.find(query));
            } else {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");
            }
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/time_change_details/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
    public ResponseEntity<Long> countTimeChangeDetailsNotification(
            @RequestParam(name = "intersection_id", required = false) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            Query query = timeChangeDetailsNotificationRepo.getQuery(intersectionID, startTime, endTime, false);
            long count = timeChangeDetailsNotificationRepo.getQueryResultCount(query); 
            logger.info("Found: " + count + " Time Change Detail Notifications");
            return ResponseEntity.ok(count);
        }
    }
}