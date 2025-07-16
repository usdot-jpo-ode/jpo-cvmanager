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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessment;
import us.dot.its.jpo.ode.api.accessors.assessments.connection_of_travel_assessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.lane_direction_of_travel_assessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.stop_line_passage_assessment.StopLinePassageAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.stop_line_stop_assessment.StopLineStopAssessmentRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockAssessmentGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class CmAssessmentControllerTest {

        private final CmAssessmentController controller;

        @MockBean
        LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo;

        @MockBean
        ConnectionOfTravelAssessmentRepository connectionOfTravelAssessmentRepo;

        @MockBean
        StopLineStopAssessmentRepository stopLineStopAssessmentRepo;

        @MockBean
        StopLinePassageAssessmentRepository stopLinePassageAssessmentRepo;

        @MockBean
        PermissionService permissionService;

        @Autowired
        public CmAssessmentControllerTest(CmAssessmentController controller) {
                this.controller = controller;
        }

        @Test
        void testFindConnectionOfTravelAssessmentWithTestData() {
                ConnectionOfTravelAssessment event = MockAssessmentGenerator.getConnectionOfTravelAssessment();

                List<ConnectionOfTravelAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<ConnectionOfTravelAssessment>> response = controller
                                .findConnectionOfTravelAssessments(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindConnectionOfTravelAssessmentsWithLatestFlag() {
                ConnectionOfTravelAssessment event = MockAssessmentGenerator.getConnectionOfTravelAssessment();

                List<ConnectionOfTravelAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<ConnectionOfTravelAssessment> mockPage = new PageImpl<>(events);
                when(connectionOfTravelAssessmentRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<ConnectionOfTravelAssessment>> response = controller
                                .findConnectionOfTravelAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(connectionOfTravelAssessmentRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindConnectionOfTravelAssessmentsWithPagination() {
                ConnectionOfTravelAssessment event = MockAssessmentGenerator.getConnectionOfTravelAssessment();

                List<ConnectionOfTravelAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<ConnectionOfTravelAssessment> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(connectionOfTravelAssessmentRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<ConnectionOfTravelAssessment>> response = controller
                                .findConnectionOfTravelAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(connectionOfTravelAssessmentRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountConnectionOfTravelAssessmentsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countConnectionOfTravelAssessments(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountConnectionOfTravelAssessments() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(connectionOfTravelAssessmentRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countConnectionOfTravelAssessments(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(connectionOfTravelAssessmentRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindLaneDirectionOfTravelAssessmentWithTestData() {
                LaneDirectionOfTravelAssessment event = MockAssessmentGenerator.getLaneDirectionOfTravelAssessment();

                List<LaneDirectionOfTravelAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<LaneDirectionOfTravelAssessment>> response = controller
                                .findLaneDirectionOfTravelAssessments(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindLaneDirectionOfTravelAssessmentsWithLatestFlag() {
                LaneDirectionOfTravelAssessment event = MockAssessmentGenerator.getLaneDirectionOfTravelAssessment();

                List<LaneDirectionOfTravelAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<LaneDirectionOfTravelAssessment> mockPage = new PageImpl<>(events);
                when(laneDirectionOfTravelAssessmentRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<LaneDirectionOfTravelAssessment>> response = controller
                                .findLaneDirectionOfTravelAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(laneDirectionOfTravelAssessmentRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindLaneDirectionOfTravelAssessmentsWithPagination() {
                LaneDirectionOfTravelAssessment event = MockAssessmentGenerator.getLaneDirectionOfTravelAssessment();

                List<LaneDirectionOfTravelAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<LaneDirectionOfTravelAssessment> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(laneDirectionOfTravelAssessmentRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<LaneDirectionOfTravelAssessment>> response = controller
                                .findLaneDirectionOfTravelAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(laneDirectionOfTravelAssessmentRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountLaneDirectionOfTravelAssessmentsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countLaneDirectionOfTravelAssessments(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountLaneDirectionOfTravelAssessments() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(laneDirectionOfTravelAssessmentRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countLaneDirectionOfTravelAssessments(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(laneDirectionOfTravelAssessmentRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindStopLineStopAssessmentWithTestData() {
                StopLineStopAssessment event = MockAssessmentGenerator.getStopLineStopAssessment();

                List<StopLineStopAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<StopLineStopAssessment>> response = controller
                                .findStopLineStopAssessments(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindStopLineStopAssessmentsWithLatestFlag() {
                StopLineStopAssessment event = MockAssessmentGenerator.getStopLineStopAssessment();

                List<StopLineStopAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<StopLineStopAssessment> mockPage = new PageImpl<>(events);
                when(stopLineStopAssessmentRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLineStopAssessment>> response = controller
                                .findStopLineStopAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLineStopAssessmentRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindStopLineStopAssessmentsWithPagination() {
                StopLineStopAssessment event = MockAssessmentGenerator.getStopLineStopAssessment();

                List<StopLineStopAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<StopLineStopAssessment> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(stopLineStopAssessmentRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLineStopAssessment>> response = controller
                                .findStopLineStopAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLineStopAssessmentRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountStopLineStopAssessmentsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countStopLineStopAssessments(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountStopLineStopAssessments() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(stopLineStopAssessmentRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countStopLineStopAssessments(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(stopLineStopAssessmentRepo, times(1)).count(intersectionID, startTime, endTime);
        }

        @Test
        void testFindStopLinePassageAssessmentWithTestData() {
                StopLinePassageAssessment event = MockAssessmentGenerator.getStopLinePassageAssessment();

                List<StopLinePassageAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean testData = true;

                ResponseEntity<Page<StopLinePassageAssessment>> response = controller
                                .findStopLinePassageAssessments(event.getIntersectionID(), null, null, false,
                                                0, 10,
                                                testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertFalse(response.getBody().getContent().isEmpty());
        }

        @Test
        void testFindStopLinePassageAssessmentsWithLatestFlag() {
                StopLinePassageAssessment event = MockAssessmentGenerator.getStopLinePassageAssessment();

                List<StopLinePassageAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = true;

                Page<StopLinePassageAssessment> mockPage = new PageImpl<>(events);
                when(stopLinePassageAssessmentRepo.findLatest(eq(event.getIntersectionID()), any(), any()))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLinePassageAssessment>> response = controller
                                .findStopLinePassageAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLinePassageAssessmentRepo, times(1)).findLatest(eq(event.getIntersectionID()),
                                any(), any());
        }

        @Test
        void testFindStopLinePassageAssessmentsWithPagination() {
                StopLinePassageAssessment event = MockAssessmentGenerator.getStopLinePassageAssessment();

                List<StopLinePassageAssessment> events = new ArrayList<>();
                events.add(event);

                when(permissionService.hasIntersection(event.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                boolean latest = false;

                Page<StopLinePassageAssessment> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
                when(stopLinePassageAssessmentRepo.find(eq(event.getIntersectionID()), any(), any(),
                                any(PageRequest.class)))
                                .thenReturn(mockPage);

                ResponseEntity<Page<StopLinePassageAssessment>> response = controller
                                .findStopLinePassageAssessments(event.getIntersectionID(), null, null, latest,
                                                0, 10,
                                                false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).isEqualTo(events);
                verify(stopLinePassageAssessmentRepo, times(1))
                                .find(eq(event.getIntersectionID()), any(), any(), any(PageRequest.class));
        }

        @Test
        public void testCountStopLinePassageAssessmentsWithTestData() {
                Integer intersectionID = 1;
                boolean testData = true;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                ResponseEntity<Long> response = controller.countStopLinePassageAssessments(intersectionID,
                                null, null, testData);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(1L); // Test data should return 1L
        }

        @Test
        public void testCountStopLinePassageAssessments() {
                Integer intersectionID = 1;
                Long startTime = 1000L;
                Long endTime = 2000L;
                Long expectedCount = 5L;

                when(permissionService.hasIntersection(intersectionID, "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);
                when(stopLinePassageAssessmentRepo.count(intersectionID, startTime, endTime))
                                .thenReturn(expectedCount);

                ResponseEntity<Long> response = controller.countStopLinePassageAssessments(intersectionID,
                                startTime, endTime, false);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(expectedCount);
                verify(stopLinePassageAssessmentRepo, times(1)).count(intersectionID, startTime, endTime);
        }
}