package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
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
import us.dot.its.jpo.ode.api.controllers.data.NotificationController;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class NotificationTest {

        private final NotificationController controller;

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

        @MockBean
        PermissionService permissionService;

        @Autowired
        public NotificationTest(NotificationController controller) {
                this.controller = controller;
        }

        @Test
        public void testConnectionOfTravelNotification() {
                ConnectionOfTravelNotification notification = MockNotificationGenerator
                                .getConnectionOfTravelNotification();

                List<ConnectionOfTravelNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(connectionOfTravelNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<ConnectionOfTravelNotification>> result = controller
                                .findConnectionOfTravelNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testIntersectionReferenceAlignmentNotification() {
                IntersectionReferenceAlignmentNotification notification = MockNotificationGenerator
                                .getIntersectionReferenceAlignmentNotification();

                List<IntersectionReferenceAlignmentNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(intersectionReferenceAlignmentNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<IntersectionReferenceAlignmentNotification>> result = controller
                                .findIntersectionReferenceAlignmentNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testLaneDirectionOfTravelNotification() {
                LaneDirectionOfTravelNotification notification = MockNotificationGenerator
                                .getLaneDirectionOfTravelNotification();

                List<LaneDirectionOfTravelNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(laneDirectionOfTravelNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<LaneDirectionOfTravelNotification>> result = controller
                                .findLaneDirectionOfTravelNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testMapBroadcastRateNotification() {
                MapBroadcastRateNotification notification = MockNotificationGenerator.getMapBroadcastRateNotification();

                List<MapBroadcastRateNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(mapBroadcastRateNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<MapBroadcastRateNotification>> result = controller
                                .findMapBroadcastRateNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testSignalGroupAlignmentNotification() {
                SignalGroupAlignmentNotification notification = MockNotificationGenerator
                                .getSignalGroupAlignmentNotification();

                List<SignalGroupAlignmentNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(signalGroupAlignmentNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<SignalGroupAlignmentNotification>> result = controller
                                .findSignalGroupAlignmentNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testSignalStateConflictNotification() {
                SignalStateConflictNotification notification = MockNotificationGenerator
                                .getSignalStateConflictNotification();

                List<SignalStateConflictNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(signalStateConflictNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<SignalStateConflictNotification>> result = controller
                                .findSignalStateConflictNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testSpatBroadcastRateNotification() {
                SpatBroadcastRateNotification notification = MockNotificationGenerator
                                .getSpatBroadcastRateNotification();

                List<SpatBroadcastRateNotification> notifications = new ArrayList<>();
                notifications.add(notification);

                when(permissionService.hasIntersection(notification.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(spatBroadcastRateNotificationRepo.find(notification.getIntersectionID(),
                                notification.getNotificationGeneratedAt() - 1,
                                notification.getNotificationGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<SpatBroadcastRateNotification>> result = controller
                                .findSpatBroadcastRateNotifications(
                                                notification.getIntersectionID(),
                                                notification.getNotificationGeneratedAt() - 1,
                                                notification.getNotificationGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

        @Test
        public void testActiveNotification() {
                List<Integer> allowedInteresections = new ArrayList<>();
                allowedInteresections.add(null);

                when(permissionService.hasIntersection(null, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                SpatBroadcastRateNotification spatBroadcastRateNotification = MockNotificationGenerator
                                .getSpatBroadcastRateNotification();
                SignalStateConflictNotification signalStateConflictNotification = MockNotificationGenerator
                                .getSignalStateConflictNotification();
                SignalGroupAlignmentNotification signalGroupAlignmentNotification = MockNotificationGenerator
                                .getSignalGroupAlignmentNotification();
                MapBroadcastRateNotification mapBroadcastRateNotification = MockNotificationGenerator
                                .getMapBroadcastRateNotification();
                LaneDirectionOfTravelNotification laneDirectionOfTravelNotification = MockNotificationGenerator
                                .getLaneDirectionOfTravelNotification();
                ConnectionOfTravelNotification connectionOfTravelNotification = MockNotificationGenerator
                                .getConnectionOfTravelNotification();

                List<Notification> notifications = new ArrayList<>();
                notifications.add(spatBroadcastRateNotification);
                notifications.add(signalStateConflictNotification);
                notifications.add(signalGroupAlignmentNotification);
                notifications.add(mapBroadcastRateNotification);
                notifications.add(laneDirectionOfTravelNotification);
                notifications.add(connectionOfTravelNotification);

                PageRequest page = PageRequest.of(1, 1);
                when(activeNotificationRepo.find(null,
                                null,
                                null, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(notifications, page, 1L));

                ResponseEntity<Page<Notification>> result = controller
                                .findActiveNotifications(
                                                null, null, null, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(notifications);
        }

}
