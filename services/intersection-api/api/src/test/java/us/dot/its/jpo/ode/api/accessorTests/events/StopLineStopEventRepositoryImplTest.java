package us.dot.its.jpo.ode.api.accessorTests.events;

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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.ode.api.accessors.events.StopLineStopEvent.StopLineStopEventRepositoryImpl;
import us.dot.its.jpo.ode.api.models.IDCount;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class StopLineStopEventRepositoryImplTest {

        @Mock
        private MongoTemplate mongoTemplate;

        @Mock
        private Page<StopLineStopEvent> mockPage;

        @InjectMocks
        private StopLineStopEventRepositoryImpl repository;

        Integer intersectionID = 123;
        Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
        Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
        boolean latest = true;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                repository = new StopLineStopEventRepositoryImpl(mongoTemplate);
        }

        @Test
        public void testCount() {
                long expectedCount = 10;

                when(mongoTemplate.count(any(),
                                Mockito.<String>any())).thenReturn(expectedCount);

                long resultCount = repository.count(1, null, null);

                assertThat(resultCount).isEqualTo(expectedCount);
                verify(mongoTemplate).count(any(Query.class), anyString());
        }

        @Test

        public void testFind() {
                StopLineStopEventRepositoryImpl repo = mock(StopLineStopEventRepositoryImpl.class);

                when(repo.findPage(
                                any(),
                                any(),
                                any(PageRequest.class),
                                any(Criteria.class),
                                any(Sort.class),
                                any(),
                                eq(StopLineStopEvent.class))).thenReturn(mockPage);
                PageRequest pageRequest = PageRequest.of(0, 1);
                doCallRealMethod().when(repo).find(1, null, null, pageRequest);

                Page<StopLineStopEvent> results = repo.find(1, null, null, pageRequest);

                assertThat(results).isEqualTo(mockPage);
        }

        @Test
        public void testGetStopLineStopEventEventsByDay() {

                List<IDCount> aggregatedResults = new ArrayList<>();
                IDCount result1 = new IDCount();
                result1.setId("2023-06-26");
                result1.setCount(3600);
                IDCount result2 = new IDCount();
                result2.setId("2023-06-26");
                result2.setCount(7200);
                aggregatedResults.add(result1);
                aggregatedResults.add(result2);

                AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults,
                                new Document());
                Mockito.when(
                                mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(),
                                                Mockito.eq(IDCount.class)))
                                .thenReturn(aggregationResults);

                List<IDCount> actualResults = repository.getAggregatedDailyStopLineStopEventCounts(intersectionID,
                                startTime,
                                endTime);

                assertThat(actualResults.size()).isEqualTo(2);
                assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26");
                assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
                assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26");
                assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
        }

}