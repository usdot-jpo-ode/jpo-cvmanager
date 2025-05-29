package us.dot.its.jpo.ode.api.controllers.intersections;

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
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class ActiveNotificationControllerTest {

        private final ActiveNotificationController controller;

        @MockBean
        ActiveNotificationRepository activeNotificationRepo;

        @MockBean
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

}
