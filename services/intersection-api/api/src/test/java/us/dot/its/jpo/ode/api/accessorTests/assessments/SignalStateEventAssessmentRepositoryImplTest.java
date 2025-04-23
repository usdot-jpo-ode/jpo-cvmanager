package us.dot.its.jpo.ode.api.accessorTests.assessments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new SignalStateEventAssessmentRepositoryImpl(mongoTemplate);
    }

    @Test
    public void testCount() {
        long expectedCount = 10;

        when(mongoTemplate.count(any(),
                Mockito.<String>any())).thenReturn(expectedCount);

        PageRequest pageRequest = PageRequest.of(0, 1);
        long resultCount = repository.count(1, null, null, pageRequest);

        assertThat(resultCount).isEqualTo(expectedCount);
        verify(mongoTemplate).count(any(Query.class), anyString());
    }

    @Test
    public void testFind() {

        @SuppressWarnings("rawtypes")
        Page expected = Mockito.mock(Page.class);
        SignalStateEventAssessmentRepositoryImpl repo = mock(SignalStateEventAssessmentRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                any())).thenReturn(expected);
        PageRequest pageRequest = PageRequest.of(0, 1);
        doCallRealMethod().when(repo).find(1, null, null, pageRequest);

        Page<StopLinePassageAssessment> results = repo.find(1, null, null, pageRequest);

        assertThat(results).isEqualTo(expected);
    }

}