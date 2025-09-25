package us.dot.its.jpo.ode.api.accessorTests.events;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.ode.api.accessors.events.bsm_event.BsmEventRepositoryImpl;
import us.dot.its.jpo.ode.api.models.IDCount;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class BsmEventRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private BsmEventRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
    boolean latest = true;
    String collectionName = "CmBsmEvents";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new BsmEventRepositoryImpl(mongoTemplate);
    }

    // @Test
    // public void testGetQuery() {
    // Page expected = Mockito.mock(Page.class);
    // ConnectionOfTravelNotificationRepositoryImpl repo =
    // mock(ConnectionOfTravelNotificationRepositoryImpl.class);

    // when(repo.findPage(
    // any(),
    // any(),
    // any(PageRequest.class),
    // any(Criteria.class),
    // any(Sort.class))).thenReturn(expected);
    // PageRequest pageRequest = PageRequest.of(0, 1);
    // doCallRealMethod().when(repo).find(1, null, null, pageRequest);

    // // Assert IntersectionID
    // assertThat(query.getQueryObject().get("intersectionID")).isEqualTo(intersectionID);

    // // // Assert Start and End Time
    // Document queryTimeDocument = (Document)
    // query.getQueryObject().get("startingBsmTimestamp");
    // assertThat(queryTimeDocument.getLong("$gte")).isEqualTo(startTime);
    // assertThat(queryTimeDocument.getLong("$lte")).isEqualTo(endTime);

    // // // Assert sorting and limit
    // assertThat(query.getSortObject().keySet().contains("startingBsmTimestamp")).isTrue();
    // assertThat(query.getSortObject().get("startingBsmTimestamp")).isEqualTo(-1);
    // assertThat(query.getLimit()).isEqualTo(1);

    // }

    // @Test
    // public void testGetQueryResultCount() {
    // Query query = new Query();
    // long expectedCount = 10;

    // Mockito.when(mongoTemplate.count(Mockito.eq(query), Mockito.any(),
    // Mockito.anyString()))
    // .thenReturn(expectedCount);

    // long resultCount = repository.getQueryResultCount(query);

    // assertThat(resultCount).isEqualTo(expectedCount);
    // Mockito.verify(mongoTemplate).count(Mockito.eq(query), Mockito.any(),
    // Mockito.anyString());
    // }

    // @Test
    // public void testFindBsms() {
    // Query query = new Query();
    // List<BsmEvent> expectedBsms = new ArrayList<>();

    // Mockito.doReturn(expectedBsms).when(mongoTemplate).find(query,
    // BsmEvent.class, "CmBsmEvents");

    // List<BsmEvent> resultBsms = repository.find(query);

    // assertThat(resultBsms).isEqualTo(expectedBsms);
    // }

    @Test
    public void testGetBsmEventsByDay() {

        List<IDCount> aggregatedResults = new ArrayList<>();
        IDCount result1 = new IDCount();
        result1.setId("2023-06-26");
        result1.setCount(3600);
        IDCount result2 = new IDCount();
        result2.setId("2023-06-26");
        result2.setCount(7200);
        aggregatedResults.add(result1);
        aggregatedResults.add(result2);

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults, new Document());
        Mockito.when(
                mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(), Mockito.eq(IDCount.class)))
                .thenReturn(aggregationResults);

        List<IDCount> actualResults = repository.getAggregatedDailyBsmEventCounts(intersectionID, startTime, endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
        assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    }

    @Test
    public void testCountWithAllParameters() {
        // Arrange
        Integer intersectionID = 123;
        Long startTime = 1622505600000L; // Example start time
        Long endTime = 1622592000000L; // Example end time

        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(42L);

        // Act
        long result = repository.count(intersectionID, startTime, endTime);

        // Assert
        assertEquals(42L, result);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).count(queryCaptor.capture(), eq(collectionName));

        // Get the captured Query
        Query capturedQuery = queryCaptor.getValue();

        // Verify the Criteria in the Query
        Document queryObject = capturedQuery.getQueryObject();

        // Verify intersectionID condition
        assertThat(queryObject.get("intersectionID")).isEqualTo(intersectionID);

        // Verify time window condition
        Document timeCondition = (Document) queryObject.get("startingBsmTimestamp");
        assertThat(timeCondition).isNotNull();
        assertThat(timeCondition.get("$gte")).isEqualTo(Date.from(Instant.ofEpochMilli(startTime)));
        assertThat(timeCondition.get("$lte")).isEqualTo(Date.from(Instant.ofEpochMilli(endTime)));
    }

    @Test
    public void testCountWithoutPageable() {
        // Arrange
        Integer intersectionID = 123;
        Long startTime = 1622505600000L; // Example start time
        Long endTime = 1622592000000L; // Example end time

        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(25L);

        // Act
        long result = repository.count(intersectionID, startTime, endTime);

        // Assert
        assertEquals(25L, result);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).count(queryCaptor.capture(), eq(collectionName));

        // Get the captured Query
        Query capturedQuery = queryCaptor.getValue();

        // Verify the Criteria in the Query
        Document queryObject = capturedQuery.getQueryObject();

        // Verify intersectionID condition
        assertThat(queryObject.get("intersectionID")).isEqualTo(intersectionID);

        // Verify time window condition
        Document timeCondition = (Document) queryObject.get("startingBsmTimestamp");
        assertThat(timeCondition).isNotNull();
        assertThat(timeCondition.get("$gte")).isEqualTo(Date.from(Instant.ofEpochMilli(startTime)));
        assertThat(timeCondition.get("$lte")).isEqualTo(Date.from(Instant.ofEpochMilli(endTime)));
    }

    @Test
    public void testCountWithNullIntersectionID() {
        // Arrange
        Integer intersectionID = null;
        Long startTime = 1622505600000L; // Example start time
        Long endTime = 1622592000000L; // Example end time

        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(15L);

        // Act
        long result = repository.count(intersectionID, startTime, endTime);

        // Assert
        assertEquals(15L, result);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).count(queryCaptor.capture(), eq(collectionName));

        // Get the captured Query
        Query capturedQuery = queryCaptor.getValue();

        // Verify the Criteria in the Query
        Document queryObject = capturedQuery.getQueryObject();

        // Verify intersectionID condition
        assertThat(queryObject.get("intersectionID")).isEqualTo(intersectionID);

        // Verify time window condition
        Document timeCondition = (Document) queryObject.get("startingBsmTimestamp");
        assertThat(timeCondition).isNotNull();
        assertThat(timeCondition.get("$gte")).isEqualTo(Date.from(Instant.ofEpochMilli(startTime)));
        assertThat(timeCondition.get("$lte")).isEqualTo(Date.from(Instant.ofEpochMilli(endTime)));
    }

    @Test
    public void testCountWithNullStartAndEndTime() {
        // Arrange
        Integer intersectionID = 123;
        Long startTime = null;
        Long endTime = null;

        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(10L);

        // Act
        long result = repository.count(intersectionID, startTime, endTime);

        // Assert
        assertEquals(10L, result);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).count(queryCaptor.capture(), eq(collectionName));

        // Get the captured Query
        Query capturedQuery = queryCaptor.getValue();

        // Verify the Criteria in the Query
        Document queryObject = capturedQuery.getQueryObject();

        // Verify intersectionID condition
        assertThat(queryObject.get("intersectionID")).isEqualTo(intersectionID);

        // Verify time window condition
        Document timeCondition = (Document) queryObject.get("startingBsmTimestamp");
        assertThat(timeCondition).isNull();
    }

    @Test
    public void testCountWithNoParameters() {
        // Arrange
        Integer intersectionID = null;
        Long startTime = null;
        Long endTime = null;

        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(5L);

        // Act
        long result = repository.count(intersectionID, startTime, endTime);

        // Assert
        assertEquals(5L, result);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).count(queryCaptor.capture(), eq(collectionName));

        // Get the captured Query
        Query capturedQuery = queryCaptor.getValue();

        // Verify the Criteria in the Query
        Document queryObject = capturedQuery.getQueryObject();

        // Verify intersectionID condition
        assertThat(queryObject.get("intersectionID")).isEqualTo(intersectionID);

        // Verify time window condition
        Document timeCondition = (Document) queryObject.get("startingBsmTimestamp");
        assertThat(timeCondition).isNull();
    }

    @Test
    void testFindLatest() {
        BsmEvent event = new BsmEvent();
        event.setIntersectionID(intersectionID);

        doReturn(event).when(mongoTemplate).findOne(any(Query.class), eq(BsmEvent.class),
                anyString());

        Page<BsmEvent> page = repository.findLatest(intersectionID, startTime, endTime);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getIntersectionID()).isEqualTo(intersectionID);
        verify(mongoTemplate).findOne(any(Query.class), eq(BsmEvent.class),
                eq("CmBsmEvents"));
    }
}