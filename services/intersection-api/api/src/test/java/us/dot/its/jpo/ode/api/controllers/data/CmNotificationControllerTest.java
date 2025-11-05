package us.dot.its.jpo.ode.api.controllers.data;

import static org.assertj.core.api.Assertions.assertThat;
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

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLineStopNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.connection_of_travel_notification.ConnectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.intersection_reference_alignment_notification.IntersectionReferenceAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.lane_direction_of_travel_notification.LaneDirectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.map_broadcast_rate_notification.MapBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.signal_group_alignment_notification.SignalGroupAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.signal_state_conflict_notification.SignalStateConflictNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.spat_broadcast_rate_notification.SpatBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.stop_line_passage_notification.StopLinePassageNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.stop_line_stop_notification.StopLineStopNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.time_change_details_notification.TimeChangeDetailsNotificationRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class CmNotificationControllerTest {

    private final CmNotificationController controller;

    @MockitoBean
    IntersectionReferenceAlignmentNotificationRepository intersectionReferenceAlignmentNotificationRepo;

    @MockitoBean
    LaneDirectionOfTravelNotificationRepository laneDirectionOfTravelNotificationRepo;

    @MockitoBean
    MapBroadcastRateNotificationRepository mapBroadcastRateNotificationRepo;

    @MockitoBean
    SignalGroupAlignmentNotificationRepository signalGroupAlignmentNotificationRepo;

    @MockitoBean
    SignalStateConflictNotificationRepository signalStateConflictNotificationRepo;

    @MockitoBean
    SpatBroadcastRateNotificationRepository spatBroadcastRateNotificationRepo;

    @MockitoBean
    ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo;

    @MockitoBean
    StopLineStopNotificationRepository stopLineStopNotificationRepo;

    @MockitoBean
    StopLinePassageNotificationRepository stopLinePassageNotificationRepo;

    @MockitoBean
    TimeChangeDetailsNotificationRepository timeChangeDetailsNotificationRepo;

    @MockitoBean
    PermissionService permissionService;

    @Autowired
    public CmNotificationControllerTest(CmNotificationController controller) {
        this.controller = controller;
    }

    @Test
    void testFindConnectionOfTravelNotificationWithTestData() {
        ConnectionOfTravelNotification event = MockNotificationGenerator.getConnectionOfTravelNotification();

        List<ConnectionOfTravelNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<ConnectionOfTravelNotification>> response = controller
                .findConnectionOfTravelNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindConnectionOfTravelNotificationsWithLatestFlag() {
        ConnectionOfTravelNotification event = MockNotificationGenerator.getConnectionOfTravelNotification();

        List<ConnectionOfTravelNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<ConnectionOfTravelNotification> mockPage = new PageImpl<>(events);
        when(connectionOfTravelNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<ConnectionOfTravelNotification>> response = controller
                .findConnectionOfTravelNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(connectionOfTravelNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindConnectionOfTravelNotificationsWithPagination() {
        ConnectionOfTravelNotification event = MockNotificationGenerator.getConnectionOfTravelNotification();

        List<ConnectionOfTravelNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<ConnectionOfTravelNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(connectionOfTravelNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<ConnectionOfTravelNotification>> response = controller
                .findConnectionOfTravelNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(connectionOfTravelNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountConnectionOfTravelNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countConnectionOfTravelNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountConnectionOfTravelNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(connectionOfTravelNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countConnectionOfTravelNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(connectionOfTravelNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindIntersectionReferenceAlignmentNotificationWithTestData() {
        IntersectionReferenceAlignmentNotification event = MockNotificationGenerator
                .getIntersectionReferenceAlignmentNotification();

        List<IntersectionReferenceAlignmentNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<IntersectionReferenceAlignmentNotification>> response = controller
                .findIntersectionReferenceAlignmentNotifications(event.getIntersectionID(), null, null,
                        false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindIntersectionReferenceAlignmentNotificationsWithLatestFlag() {
        IntersectionReferenceAlignmentNotification event = MockNotificationGenerator
                .getIntersectionReferenceAlignmentNotification();

        List<IntersectionReferenceAlignmentNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<IntersectionReferenceAlignmentNotification> mockPage = new PageImpl<>(events);
        when(intersectionReferenceAlignmentNotificationRepo.findLatest(eq(event.getIntersectionID()), any(),
                any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<IntersectionReferenceAlignmentNotification>> response = controller
                .findIntersectionReferenceAlignmentNotifications(event.getIntersectionID(), null, null,
                        latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(intersectionReferenceAlignmentNotificationRepo, times(1)).findLatest(
                eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindIntersectionReferenceAlignmentNotificationsWithPagination() {
        IntersectionReferenceAlignmentNotification event = MockNotificationGenerator
                .getIntersectionReferenceAlignmentNotification();

        List<IntersectionReferenceAlignmentNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<IntersectionReferenceAlignmentNotification> mockPage = new PageImpl<>(events,
                PageRequest.of(0, 10), 1);
        when(intersectionReferenceAlignmentNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<IntersectionReferenceAlignmentNotification>> response = controller
                .findIntersectionReferenceAlignmentNotifications(event.getIntersectionID(), null, null,
                        latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(intersectionReferenceAlignmentNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountIntersectionReferenceAlignmentNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countIntersectionReferenceAlignmentNotifications(
                intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountIntersectionReferenceAlignmentNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(intersectionReferenceAlignmentNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countIntersectionReferenceAlignmentNotifications(
                intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(intersectionReferenceAlignmentNotificationRepo, times(1)).count(intersectionID, startTime,
                endTime);
    }

    @Test
    void testFindLaneDirectionOfTravelNotificationWithTestData() {
        LaneDirectionOfTravelNotification event = MockNotificationGenerator
                .getLaneDirectionOfTravelNotification();

        List<LaneDirectionOfTravelNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<LaneDirectionOfTravelNotification>> response = controller
                .findLaneDirectionOfTravelNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindLaneDirectionOfTravelNotificationsWithLatestFlag() {
        LaneDirectionOfTravelNotification event = MockNotificationGenerator
                .getLaneDirectionOfTravelNotification();

        List<LaneDirectionOfTravelNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<LaneDirectionOfTravelNotification> mockPage = new PageImpl<>(events);
        when(laneDirectionOfTravelNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<LaneDirectionOfTravelNotification>> response = controller
                .findLaneDirectionOfTravelNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(laneDirectionOfTravelNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindLaneDirectionOfTravelNotificationsWithPagination() {
        LaneDirectionOfTravelNotification event = MockNotificationGenerator
                .getLaneDirectionOfTravelNotification();

        List<LaneDirectionOfTravelNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<LaneDirectionOfTravelNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(laneDirectionOfTravelNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<LaneDirectionOfTravelNotification>> response = controller
                .findLaneDirectionOfTravelNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(laneDirectionOfTravelNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountLaneDirectionOfTravelNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countLaneDirectionOfTravelNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountLaneDirectionOfTravelNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(laneDirectionOfTravelNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countLaneDirectionOfTravelNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(laneDirectionOfTravelNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindMapBroadcastRateNotificationWithTestData() {
        MapBroadcastRateNotification event = MockNotificationGenerator.getMapBroadcastRateNotification();

        List<MapBroadcastRateNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<MapBroadcastRateNotification>> response = controller
                .findMapBroadcastRateNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindMapBroadcastRateNotificationsWithLatestFlag() {
        MapBroadcastRateNotification event = MockNotificationGenerator.getMapBroadcastRateNotification();

        List<MapBroadcastRateNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<MapBroadcastRateNotification> mockPage = new PageImpl<>(events);
        when(mapBroadcastRateNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<MapBroadcastRateNotification>> response = controller
                .findMapBroadcastRateNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(mapBroadcastRateNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindMapBroadcastRateNotificationsWithPagination() {
        MapBroadcastRateNotification event = MockNotificationGenerator.getMapBroadcastRateNotification();

        List<MapBroadcastRateNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<MapBroadcastRateNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(mapBroadcastRateNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<MapBroadcastRateNotification>> response = controller
                .findMapBroadcastRateNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(mapBroadcastRateNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountMapBroadcastRateNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countMapBroadcastRateNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountMapBroadcastRateNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(mapBroadcastRateNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countMapBroadcastRateNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(mapBroadcastRateNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindSignalGroupAlignmentNotificationWithTestData() {
        SignalGroupAlignmentNotification event = MockNotificationGenerator
                .getSignalGroupAlignmentNotification();

        List<SignalGroupAlignmentNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<SignalGroupAlignmentNotification>> response = controller
                .findSignalGroupAlignmentNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindSignalGroupAlignmentNotificationsWithLatestFlag() {
        SignalGroupAlignmentNotification event = MockNotificationGenerator
                .getSignalGroupAlignmentNotification();

        List<SignalGroupAlignmentNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<SignalGroupAlignmentNotification> mockPage = new PageImpl<>(events);
        when(signalGroupAlignmentNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<SignalGroupAlignmentNotification>> response = controller
                .findSignalGroupAlignmentNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(signalGroupAlignmentNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindSignalGroupAlignmentNotificationsWithPagination() {
        SignalGroupAlignmentNotification event = MockNotificationGenerator
                .getSignalGroupAlignmentNotification();

        List<SignalGroupAlignmentNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<SignalGroupAlignmentNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(signalGroupAlignmentNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<SignalGroupAlignmentNotification>> response = controller
                .findSignalGroupAlignmentNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(signalGroupAlignmentNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountSignalGroupAlignmentNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countSignalGroupAlignmentNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountSignalGroupAlignmentNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(signalGroupAlignmentNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countSignalGroupAlignmentNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(signalGroupAlignmentNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindSignalStateConflictNotificationWithTestData() {
        SignalStateConflictNotification event = MockNotificationGenerator.getSignalStateConflictNotification();

        List<SignalStateConflictNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<SignalStateConflictNotification>> response = controller
                .findSignalStateConflictNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindSignalStateConflictNotificationsWithLatestFlag() {
        SignalStateConflictNotification event = MockNotificationGenerator.getSignalStateConflictNotification();

        List<SignalStateConflictNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<SignalStateConflictNotification> mockPage = new PageImpl<>(events);
        when(signalStateConflictNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<SignalStateConflictNotification>> response = controller
                .findSignalStateConflictNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(signalStateConflictNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindSignalStateConflictNotificationsWithPagination() {
        SignalStateConflictNotification event = MockNotificationGenerator.getSignalStateConflictNotification();

        List<SignalStateConflictNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<SignalStateConflictNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(signalStateConflictNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<SignalStateConflictNotification>> response = controller
                .findSignalStateConflictNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(signalStateConflictNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountSignalStateConflictNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countSignalStateConflictNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountSignalStateConflictNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(signalStateConflictNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countSignalStateConflictNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(signalStateConflictNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindSpatBroadcastRateNotificationWithTestData() {
        SpatBroadcastRateNotification event = MockNotificationGenerator.getSpatBroadcastRateNotification();

        List<SpatBroadcastRateNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<SpatBroadcastRateNotification>> response = controller
                .findSpatBroadcastRateNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindSpatBroadcastRateNotificationsWithLatestFlag() {
        SpatBroadcastRateNotification event = MockNotificationGenerator.getSpatBroadcastRateNotification();

        List<SpatBroadcastRateNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<SpatBroadcastRateNotification> mockPage = new PageImpl<>(events);
        when(spatBroadcastRateNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<SpatBroadcastRateNotification>> response = controller
                .findSpatBroadcastRateNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(spatBroadcastRateNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindSpatBroadcastRateNotificationsWithPagination() {
        SpatBroadcastRateNotification event = MockNotificationGenerator.getSpatBroadcastRateNotification();

        List<SpatBroadcastRateNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<SpatBroadcastRateNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(spatBroadcastRateNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<SpatBroadcastRateNotification>> response = controller
                .findSpatBroadcastRateNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(spatBroadcastRateNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountSpatBroadcastRateNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countSpatBroadcastRateNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountSpatBroadcastRateNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(spatBroadcastRateNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countSpatBroadcastRateNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(spatBroadcastRateNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindStopLineStopNotificationWithTestData() {
        StopLineStopNotification event = MockNotificationGenerator.getStopLineStopNotification();

        List<StopLineStopNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<StopLineStopNotification>> response = controller
                .findStopLineStopNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindStopLineStopNotificationsWithLatestFlag() {
        StopLineStopNotification event = MockNotificationGenerator.getStopLineStopNotification();

        List<StopLineStopNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<StopLineStopNotification> mockPage = new PageImpl<>(events);
        when(stopLineStopNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<StopLineStopNotification>> response = controller
                .findStopLineStopNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(stopLineStopNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindStopLineStopNotificationsWithPagination() {
        StopLineStopNotification event = MockNotificationGenerator.getStopLineStopNotification();

        List<StopLineStopNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<StopLineStopNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(stopLineStopNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<StopLineStopNotification>> response = controller
                .findStopLineStopNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(stopLineStopNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountStopLineStopNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countStopLineStopNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountStopLineStopNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(stopLineStopNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countStopLineStopNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(stopLineStopNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindStopLinePassageNotificationWithTestData() {
        StopLinePassageNotification event = MockNotificationGenerator.getStopLinePassageNotification();

        List<StopLinePassageNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<StopLinePassageNotification>> response = controller
                .findStopLinePassageNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindStopLinePassageNotificationsWithLatestFlag() {
        StopLinePassageNotification event = MockNotificationGenerator.getStopLinePassageNotification();

        List<StopLinePassageNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<StopLinePassageNotification> mockPage = new PageImpl<>(events);
        when(stopLinePassageNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<StopLinePassageNotification>> response = controller
                .findStopLinePassageNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(stopLinePassageNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindStopLinePassageNotificationsWithPagination() {
        StopLinePassageNotification event = MockNotificationGenerator.getStopLinePassageNotification();

        List<StopLinePassageNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<StopLinePassageNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(stopLinePassageNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<StopLinePassageNotification>> response = controller
                .findStopLinePassageNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(stopLinePassageNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountStopLinePassageNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countStopLinePassageNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountStopLinePassageNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(stopLinePassageNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countStopLinePassageNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(stopLinePassageNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }

    @Test
    void testFindTimeChangeDetailsNotificationWithTestData() {
        TimeChangeDetailsNotification event = MockNotificationGenerator.getTimeChangeDetailsNotification();

        List<TimeChangeDetailsNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<TimeChangeDetailsNotification>> response = controller
                .findTimeChangeDetailsNotifications(event.getIntersectionID(), null, null, false,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindTimeChangeDetailsNotificationsWithLatestFlag() {
        TimeChangeDetailsNotification event = MockNotificationGenerator.getTimeChangeDetailsNotification();

        List<TimeChangeDetailsNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = true;

        Page<TimeChangeDetailsNotification> mockPage = new PageImpl<>(events);
        when(timeChangeDetailsNotificationRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                .thenReturn(mockPage);

        ResponseEntity<Page<TimeChangeDetailsNotification>> response = controller
                .findTimeChangeDetailsNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(timeChangeDetailsNotificationRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                any(), any());
    }

    @Test
    void testFindTimeChangeDetailsNotificationsWithPagination() {
        TimeChangeDetailsNotification event = MockNotificationGenerator.getTimeChangeDetailsNotification();

        List<TimeChangeDetailsNotification> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean latest = false;

        Page<TimeChangeDetailsNotification> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(timeChangeDetailsNotificationRepo.find(eq(event.getIntersectionID()), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<TimeChangeDetailsNotification>> response = controller
                .findTimeChangeDetailsNotifications(event.getIntersectionID(), null, null, latest,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(timeChangeDetailsNotificationRepo, times(1))
                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountTimeChangeDetailsNotificationsWithTestData() {
        Integer intersectionID = 1;
        boolean testData = true;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countTimeChangeDetailsNotifications(intersectionID,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
    }

    @Test
    public void testCountTimeChangeDetailsNotifications() {
        Integer intersectionID = 1;
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(timeChangeDetailsNotificationRepo.count(intersectionID, startTime, endTime))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countTimeChangeDetailsNotifications(intersectionID,
                startTime, endTime, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(timeChangeDetailsNotificationRepo, times(1)).count(intersectionID, startTime, endTime);
    }
}
