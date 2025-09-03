package us.dot.its.jpo.ode.api.accessorTests.map;

import org.junit.jupiter.api.Test;
import org.bson.Document;
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
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import us.dot.its.jpo.ode.api.accessors.map.OdeMapDataRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.model.OdeMapData;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class OdeMapDataRepositoryImplTest {

    @SpyBean
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<AggregationResult> mockAggregationResult;

    @Mock
    private Page<Document> mockDocumentPage;

    @Mock
    private Page<OdeMapData> mockPage;

    @InjectMocks
    private OdeMapDataRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new OdeMapDataRepositoryImpl(mongoTemplate);
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

    @Test
    public void testFind() {
        OdeMapDataRepositoryImpl repo = mock(OdeMapDataRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                eq(OdeMapData.class))).thenReturn(mockPage);
        PageRequest pageRequest = PageRequest.of(0, 1);
        doCallRealMethod().when(repo).find(1, null, null, pageRequest);

        Page<OdeMapData> results = repo.find(1, null, null, pageRequest);

        assertThat(results).isEqualTo(mockPage);
    }

    @Test
    void testFindLatest() {
        OdeMapData event = new OdeMapData();

        doReturn(event).when(mongoTemplate).findOne(any(Query.class), eq(OdeMapData.class),
                anyString());

        Page<OdeMapData> page = repository.findLatest(intersectionID, startTime, endTime);

        assertThat(page.getContent()).hasSize(1);
        verify(mongoTemplate).findOne(any(Query.class), eq(OdeMapData.class),
                eq("OdeMapJson"));
    }

    @Test
    void testAdd() {
        OdeMapData event = new OdeMapData();

        doReturn(null).when(mongoTemplate).insert(any(OdeMapData.class), anyString());

        repository.add(event);

        verify(mongoTemplate).insert(event, "OdeMapJson");
    }
}