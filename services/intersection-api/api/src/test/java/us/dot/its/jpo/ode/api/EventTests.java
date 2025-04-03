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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.accessors.events.BsmEvent.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapMinimumDataEvent.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatBroadcastRateEvent.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.controllers.EventController;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class EventTests {

    private final EventController controller;

    @MockBean
    ConnectionOfTravelEventRepository connectionOfTravelEventRepo;

    @MockBean
    IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;

    @MockBean
    LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;

    @MockBean
    SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo;

    @MockBean
    SignalStateConflictEventRepository signalStateConflictEventRepo;

    @MockBean
    SignalStateStopEventRepository signalStateStopEventRepo;

    @MockBean
    SignalStateEventRepository signalStateEventRepo;

    @MockBean
    TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;

    @MockBean
    SpatMinimumDataEventRepository spatMinimumDataEventRepo;

    @MockBean
    MapMinimumDataEventRepository mapMinimumDataEventRepo;

    @MockBean
    SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;

    @MockBean
    MapBroadcastRateEventRepository mapBroadcastRateEventRepo;

    @MockBean
    BsmEventRepository bsmEventRepo;

    @MockBean
    PostgresService postgresService;

    @MockBean
    PermissionService permissionService;

    @Autowired
    public EventTests(EventController controller) {
        this.controller = controller;
    }

    @Test
    public void testIntersectionReferenceAlignmentEvents() {

        // MockKeyCloakAuth.setSecurityContextHolder("cm_user@cimms.com",
        // Set.of("USER"));

        // List<UserOrgRole> roles = new ArrayList<>();
        // UserOrgRole userOrgRole = new UserOrgRole("cm_user@cimms.com", "test",
        // "USER");

        // roles.add(userOrgRole);
        // when(postgresService.findUserOrgRoles("cm_user@cimms.com")).thenReturn(roles);

        IntersectionReferenceAlignmentEvent event = MockEventGenerator.getIntersectionReferenceAlignmentEvent();

        List<IntersectionReferenceAlignmentEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(intersectionReferenceAlignmentEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<IntersectionReferenceAlignmentEvent>> result = controller
                .findIntersectionReferenceAlignmentEvents(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testConnectionOfTravelEvents() {
        ConnectionOfTravelEvent event = MockEventGenerator.getConnectionOfTravelEvent();

        List<ConnectionOfTravelEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(connectionOfTravelEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<ConnectionOfTravelEvent>> result = controller
                .findConnectionOfTravelEvents(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testLaneDirectionOfTravelEvents() {
        LaneDirectionOfTravelEvent event = MockEventGenerator.getLaneDirectionOfTravelEvent();

        List<LaneDirectionOfTravelEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(laneDirectionOfTravelEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<LaneDirectionOfTravelEvent>> result = controller
                .findLaneDirectionOfTravelEvent(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testSignalGroupAlignmentEvents() {
        SignalGroupAlignmentEvent event = MockEventGenerator.getSignalGroupAlignmentEvent();

        List<SignalGroupAlignmentEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(signalGroupAlignmentEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<SignalGroupAlignmentEvent>> result = controller
                .findSignalGroupAlignmentEvent(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testSignalStateConflictEvents() {
        SignalStateConflictEvent event = MockEventGenerator.getSignalStateConflictEvent();

        List<SignalStateConflictEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(signalStateConflictEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<SignalStateConflictEvent>> result = controller
                .findSignalStateConflictEvent(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testSignalStateStopEvents() {
        StopLineStopEvent event = MockEventGenerator.getStopLineStopEvent();

        List<StopLineStopEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(signalStateStopEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<StopLineStopEvent>> result = controller
                .findSignalStateStopEvent(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testSignalStateEvents() {
        StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();

        List<StopLinePassageEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(signalStateEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<StopLinePassageEvent>> result = controller
                .findSignalStateEvent(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testTimeChangeDetailsEvents() {
        TimeChangeDetailsEvent event = MockEventGenerator.getTimeChangeDetailsEvent();

        List<TimeChangeDetailsEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(timeChangeDetailsEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<TimeChangeDetailsEvent>> result = controller
                .findTimeChangeDetailsEvent(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testSpatMinimumDataEvents() {
        SpatMinimumDataEvent event = MockEventGenerator.getSpatMinimumDataEvent();

        List<SpatMinimumDataEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(spatMinimumDataEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<SpatMinimumDataEvent>> result = controller
                .findSpatMinimumDataEvents(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testMapMinimumDataEvents() {
        MapMinimumDataEvent event = MockEventGenerator.getMapMinimumDataEvent();

        List<MapMinimumDataEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(mapMinimumDataEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<MapMinimumDataEvent>> result = controller
                .findMapMinimumDataEvents(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testSpatBroadcastRateEvents() {
        SpatBroadcastRateEvent event = MockEventGenerator.getSpatBroadcastRateEvent();

        List<SpatBroadcastRateEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(spatBroadcastRateEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<SpatBroadcastRateEvent>> result = controller
                .findSpatBroadcastRateEvents(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testMapBroadcastRateEvents() {
        MapBroadcastRateEvent event = MockEventGenerator.getMapBroadcastRateEvent();

        List<MapBroadcastRateEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(mapBroadcastRateEventRepo.find(event.getIntersectionID(),
                event.getEventGeneratedAt() - 1,
                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<MapBroadcastRateEvent>> result = controller
                .findMapBroadcastRateEvents(
                        event.getIntersectionID(),
                        event.getEventGeneratedAt() - 1,
                        event.getEventGeneratedAt() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

    @Test
    public void testBsmEvents() {
        BsmEvent event = MockEventGenerator.getBsmEvent();

        List<BsmEvent> events = new ArrayList<>();
        events.add(event);

        when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(bsmEventRepo.find(event.getIntersectionID(),
                event.getStartingBsmTimestamp() - 1,
                event.getStartingBsmTimestamp() + 1, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(events, page, 1L));

        ResponseEntity<Page<BsmEvent>> result = controller
                .findBsmEvents(
                        event.getIntersectionID(),
                        event.getStartingBsmTimestamp() - 1,
                        event.getStartingBsmTimestamp() + 1, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(events);
    }

}
