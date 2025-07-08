package us.dot.its.jpo.ode.api.controllers.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
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
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.accessors.events.bsm_event.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.bsm_message_count_progression_event.BsmMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.connection_of_travel_event.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.intersection_reference_alignment_event.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.lane_direction_of_travel_event.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_broadcast_rate_event.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_message_count_progression_event.MapMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_minimum_data_event.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_group_alignment_event.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_state_conflict_event.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_broadcast_rate_event.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_message_count_progression_event.SpatMessageCountProgressionEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_minimum_data_event.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_passage_event.StopLinePassageEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_stop_event.StopLineStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.time_change_details_event.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.MinuteCount;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;
import us.dot.its.jpo.ode.mockdata.MockEventGenerator;
import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.model.OdeObject;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmCoreData;

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
        LaneDirectionOfTravelEventRepository laneDirectionOfTravelRepo;

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
        SpatMessageCountProgressionEventRepository spatMessageCountProgressionEventRepo;

        @MockBean
        MapMessageCountProgressionEventRepository mapMessageCountProgressionEventRepo;

        @MockBean
        BsmMessageCountProgressionEventRepository bsmMessageCountProgressionEventRepo;

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
        void testFindIntersectionReferenceAlignmentEventsWithTestData() {
                IntersectionReferenceAlignmentEvent event = MockEventGenerator.getIntersectionReferenceAlignmentEvent();

                List<IntersectionReferenceAlignmentEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<IntersectionReferenceAlignmentEvent>> response = controller
                                .findIntersectionReferenceAlignmentEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindIntersectionReferenceAlignmentEventsWithLatestFlag() {
                IntersectionReferenceAlignmentEvent event = MockEventGenerator.getIntersectionReferenceAlignmentEvent();

                List<IntersectionReferenceAlignmentEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<IntersectionReferenceAlignmentEvent> mockPage = new PageImpl<>(events);
                when(intersectionReferenceAlignmentEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<IntersectionReferenceAlignmentEvent>> response = controller
                                .findIntersectionReferenceAlignmentEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(intersectionReferenceAlignmentEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindIntersectionReferenceAlignmentEventsWithPagination() {
                IntersectionReferenceAlignmentEvent event = MockEventGenerator.getIntersectionReferenceAlignmentEvent();

                List<IntersectionReferenceAlignmentEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<IntersectionReferenceAlignmentEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(intersectionReferenceAlignmentEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<IntersectionReferenceAlignmentEvent>> response = controller
                                .findIntersectionReferenceAlignmentEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(intersectionReferenceAlignmentEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountIntersectionReferenceAlignmentEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countIntersectionReferenceAlignmentEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountIntersectionReferenceAlignmentEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(intersectionReferenceAlignmentEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countIntersectionReferenceAlignmentEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(intersectionReferenceAlignmentEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindConnectionOfTravelEventWithTestData() {
                ConnectionOfTravelEvent event = MockEventGenerator.getConnectionOfTravelEvent();

                List<ConnectionOfTravelEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<ConnectionOfTravelEvent>> response = controller
                                .findConnectionOfTravelEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindConnectionOfTravelEventsWithLatestFlag() {
                ConnectionOfTravelEvent event = MockEventGenerator.getConnectionOfTravelEvent();

                List<ConnectionOfTravelEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<ConnectionOfTravelEvent> mockPage = new PageImpl<>(events);
                when(connectionOfTravelEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<ConnectionOfTravelEvent>> response = controller
                                .findConnectionOfTravelEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(connectionOfTravelEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindConnectionOfTravelEventsWithPagination() {
                ConnectionOfTravelEvent event = MockEventGenerator.getConnectionOfTravelEvent();

                List<ConnectionOfTravelEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<ConnectionOfTravelEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(connectionOfTravelEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<ConnectionOfTravelEvent>> response = controller
                                .findConnectionOfTravelEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(connectionOfTravelEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountConnectionOfTravelEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countConnectionOfTravelEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountConnectionOfTravelEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(connectionOfTravelEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countConnectionOfTravelEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(connectionOfTravelEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailyConnectionOfTravelEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailyConnectionOfTravelEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailyConnectionOfTravelEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(connectionOfTravelEventRepo.getAggregatedDailyConnectionOfTravelEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailyConnectionOfTravelEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(connectionOfTravelEventRepo, times(1))
                                .getAggregatedDailyConnectionOfTravelEventCounts(intersectionID, startTime, endTime);
        }

        @Test
        void testFindLaneDirectionOfTravelEventWithTestData() {
                LaneDirectionOfTravelEvent event = MockEventGenerator.getLaneDirectionOfTravelEvent();

                List<LaneDirectionOfTravelEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<LaneDirectionOfTravelEvent>> response = controller
                                .findLaneDirectionOfTravelEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindLaneDirectionOfTravelEventsWithLatestFlag() {
                LaneDirectionOfTravelEvent event = MockEventGenerator.getLaneDirectionOfTravelEvent();

                List<LaneDirectionOfTravelEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<LaneDirectionOfTravelEvent> mockPage = new PageImpl<>(events);
                when(laneDirectionOfTravelRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<LaneDirectionOfTravelEvent>> response = controller
                                .findLaneDirectionOfTravelEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(laneDirectionOfTravelRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindLaneDirectionOfTravelEventsWithPagination() {
                LaneDirectionOfTravelEvent event = MockEventGenerator.getLaneDirectionOfTravelEvent();

                List<LaneDirectionOfTravelEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<LaneDirectionOfTravelEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(laneDirectionOfTravelRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<LaneDirectionOfTravelEvent>> response = controller
                                .findLaneDirectionOfTravelEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(laneDirectionOfTravelRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountLaneDirectionOfTravelEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countLaneDirectionOfTravelEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountLaneDirectionOfTravelEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(laneDirectionOfTravelRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countLaneDirectionOfTravelEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(laneDirectionOfTravelRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailyLaneDirectionOfTravelEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailyLaneDirectionOfTravelEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailyLaneDirectionOfTravelEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(laneDirectionOfTravelRepo.getAggregatedDailyLaneDirectionOfTravelEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailyLaneDirectionOfTravelEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(laneDirectionOfTravelRepo, times(1))
                                .getAggregatedDailyLaneDirectionOfTravelEventCounts(intersectionID, startTime, endTime);
        }

        @Test
        void testFindSignalGroupAlignmentEventWithTestData() {
                SignalGroupAlignmentEvent event = MockEventGenerator.getSignalGroupAlignmentEvent();

                List<SignalGroupAlignmentEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<SignalGroupAlignmentEvent>> response = controller
                                .findSignalGroupAlignmentEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindSignalGroupAlignmentEventsWithLatestFlag() {
                SignalGroupAlignmentEvent event = MockEventGenerator.getSignalGroupAlignmentEvent();

                List<SignalGroupAlignmentEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<SignalGroupAlignmentEvent> mockPage = new PageImpl<>(events);
                when(signalGroupAlignmentEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SignalGroupAlignmentEvent>> response = controller
                                .findSignalGroupAlignmentEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(signalGroupAlignmentEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindSignalGroupAlignmentEventsWithPagination() {
                SignalGroupAlignmentEvent event = MockEventGenerator.getSignalGroupAlignmentEvent();

                List<SignalGroupAlignmentEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<SignalGroupAlignmentEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(signalGroupAlignmentEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SignalGroupAlignmentEvent>> response = controller
                                .findSignalGroupAlignmentEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(signalGroupAlignmentEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountSignalGroupAlignmentEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countSignalGroupAlignmentEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountSignalGroupAlignmentEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(signalGroupAlignmentEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countSignalGroupAlignmentEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(signalGroupAlignmentEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailySignalGroupAlignmentEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailySignalGroupAlignmentEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailySignalGroupAlignmentEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(signalGroupAlignmentEventRepo.getAggregatedDailySignalGroupAlignmentEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailySignalGroupAlignmentEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(signalGroupAlignmentEventRepo, times(1))
                                .getAggregatedDailySignalGroupAlignmentEventCounts(intersectionID, startTime, endTime);
        }

        @Test
        void testFindSignalStateConflictEventWithTestData() {
                SignalStateConflictEvent event = MockEventGenerator.getSignalStateConflictEvent();

                List<SignalStateConflictEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<SignalStateConflictEvent>> response = controller
                                .findSignalStateConflictEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindSignalStateConflictEventsWithLatestFlag() {
                SignalStateConflictEvent event = MockEventGenerator.getSignalStateConflictEvent();

                List<SignalStateConflictEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<SignalStateConflictEvent> mockPage = new PageImpl<>(events);
                when(signalStateConflictEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SignalStateConflictEvent>> response = controller
                                .findSignalStateConflictEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(signalStateConflictEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindSignalStateConflictEventsWithPagination() {
                SignalStateConflictEvent event = MockEventGenerator.getSignalStateConflictEvent();

                List<SignalStateConflictEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<SignalStateConflictEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(signalStateConflictEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SignalStateConflictEvent>> response = controller
                                .findSignalStateConflictEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(signalStateConflictEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountSignalStateConflictEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countSignalStateConflictEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountSignalStateConflictEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(signalStateConflictEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countSignalStateConflictEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(signalStateConflictEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailySignalStateConflictEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailySignalStateConflictEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailySignalStateConflictEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(signalStateConflictEventRepo.getAggregatedDailySignalStateConflictEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailySignalStateConflictEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(signalStateConflictEventRepo, times(1))
                                .getAggregatedDailySignalStateConflictEventCounts(intersectionID, startTime, endTime);
        }

        @Test
        void testFindStopLinePassageEventWithTestData() {
                StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();

                List<StopLinePassageEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<StopLinePassageEvent>> response = controller
                                .findStopLinePassageEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindStopLinePassageEventsWithLatestFlag() {
                StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();

                List<StopLinePassageEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<StopLinePassageEvent> mockPage = new PageImpl<>(events);
                when(stopLinePassageEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLinePassageEvent>> response = controller
                                .findStopLinePassageEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLinePassageEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindStopLinePassageEventsWithPagination() {
                StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();

                List<StopLinePassageEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<StopLinePassageEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(stopLinePassageEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLinePassageEvent>> response = controller
                                .findStopLinePassageEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLinePassageEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountStopLinePassageEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countStopLinePassageEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountStopLinePassageEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(stopLinePassageEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countStopLinePassageEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(stopLinePassageEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailyStopLinePassageEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailyStopLinePassageEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailyStopLinePassageEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(stopLinePassageEventRepo.getAggregatedDailyStopLinePassageEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailyStopLinePassageEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(stopLinePassageEventRepo, times(1)).getAggregatedDailyStopLinePassageEventCounts(intersectionID,
                                startTime, endTime);
        }

        @Test
        void testFindStopLineStopEventWithTestData() {
                StopLineStopEvent event = MockEventGenerator.getStopLineStopEvent();

                List<StopLineStopEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<StopLineStopEvent>> response = controller
                                .findStopLineStopEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindStopLineStopEventsWithLatestFlag() {
                StopLineStopEvent event = MockEventGenerator.getStopLineStopEvent();

                List<StopLineStopEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<StopLineStopEvent> mockPage = new PageImpl<>(events);
                when(stopLineStopEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLineStopEvent>> response = controller
                                .findStopLineStopEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLineStopEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindStopLineStopEventsWithPagination() {
                StopLineStopEvent event = MockEventGenerator.getStopLineStopEvent();

                List<StopLineStopEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<StopLineStopEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(stopLineStopEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLineStopEvent>> response = controller
                                .findStopLineStopEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLineStopEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountStopLineStopEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countStopLineStopEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountStopLineStopEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(stopLineStopEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countStopLineStopEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(stopLineStopEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailyStopLineStopEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailyStopLineStopEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailyStopLineStopEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(stopLineStopEventRepo.getAggregatedDailyStopLineStopEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailyStopLineStopEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(stopLineStopEventRepo, times(1)).getAggregatedDailyStopLineStopEventCounts(intersectionID,
                                startTime, endTime);
        }

        @Test
        void testFindTimeChangeDetailsEventWithTestData() {
                TimeChangeDetailsEvent event = MockEventGenerator.getTimeChangeDetailsEvent();

                List<TimeChangeDetailsEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<TimeChangeDetailsEvent>> response = controller
                                .findTimeChangeDetailsEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindTimeChangeDetailsEventsWithLatestFlag() {
                TimeChangeDetailsEvent event = MockEventGenerator.getTimeChangeDetailsEvent();

                List<TimeChangeDetailsEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<TimeChangeDetailsEvent> mockPage = new PageImpl<>(events);
                when(timeChangeDetailsEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<TimeChangeDetailsEvent>> response = controller
                                .findTimeChangeDetailsEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(timeChangeDetailsEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindTimeChangeDetailsEventsWithPagination() {
                TimeChangeDetailsEvent event = MockEventGenerator.getTimeChangeDetailsEvent();

                List<TimeChangeDetailsEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<TimeChangeDetailsEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(timeChangeDetailsEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<TimeChangeDetailsEvent>> response = controller
                                .findTimeChangeDetailsEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(timeChangeDetailsEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountTimeChangeDetailsEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countTimeChangeDetailsEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountTimeChangeDetailsEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(timeChangeDetailsEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countTimeChangeDetailsEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(timeChangeDetailsEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        public void testGetDailyTimeChangeDetailsEventCountsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<List<IDCount>> response = controller.getDailyTimeChangeDetailsEventCounts(
                                intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().size()).isEqualTo(3); // Test data should return 1L
        }

        @Test
        public void testGetDailyTimeChangeDetailsEventCounts() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                List<IDCount> expectedCounts = List.of(new IDCount("1", 5.0));

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(timeChangeDetailsEventRepo.getAggregatedDailyTimeChangeDetailsEventCounts(intersectionID,
                                startTime, endTime))
                                .thenReturn(expectedCounts);

                ResponseEntity<List<IDCount>> response = controller.getDailyTimeChangeDetailsEventCounts(
                                intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCounts);
                verify(timeChangeDetailsEventRepo, times(1))
                                .getAggregatedDailyTimeChangeDetailsEventCounts(intersectionID, startTime, endTime);
        }

        @Test
        void testFindSpatMinimumDataEventWithTestData() {
                SpatMinimumDataEvent event = MockEventGenerator.getSpatMinimumDataEvent();

                List<SpatMinimumDataEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<SpatMinimumDataEvent>> response = controller
                                .findSpatMinimumDataEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertTrue(response.getBody().getContent().isEmpty()); // Test data should return empty
        }

        @Test
        void testFindSpatMinimumDataEventsWithLatestFlag() {
                SpatMinimumDataEvent event = MockEventGenerator.getSpatMinimumDataEvent();

                List<SpatMinimumDataEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<SpatMinimumDataEvent> mockPage = new PageImpl<>(events);
                when(spatMinimumDataEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SpatMinimumDataEvent>> response = controller
                                .findSpatMinimumDataEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(spatMinimumDataEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindSpatMinimumDataEventsWithPagination() {
                SpatMinimumDataEvent event = MockEventGenerator.getSpatMinimumDataEvent();

                List<SpatMinimumDataEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<SpatMinimumDataEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(spatMinimumDataEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SpatMinimumDataEvent>> response = controller
                                .findSpatMinimumDataEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(spatMinimumDataEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountSpatMinimumDataEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countSpatMinimumDataEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountSpatMinimumDataEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(spatMinimumDataEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countSpatMinimumDataEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(spatMinimumDataEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindMapMinimumDataEventWithTestData() {
                MapMinimumDataEvent event = MockEventGenerator.getMapMinimumDataEvent();

                List<MapMinimumDataEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<MapMinimumDataEvent>> response = controller
                                .findMapMinimumDataEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertTrue(response.getBody().getContent().isEmpty()); // Test data should return empty
        }

        @Test
        void testFindMapMinimumDataEventsWithLatestFlag() {
                MapMinimumDataEvent event = MockEventGenerator.getMapMinimumDataEvent();

                List<MapMinimumDataEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<MapMinimumDataEvent> mockPage = new PageImpl<>(events);
                when(mapMinimumDataEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<MapMinimumDataEvent>> response = controller
                                .findMapMinimumDataEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(mapMinimumDataEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindMapMinimumDataEventsWithPagination() {
                MapMinimumDataEvent event = MockEventGenerator.getMapMinimumDataEvent();

                List<MapMinimumDataEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<MapMinimumDataEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(mapMinimumDataEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<MapMinimumDataEvent>> response = controller
                                .findMapMinimumDataEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(mapMinimumDataEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountMapMinimumDataEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countMapMinimumDataEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountMapMinimumDataEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(mapMinimumDataEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countMapMinimumDataEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(mapMinimumDataEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindMapBroadcastRateEventWithTestData() {
                MapBroadcastRateEvent event = MockEventGenerator.getMapBroadcastRateEvent();

                List<MapBroadcastRateEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<MapBroadcastRateEvent>> response = controller
                                .findMapBroadcastRateEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindMapBroadcastRateEventsWithLatestFlag() {
                MapBroadcastRateEvent event = MockEventGenerator.getMapBroadcastRateEvent();

                List<MapBroadcastRateEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<MapBroadcastRateEvent> mockPage = new PageImpl<>(events);
                when(mapBroadcastRateEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<MapBroadcastRateEvent>> response = controller
                                .findMapBroadcastRateEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(mapBroadcastRateEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindMapBroadcastRateEventsWithPagination() {
                MapBroadcastRateEvent event = MockEventGenerator.getMapBroadcastRateEvent();

                List<MapBroadcastRateEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<MapBroadcastRateEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(mapBroadcastRateEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<MapBroadcastRateEvent>> response = controller
                                .findMapBroadcastRateEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(mapBroadcastRateEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountMapBroadcastRateEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countMapBroadcastRateEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountMapBroadcastRateEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(mapBroadcastRateEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countMapBroadcastRateEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(mapBroadcastRateEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindSpatBroadcastRateEventWithTestData() {
                SpatBroadcastRateEvent event = MockEventGenerator.getSpatBroadcastRateEvent();

                List<SpatBroadcastRateEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<SpatBroadcastRateEvent>> response = controller
                                .findSpatBroadcastRateEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindSpatBroadcastRateEventsWithLatestFlag() {
                SpatBroadcastRateEvent event = MockEventGenerator.getSpatBroadcastRateEvent();

                List<SpatBroadcastRateEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<SpatBroadcastRateEvent> mockPage = new PageImpl<>(events);
                when(spatBroadcastRateEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SpatBroadcastRateEvent>> response = controller
                                .findSpatBroadcastRateEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(spatBroadcastRateEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindSpatBroadcastRateEventsWithPagination() {
                SpatBroadcastRateEvent event = MockEventGenerator.getSpatBroadcastRateEvent();

                List<SpatBroadcastRateEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<SpatBroadcastRateEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(spatBroadcastRateEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SpatBroadcastRateEvent>> response = controller
                                .findSpatBroadcastRateEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(spatBroadcastRateEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountSpatBroadcastRateEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countSpatBroadcastRateEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountSpatBroadcastRateEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(spatBroadcastRateEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countSpatBroadcastRateEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(spatBroadcastRateEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindSpatMessageCountProgressionEventWithTestData() {
                SpatMessageCountProgressionEvent event = MockEventGenerator.getSpatMessageCountProgressionEvent();

                List<SpatMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<SpatMessageCountProgressionEvent>> response = controller
                                .findSpatMessageCountProgressionEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindSpatMessageCountProgressionEventsWithLatestFlag() {
                SpatMessageCountProgressionEvent event = MockEventGenerator.getSpatMessageCountProgressionEvent();

                List<SpatMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<SpatMessageCountProgressionEvent> mockPage = new PageImpl<>(events);
                when(spatMessageCountProgressionEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SpatMessageCountProgressionEvent>> response = controller
                                .findSpatMessageCountProgressionEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(spatMessageCountProgressionEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindSpatMessageCountProgressionEventsWithPagination() {
                SpatMessageCountProgressionEvent event = MockEventGenerator.getSpatMessageCountProgressionEvent();

                List<SpatMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<SpatMessageCountProgressionEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(spatMessageCountProgressionEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<SpatMessageCountProgressionEvent>> response = controller
                                .findSpatMessageCountProgressionEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(spatMessageCountProgressionEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountSpatMessageCountProgressionEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countSpatMessageCountProgressionEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountSpatMessageCountProgressionEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(spatMessageCountProgressionEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countSpatMessageCountProgressionEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(spatMessageCountProgressionEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        void testFindMapMessageCountProgressionEventWithTestData() {
                MapMessageCountProgressionEvent event = MockEventGenerator.getMapMessageCountProgressionEvent();

                List<MapMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<MapMessageCountProgressionEvent>> response = controller
                                .findMapMessageCountProgressionEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindMapMessageCountProgressionEventsWithLatestFlag() {
                MapMessageCountProgressionEvent event = MockEventGenerator.getMapMessageCountProgressionEvent();

                List<MapMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<MapMessageCountProgressionEvent> mockPage = new PageImpl<>(events);
                when(mapMessageCountProgressionEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<MapMessageCountProgressionEvent>> response = controller
                                .findMapMessageCountProgressionEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(mapMessageCountProgressionEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindMapMessageCountProgressionEventsWithPagination() {
                MapMessageCountProgressionEvent event = MockEventGenerator.getMapMessageCountProgressionEvent();

                List<MapMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<MapMessageCountProgressionEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(mapMessageCountProgressionEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<MapMessageCountProgressionEvent>> response = controller
                                .findMapMessageCountProgressionEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(mapMessageCountProgressionEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountMapMessageCountProgressionEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countMapMessageCountProgressionEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountMapMessageCountProgressionEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(mapMessageCountProgressionEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countMapMessageCountProgressionEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(mapMessageCountProgressionEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindBsmMessageCountProgressionEventWithTestData() {
                BsmMessageCountProgressionEvent event = MockEventGenerator.getBsmMessageCountProgressionEvent();

                List<BsmMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<BsmMessageCountProgressionEvent>> response = controller
                                .findBsmMessageCountProgressionEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindBsmMessageCountProgressionEventsWithLatestFlag() {
                BsmMessageCountProgressionEvent event = MockEventGenerator.getBsmMessageCountProgressionEvent();

                List<BsmMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<BsmMessageCountProgressionEvent> mockPage = new PageImpl<>(events);
                when(bsmMessageCountProgressionEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<BsmMessageCountProgressionEvent>> response = controller
                                .findBsmMessageCountProgressionEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(bsmMessageCountProgressionEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindBsmMessageCountProgressionEventsWithPagination() {
                BsmMessageCountProgressionEvent event = MockEventGenerator.getBsmMessageCountProgressionEvent();

                List<BsmMessageCountProgressionEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<BsmMessageCountProgressionEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(bsmMessageCountProgressionEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<BsmMessageCountProgressionEvent>> response = controller
                                .findBsmMessageCountProgressionEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(bsmMessageCountProgressionEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountBsmMessageCountProgressionEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countBsmMessageCountProgressionEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountBsmMessageCountProgressionEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(bsmMessageCountProgressionEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countBsmMessageCountProgressionEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(bsmMessageCountProgressionEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindBsmEventWithTestData() {
                BsmEvent event = MockEventGenerator.getBsmEvent();

                List<BsmEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<BsmEvent>> response = controller
                                .findBsmEvents(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindBsmEventsWithLatestFlag() {
                BsmEvent event = MockEventGenerator.getBsmEvent();

                List<BsmEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<BsmEvent> mockPage = new PageImpl<>(events);
                when(bsmEventRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<BsmEvent>> response = controller
                                .findBsmEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(bsmEventRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindBsmEventsWithPagination() {
                BsmEvent event = MockEventGenerator.getBsmEvent();

                List<BsmEvent> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<BsmEvent> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(bsmEventRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<BsmEvent>> response = controller
                                .findBsmEvents(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(bsmEventRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountBsmEventsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countBsmEvents(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountBsmEvents() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(bsmEventRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countBsmEvents(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(bsmEventRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testGetBsmActivityByMinuteInRangeWithTestData() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Page<MinuteCount>> response = controller.getBsmActivityByMinuteInRange(
                                intersectionID, startTime, endTime, false, 0, 10, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isNotEmpty();
                assertThat(response.getBody().getContent().size()).isEqualTo(10); // Test data generates 10 items
        }

        @Test
        void testGetBsmActivityByMinuteInRangeWithLatestFlag() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                boolean latest = true;
                boolean testData = false;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                Page<BsmEvent> mockPage = new PageImpl<>(Collections.emptyList());
                when(bsmEventRepo.findLatest(intersectionID, startTime, endTime)).thenReturn(mockPage);

                ResponseEntity<Page<MinuteCount>> response = controller.getBsmActivityByMinuteInRange(
                                intersectionID, startTime, endTime, latest, 0, 10, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEmpty(); // No events in the mock repository
                verify(bsmEventRepo, times(1)).findLatest(intersectionID, startTime, endTime);
        }

        @Test
        void testGetBsmActivityByMinuteInRangeWithPagination() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                boolean latest = false;
                boolean testData = false;
                J2735Bsm bsm = new J2735Bsm();
                J2735BsmCoreData coreData = new J2735BsmCoreData();
                coreData.setId("id");
                bsm.setCoreData(coreData);

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                BsmEvent mockEvent = new BsmEvent();
                mockEvent.setStartingBsm(new OdeBsmData(new OdeMsgMetadata(), new OdeMsgPayload(bsm)));
                mockEvent.setEndingBsm(new OdeBsmData(new OdeMsgMetadata(), new OdeMsgPayload()));
                Page<BsmEvent> mockPage = new PageImpl<>(Collections.singletonList(mockEvent), PageRequest.of(0, 10),
                                1);
                when(bsmEventRepo.find(intersectionID, startTime, endTime, PageRequest.of(0, 10))).thenReturn(mockPage);

                ResponseEntity<Page<MinuteCount>> response = controller.getBsmActivityByMinuteInRange(
                                intersectionID, startTime, endTime, latest, 0, 10, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isNotEmpty();
                verify(bsmEventRepo, times(1)).find(intersectionID, startTime, endTime, PageRequest.of(0, 10));
        }

}
