package us.dot.its.jpo.ode.api.controllers.intersections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.active_notification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class ActiveNotificationControllerTest {

    private final ActiveNotificationController controller;

    @MockitoBean
    ActiveNotificationRepository activeNotificationRepo;

    @MockitoBean
    PermissionService permissionService;

    @Autowired
    public ActiveNotificationControllerTest(ActiveNotificationController controller) {
        this.controller = controller;
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

        @Test
        void testFindActiveNotificationWithTestData() {
                Notification event = MockNotificationGenerator.getConnectionOfTravelNotification();

                List<Notification> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<Notification>> response = controller
                                .findActiveNotifications(event.getIntersectionID(), null, null,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindActiveNotificationsWithPagination() {
                Notification event = MockNotificationGenerator.getConnectionOfTravelNotification();

                List<Notification> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                Page<Notification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(activeNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<Notification>> response = controller
                                .findActiveNotifications(event.getIntersectionID(), null, null,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(activeNotificationRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountActiveNotificationsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countActiveNotifications(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountActiveNotifications() {
                Integer intersectionID = 1;
                String notificationType = "notification_type";
                String key = "key";
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(activeNotificationRepo.count(intersectionID, notificationType, key))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countActiveNotifications(intersectionID,
                                notificationType, key, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(activeNotificationRepo, times(1)).count(intersectionID, notificationType, key);
        }

        @Test
        public void testDeleteActiveNotifications() {
                String key = "key";

                when(permissionService.hasRole("OPERATOR")).thenReturn(true);
                when(activeNotificationRepo.delete(key))
                                .thenReturn(1L);

                ResponseEntity<String> response = controller.deleteActiveNotification(key);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                verify(activeNotificationRepo, times(1)).delete(key);
        }

        @Test
        public void testDeleteActiveNotificationsNotFound() {
                String key = "key";

                when(permissionService.hasRole("OPERATOR")).thenReturn(true);
                when(activeNotificationRepo.delete(key))
                                .thenReturn(0L);

                ResponseEntity<String> response = controller.deleteActiveNotification(key);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                verify(activeNotificationRepo, times(1)).delete(key);
        }

        @Test
        public void testDeleteActiveNotificationsThrowsException() {
                String key = "key";
                RuntimeException repoException = new RuntimeException("repo error");

                when(permissionService.hasRole("OPERATOR")).thenReturn(true);
                when(activeNotificationRepo.delete(key)).thenThrow(repoException);

                ResponseStatusException thrown = assertThrows(
                                ResponseStatusException.class,
                                () -> controller.deleteActiveNotification(key));

                assertThat(thrown.getReason())
                                .isEqualTo("Failed to delete Active Notification: " + repoException.getMessage());
                assertThat(thrown.getCause()).isEqualTo(repoException);
                verify(activeNotificationRepo, times(1)).delete(key);
        }

}
