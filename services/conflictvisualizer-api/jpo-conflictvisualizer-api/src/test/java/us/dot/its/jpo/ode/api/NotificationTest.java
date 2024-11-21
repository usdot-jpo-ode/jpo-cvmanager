package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
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
import us.dot.its.jpo.ode.api.controllers.NotificationController;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
public class NotificationTest {

    @Autowired
    NotificationController controller;

    @MockBean
    IntersectionReferenceAlignmentNotificationRepository intersectionReferenceAlignmentNotificationRepo;

    @MockBean
    LaneDirectionOfTravelNotificationRepository laneDirectionOfTravelNotificationRepo;

    @MockBean
    MapBroadcastRateNotificationRepository mapBroadcastRateNotificationRepo;

    @MockBean
    SignalGroupAlignmentNotificationRepository signalGroupAlignmentNotificationRepo;

    @MockBean
    SignalStateConflictNotificationRepository signalStateConflictNotificationRepo;

    @MockBean
    SpatBroadcastRateNotificationRepository spatBroadcastRateNotificationRepo;

    @MockBean
    ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo;

    @MockBean
    ActiveNotificationRepository activeNotificationRepo;

    @Test
    public void testConnectionOfTravelNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        ConnectionOfTravelNotification notification = MockNotificationGenerator.getConnectionOfTravelNotification();

        List<ConnectionOfTravelNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = connectionOfTravelNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(connectionOfTravelNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<ConnectionOfTravelNotification>> result = controller
                .findConnectionOfTravelNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testIntersectionReferenceAlignmentNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        IntersectionReferenceAlignmentNotification notification = MockNotificationGenerator.getIntersectionReferenceAlignmentNotification();

        List<IntersectionReferenceAlignmentNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = intersectionReferenceAlignmentNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(intersectionReferenceAlignmentNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<IntersectionReferenceAlignmentNotification>> result = controller
                .findIntersectionReferenceAlignmentNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testLaneDirectionOfTravelNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        LaneDirectionOfTravelNotification notification = MockNotificationGenerator.getLaneDirectionOfTravelNotification();

        List<LaneDirectionOfTravelNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = laneDirectionOfTravelNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(laneDirectionOfTravelNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<LaneDirectionOfTravelNotification>> result = controller
                .findLaneDirectionOfTravelNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testMapBroadcastRateNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        MapBroadcastRateNotification notification = MockNotificationGenerator.getMapBroadcastRateNotification();

        List<MapBroadcastRateNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = mapBroadcastRateNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(mapBroadcastRateNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<MapBroadcastRateNotification>> result = controller
                .findMapBroadcastRateNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testSignalGroupAlignmentNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        SignalGroupAlignmentNotification notification = MockNotificationGenerator.getSignalGroupAlignmentNotification();

        List<SignalGroupAlignmentNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = signalGroupAlignmentNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(signalGroupAlignmentNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<SignalGroupAlignmentNotification>> result = controller
                .findSignalGroupAlignmentNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testSignalStateConflictNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        SignalStateConflictNotification notification = MockNotificationGenerator.getSignalStateConflictNotification();

        List<SignalStateConflictNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = signalStateConflictNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(signalStateConflictNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<SignalStateConflictNotification>> result = controller
                .findSignalStateConflictNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testSpatBroadcastRateNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        SpatBroadcastRateNotification notification = MockNotificationGenerator.getSpatBroadcastRateNotification();

        List<SpatBroadcastRateNotification> notifications = new ArrayList<>();

        notifications.add(notification);

        Query query = spatBroadcastRateNotificationRepo.getQuery(notification.getIntersectionID(),
                notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true);
        when(spatBroadcastRateNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<SpatBroadcastRateNotification>> result = controller
                .findSpatBroadcastRateNotification(notification.getIntersectionID(), notification.getNotificationGeneratedAt() - 1, notification.getNotificationGeneratedAt() + 1, true, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    @Test
    public void testActiveNotification() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        SpatBroadcastRateNotification spatBroadcastRateNotification = MockNotificationGenerator.getSpatBroadcastRateNotification();
        SignalStateConflictNotification signalStateConflictNotification = MockNotificationGenerator.getSignalStateConflictNotification();
        SignalGroupAlignmentNotification signalGroupAlignmentNotification = MockNotificationGenerator.getSignalGroupAlignmentNotification();
        MapBroadcastRateNotification mapBroadcastRateNotification = MockNotificationGenerator.getMapBroadcastRateNotification();
        LaneDirectionOfTravelNotification laneDirectionOfTravelNotification = MockNotificationGenerator.getLaneDirectionOfTravelNotification();
        ConnectionOfTravelNotification connectionOfTravelNotification = MockNotificationGenerator.getConnectionOfTravelNotification();
        

        List<Notification> notifications = new ArrayList<>();
        notifications.add(spatBroadcastRateNotification);
        notifications.add(signalStateConflictNotification);
        notifications.add(signalGroupAlignmentNotification);
        notifications.add(mapBroadcastRateNotification);
        notifications.add(laneDirectionOfTravelNotification);
        notifications.add(connectionOfTravelNotification);

        Query query = activeNotificationRepo.getQuery(null, null, null, null);
        when(activeNotificationRepo.find(query)).thenReturn(notifications);

        ResponseEntity<List<Notification>> result = controller
                .findActiveNotification(null,null,null,null, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(notifications);
    }

    

}

