package us.dot.its.jpo.ode.api.accessorTests.spat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.bson.Document;

import us.dot.its.jpo.ode.api.accessors.spat.OdeSpatDataRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.model.OdeSpatData;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class OdeSpatDataRepositoryImplTest {

    @SpyBean
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<AggregationResult> mockAggregationResult;

    @Mock
    private Page<Document> mockDocumentPage;

    @Mock
    private Page<OdeSpatData> mockPage;

    @InjectMocks
    private OdeSpatDataRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new OdeSpatDataRepositoryImpl(mongoTemplate);
    }

    @Test
    public void testCount() {
        long expectedCount = 10;

        doReturn(expectedCount).when(mongoTemplate).count(any(),
                Mockito.<String>any());

        long resultCount = repository.count(1, null, null);

        assertThat(resultCount).isEqualTo(expectedCount);
        verify(mongoTemplate).count(any(Query.class), anyString());
    }
}
