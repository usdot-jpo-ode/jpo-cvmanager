package us.dot.its.jpo.ode.api.accessorTests.events;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepositoryImpl;
import us.dot.its.jpo.ode.api.models.IDCount;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ConnectionOfTravelEventRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ConnectionOfTravelEventRepositoryImpl repository;

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
        Document queryTimeDocument = (Document)query.getQueryObject().get("eventGeneratedAt");
        assertThat(queryTimeDocument.getDate("$gte")).isEqualTo(new Date(startTime));
        assertThat(queryTimeDocument.getDate("$lte")).isEqualTo(new Date(endTime));


        // Assert sorting and limit
        assertThat(query.getSortObject().keySet().contains("eventGeneratedAt")).isTrue();
        assertThat(query.getSortObject().get("eventGeneratedAt")).isEqualTo(-1);
        assertThat(query.getLimit()).isEqualTo(1);

    }

    @Test
    public void testGetQueryResultCount() {
        Query query = new Query();
        long expectedCount = 10;

        Mockito.when(mongoTemplate.count(Mockito.eq(query), Mockito.any(), Mockito.anyString())).thenReturn(expectedCount);

        long resultCount = repository.getQueryResultCount(query);

        assertThat(resultCount).isEqualTo(expectedCount);
        Mockito.verify(mongoTemplate).count(Mockito.eq(query), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testFindConnectionOfTravelEvents() {
        Query query = new Query();
        List<ConnectionOfTravelEvent> expected = new ArrayList<>();

        Mockito.doReturn(expected).when(mongoTemplate).find(query, ConnectionOfTravelEvent.class, "CmConnectionOfTravelEvents");

        List<ConnectionOfTravelEvent> results = repository.find(query);

        assertThat(results).isEqualTo(expected);
    }

    @Test
    public void testGetConnectionOfTravelEventsByDay() {

        List<IDCount> aggregatedResults = new ArrayList<>();
        IDCount result1 = new IDCount();
        result1.setId("2023-06-26");
        result1.setCount(3600);
        IDCount result2 = new IDCount();
        result2.setId("2023-06-26");
        result2.setCount(7200);
        aggregatedResults.add(result1);
        aggregatedResults.add(result2);

         


        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults,  new Document());
        Mockito.when(mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(), Mockito.eq(IDCount.class))).thenReturn(aggregationResults);

        List<IDCount> actualResults = repository.getConnectionOfTravelEventsByDay(intersectionID, startTime, endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
        assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    }

}