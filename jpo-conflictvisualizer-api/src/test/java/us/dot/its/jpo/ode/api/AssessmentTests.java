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

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessment;
import us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment.StopLineStopAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment.SignalStateEventAssessmentRepository;
import us.dot.its.jpo.ode.api.controllers.AssessmentController;
import us.dot.its.jpo.ode.mockdata.MockAssessmentGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AssessmentTests {

    @Autowired
    AssessmentController controller;

    @MockBean
    LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo;

    @MockBean
    ConnectionOfTravelAssessmentRepository connectionOfTravelAssessmentRepo;

    @MockBean
    StopLineStopAssessmentRepository stopLineStopAssessmentRepo;

    @MockBean
    SignalStateEventAssessmentRepository signalStateEventAssessmentRepo;

    @Test
    public void testLaneDirectionOfTravelAssessment() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        LaneDirectionOfTravelAssessment assessment = MockAssessmentGenerator.getLaneDirectionOfTravelAssessment();

        List<LaneDirectionOfTravelAssessment> assessments= new ArrayList<>();
        assessments.add(assessment);


        Query query = laneDirectionOfTravelAssessmentRepo.getQuery(assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt()-1, assessment.getAssessmentGeneratedAt() + 1, false);

        when(laneDirectionOfTravelAssessmentRepo.find(query)).thenReturn(assessments);

        ResponseEntity<List<LaneDirectionOfTravelAssessment>> result = controller.findLaneDirectionOfTravelAssessment(assessment.getRoadRegulatorID(), assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt() - 1, assessment.getAssessmentGeneratedAt() + 1, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(assessments);
    }

    @Test
    public void testConnectionOfTravelAssessment() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        ConnectionOfTravelAssessment assessment = MockAssessmentGenerator.getConnectionOfTravelAssessment();

        List<ConnectionOfTravelAssessment> assessments= new ArrayList<>();
        assessments.add(assessment);


        Query query = connectionOfTravelAssessmentRepo.getQuery(assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt()-1, assessment.getAssessmentGeneratedAt() + 1, false);
        when(connectionOfTravelAssessmentRepo.find(query)).thenReturn(assessments);

        ResponseEntity<List<ConnectionOfTravelAssessment>> result = controller.findConnectionOfTravelAssessment(assessment.getRoadRegulatorID(), assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt() - 1, assessment.getAssessmentGeneratedAt() + 1, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(assessments);
    }

    @Test
    public void testStopLineStopAssessment() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        StopLineStopAssessment assessment = MockAssessmentGenerator.getStopLineStopAssessment();

        List<StopLineStopAssessment> assessments= new ArrayList<>();
        assessments.add(assessment);


        Query query = stopLineStopAssessmentRepo.getQuery(assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt()-1, assessment.getAssessmentGeneratedAt() + 1, false);
        when(stopLineStopAssessmentRepo.find(query)).thenReturn(assessments);

        ResponseEntity<List<StopLineStopAssessment>> result = controller.findSignalStateAssessment(assessment.getRoadRegulatorID(), assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt() - 1, assessment.getAssessmentGeneratedAt() + 1, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(assessments);
    }

    @Test
    public void testSignalStateEventAssessment() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

        StopLinePassageAssessment assessment = MockAssessmentGenerator.getStopLinePassageAssessment();

        List<StopLinePassageAssessment> assessments= new ArrayList<>();
        assessments.add(assessment);


        Query query = signalStateEventAssessmentRepo.getQuery(assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt()-1, assessment.getAssessmentGeneratedAt() + 1, false);
        when(signalStateEventAssessmentRepo.find(query)).thenReturn(assessments);

        ResponseEntity<List<StopLinePassageAssessment>> result = controller.findSignalStateEventAssessment(assessment.getRoadRegulatorID(), assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt() - 1, assessment.getAssessmentGeneratedAt() + 1, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(assessments);
    }
}