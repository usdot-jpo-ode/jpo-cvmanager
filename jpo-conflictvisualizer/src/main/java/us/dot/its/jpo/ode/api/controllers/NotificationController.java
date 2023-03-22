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
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.Properties;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification.ConnectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification.IntersectionReferenceAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo.LaneDirectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.MapBroadcastRateNotification.MapBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalGroupAlignmentNotificationRepo.SignalGroupAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalStateConflictNotification.SignalStateConflictNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SpatBroadcastRateNotification.SpatBroadcastRateNotificationRepository;
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
    ActiveNotificationRepository activeNotificationRepo;
    @Autowired
    Properties props;

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/notifications/active", method = RequestMethod.GET, produces = "application/json")
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
    @DeleteMapping(value = "/notifications/active")
    public @ResponseBody ResponseEntity<String> deleteActiveNotification(@RequestBody String key) {
        Query query = activeNotificationRepo.getQuery(null, null, null, key);

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
    @RequestMapping(value = "/notifications/intersection_reference_alignment", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/notifications/lane_direction_of_travel", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/notifications/map_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/notifications/signal_group_alignment_notification", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/notifications/signal_state_conflict_notification", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/notifications/spat_broadcast_rate_notification", method = RequestMethod.GET, produces = "application/json")
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
}