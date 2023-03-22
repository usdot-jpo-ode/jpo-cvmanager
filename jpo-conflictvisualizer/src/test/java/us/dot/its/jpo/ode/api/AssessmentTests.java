package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateEventAssessment;
import us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment.SignalStateAssessmentRepository;
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
    SignalStateAssessmentRepository signalStateAssessmentRepo;

    @MockBean
    SignalStateEventAssessmentRepository signalStateEventAssessmentRepo;

    @Test
    public void testLaneDirectionOfTravelAssessment() {

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
    public void testSignalStateAssessment() {

        SignalStateAssessment assessment = MockAssessmentGenerator.getSignalStateAssessment();

        List<SignalStateAssessment> assessments= new ArrayList<>();
        assessments.add(assessment);


        Query query = signalStateAssessmentRepo.getQuery(assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt()-1, assessment.getAssessmentGeneratedAt() + 1, false);
        when(signalStateAssessmentRepo.find(query)).thenReturn(assessments);

        ResponseEntity<List<SignalStateAssessment>> result = controller.findSignalStateAssessment(assessment.getRoadRegulatorID(), assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt() - 1, assessment.getAssessmentGeneratedAt() + 1, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(assessments);
    }

    @Test
    public void testSignalStateEventAssessment() {

        SignalStateEventAssessment assessment = MockAssessmentGenerator.getSignalStateEventAssessment();

        List<SignalStateEventAssessment> assessments= new ArrayList<>();
        assessments.add(assessment);


        Query query = signalStateEventAssessmentRepo.getQuery(assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt()-1, assessment.getAssessmentGeneratedAt() + 1, false);
        when(signalStateEventAssessmentRepo.find(query)).thenReturn(assessments);

        ResponseEntity<List<SignalStateEventAssessment>> result = controller.findSignalStateEventAssessment(assessment.getRoadRegulatorID(), assessment.getIntersectionID(), assessment.getAssessmentGeneratedAt() - 1, assessment.getAssessmentGeneratedAt() + 1, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(assessments);
    }
}