package us.dot.its.jpo.ode.api.accessorTests.assessments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment.SignalStateEventAssessmentRepositoryImpl;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class SignalStateEventAssessmentRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SignalStateEventAssessmentRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuery() {

        Query query = repository.getQuery(intersectionID, startTime, endTime, latest);

        // Assert IntersectionID
        assertThat(query.getQueryObject().get("intersectionID")).isEqualTo(intersectionID);

        // Assert Start and End Time
        Document queryTimeDocument = (Document) query.getQueryObject().get("assessmentGeneratedAt");
        assertThat(queryTimeDocument.getDate("$gte")).isEqualTo(new Date(startTime));
        assertThat(queryTimeDocument.getDate("$lte")).isEqualTo(new Date(endTime));

        // Assert sorting and limit
        assertThat(query.getSortObject().keySet().contains("assessmentGeneratedAt")).isTrue();
        assertThat(query.getSortObject().get("assessmentGeneratedAt")).isEqualTo(-1);
        assertThat(query.getLimit()).isEqualTo(1);

    }

    @Test
    public void testGetQueryResultCount() {
        Query query = new Query();
        long expectedCount = 10;

        Mockito.when(mongoTemplate.count(Mockito.eq(query), Mockito.any(), Mockito.anyString()))
                .thenReturn(expectedCount);

        long resultCount = repository.getQueryResultCount(query);

        assertThat(resultCount).isEqualTo(expectedCount);
        Mockito.verify(mongoTemplate).count(Mockito.eq(query), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testFindSignalStateEventAssessments() {
        Query query = new Query();
        List<StopLinePassageAssessment> expected = new ArrayList<>();

        Mockito.doReturn(expected).when(mongoTemplate).find(query, StopLinePassageAssessment.class,
                "CmSignalStateEventAssessments");

        List<StopLinePassageAssessment> results = repository.find(query);

        assertThat(results).isEqualTo(expected);
    }

}