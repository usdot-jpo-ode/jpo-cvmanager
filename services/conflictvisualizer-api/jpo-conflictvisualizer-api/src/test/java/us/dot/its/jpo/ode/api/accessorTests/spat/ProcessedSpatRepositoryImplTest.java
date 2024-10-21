package us.dot.its.jpo.ode.api.accessorTests.spat;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepositoryImpl;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProcessedSpatRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProcessedSpatRepositoryImpl repository;

    @Mock
    private ConflictMonitorApiProperties props;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuery() {
    
        Query query = repository.getQuery(intersectionID, startTime, endTime, false, false);

        

        // Assert IntersectionID
        assertThat(query.getQueryObject().get("intersectionId")).isEqualTo(intersectionID);
        
        
        // Assert Start and End Time
        Document queryTimeDocument = (Document)query.getQueryObject().get("utcTimeStamp");
        assertThat(queryTimeDocument.getString("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
        assertThat(queryTimeDocument.getString("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

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
    public void testFindProcessedSpats() {
        Query query = new Query();
        List<ProcessedSpat> expectedSpats = new ArrayList<>();

        Mockito.doReturn(expectedSpats).when(mongoTemplate).find(query, ProcessedSpat.class, "ProcessedSpat");

        List<ProcessedSpat> resultSpats = repository.findProcessedSpats(query);

        assertThat(resultSpats).isEqualTo(expectedSpats);
    }


    // @Test
    // public void testGetSpatBroadcastRates() {

    //     List<IDCount> aggregatedResults = new ArrayList<>();
    //     IDCount result1 = new IDCount();
    //     result1.setId("2023-06-26-01");
    //     result1.setCount(3600);
    //     IDCount result2 = new IDCount();
    //     result2.setId("2023-06-26-02");
    //     result2.setCount(7200);
    //     aggregatedResults.add(result1);
    //     aggregatedResults.add(result2);

         


    //     AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults,  new Document());
    //     Mockito.when(mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(), Mockito.eq(IDCount.class))).thenReturn(aggregationResults);

    //     List<IDCount> actualResults = repository.getSpatBroadcastRates(intersectionID, startTime, endTime);

    //     assertThat(actualResults.size()).isEqualTo(2);
    //     assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26-01");
    //     assertThat(actualResults.get(0).getCount()).isEqualTo(1);
    //     assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26-02");
    //     assertThat(actualResults.get(1).getCount()).isEqualTo(2);
    // }

    // @Test
    // public void testGetSpatBroadcastRateDistribution() {

    //     List<IDCount> aggregatedResults = new ArrayList<>();
    //     IDCount result1 = new IDCount();
    //     result1.setId("150");
    //     result1.setCount(3600);
    //     IDCount result2 = new IDCount();
    //     result2.setId("80");
    //     result2.setCount(7200);
    //     aggregatedResults.add(result1);
    //     aggregatedResults.add(result2);

         


    //     AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults,  new Document());
    //     Mockito.when(mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(), Mockito.eq(IDCount.class))).thenReturn(aggregationResults);

    //     List<IDCount> actualResults = repository.getSpatBroadcastRateDistribution(intersectionID, startTime, endTime);

    //     assertThat(actualResults.size()).isEqualTo(2);
    //     assertThat(actualResults.get(0).getId()).isEqualTo("150");
    //     assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
    //     assertThat(actualResults.get(1).getId()).isEqualTo("80");
    //     assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    // }
}
