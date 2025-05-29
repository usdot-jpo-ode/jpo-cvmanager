package us.dot.its.jpo.ode.api.controllers.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import us.dot.its.jpo.ode.api.accessors.events.bsm_event.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.connection_of_travel_event.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.intersection_reference_alignment_event.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.lane_direction_of_travel_event.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_broadcast_rate_event.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_minimum_data_event.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_group_alignment_event.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_state_conflict_event.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_broadcast_rate_event.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_minimum_data_event.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_passage_event.StopLinePassageEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_stop_event.StopLineStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.time_change_details_event.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class CmEventControllerTest {

        private final CmEventController controller;

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
        StopLineStopEventRepository stopLineStopEventRepo;

        @MockBean
        StopLinePassageEventRepository stopLinePassageEventRepo;

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
        public CmEventControllerTest(CmEventController controller) {
                this.controller = controller;
        }

        @Test
        public void testIntersectionReferenceAlignmentEvents() {

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
        public void testStopLineStopEvents() {
                StopLineStopEvent event = MockEventGenerator.getStopLineStopEvent();

                List<StopLineStopEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(stopLineStopEventRepo.find(event.getIntersectionID(),
                                event.getEventGeneratedAt() - 1,
                                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(events, page, 1L));

                ResponseEntity<Page<StopLineStopEvent>> result = controller
                                .findStopLineStopEvent(
                                                event.getIntersectionID(),
                                                event.getEventGeneratedAt() - 1,
                                                event.getEventGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(events);
        }

        @Test
        public void testStopLinePassageEvents() {
                StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();

                List<StopLinePassageEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(stopLinePassageEventRepo.find(event.getIntersectionID(),
                                event.getEventGeneratedAt() - 1,
                                event.getEventGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(events, page, 1L));

                ResponseEntity<Page<StopLinePassageEvent>> result = controller
                                .findStopLinePassageEvent(
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
