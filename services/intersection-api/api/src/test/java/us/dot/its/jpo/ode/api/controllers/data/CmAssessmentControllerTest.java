package us.dot.its.jpo.ode.api.controllers.data;

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
        public void testLaneDirectionOfTravelAssessment() {

                LaneDirectionOfTravelAssessment assessment = MockAssessmentGenerator
                                .getLaneDirectionOfTravelAssessment();

                List<LaneDirectionOfTravelAssessment> assessments = new ArrayList<>();
                assessments.add(assessment);

                when(permissionService.hasIntersection(assessment.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(laneDirectionOfTravelAssessmentRepo.find(assessment.getIntersectionID(),
                                assessment.getAssessmentGeneratedAt() - 1,
                                assessment.getAssessmentGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(assessments, page, 1L));

                ResponseEntity<Page<LaneDirectionOfTravelAssessment>> result = controller
                                .findLaneDirectionOfTravelAssessment(
                                                assessment.getIntersectionID(),
                                                assessment.getAssessmentGeneratedAt() - 1,
                                                assessment.getAssessmentGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(assessments);
        }

        @Test
        public void testConnectionOfTravelAssessment() {

                ConnectionOfTravelAssessment assessment = MockAssessmentGenerator.getConnectionOfTravelAssessment();

                List<ConnectionOfTravelAssessment> assessments = new ArrayList<>();
                assessments.add(assessment);

                when(permissionService.hasIntersection(assessment.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(connectionOfTravelAssessmentRepo.find(assessment.getIntersectionID(),
                                assessment.getAssessmentGeneratedAt() - 1,
                                assessment.getAssessmentGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(assessments, page, 1L));

                ResponseEntity<Page<ConnectionOfTravelAssessment>> result = controller
                                .findConnectionOfTravelAssessment(
                                                assessment.getIntersectionID(),
                                                assessment.getAssessmentGeneratedAt() - 1,
                                                assessment.getAssessmentGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(assessments);
        }

        @Test
        public void testStopLineStopAssessment() {

                StopLineStopAssessment assessment = MockAssessmentGenerator.getStopLineStopAssessment();

                List<StopLineStopAssessment> assessments = new ArrayList<>();
                assessments.add(assessment);

                when(permissionService.hasIntersection(assessment.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(stopLineStopAssessmentRepo.find(assessment.getIntersectionID(),
                                assessment.getAssessmentGeneratedAt() - 1,
                                assessment.getAssessmentGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(assessments, page, 1L));

                ResponseEntity<Page<StopLineStopAssessment>> result = controller
                                .findStopLineStopAssessment(
                                                assessment.getIntersectionID(),
                                                assessment.getAssessmentGeneratedAt() - 1,
                                                assessment.getAssessmentGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(assessments);
        }

        @Test
        public void testStopLinePassageAssessmentRepo() {

                StopLinePassageAssessment assessment = MockAssessmentGenerator.getStopLinePassageAssessment();

                List<StopLinePassageAssessment> assessments = new ArrayList<>();
                assessments.add(assessment);

                when(permissionService.hasIntersection(assessment.getIntersectionID(), "USER")).thenReturn(true);
                when(permissionService.hasRole("USER")).thenReturn(true);

                PageRequest page = PageRequest.of(1, 1);
                when(stopLinePassageAssessmentRepo.find(assessment.getIntersectionID(),
                                assessment.getAssessmentGeneratedAt() - 1,
                                assessment.getAssessmentGeneratedAt() + 1, PageRequest.of(1, 1)))
                                .thenReturn(new PageImpl<>(assessments, page, 1L));

                ResponseEntity<Page<StopLinePassageAssessment>> result = controller
                                .findStopLinePassageAssessment(
                                                assessment.getIntersectionID(),
                                                assessment.getAssessmentGeneratedAt() - 1,
                                                assessment.getAssessmentGeneratedAt() + 1, false, 1, 1, false);
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody().getContent()).isEqualTo(assessments);
        }
}