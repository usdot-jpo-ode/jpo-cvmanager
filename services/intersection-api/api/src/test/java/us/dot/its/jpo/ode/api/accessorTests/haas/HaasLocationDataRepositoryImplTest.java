package us.dot.its.jpo.ode.api.accessorTests.haas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import us.dot.its.jpo.ode.api.accessors.haas.HaasLocationDataRepositoryImpl;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.models.haas.HaasLocationResult;
import us.dot.its.jpo.ode.mockdata.MockHaasGenerator;

@ExtendWith(MockitoExtension.class)
public class HaasLocationDataRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private HaasLocationDataRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new HaasLocationDataRepositoryImpl(mongoTemplate);
    }

    @Test
    void testFindWithLimit_ActiveOnly() {
        boolean activeOnly = true;
        Long startTime = 1000L;
        Long endTime = 2000L;
        int limit = 10;

        List<HaasLocation> mockLocations = MockHaasGenerator.getHaasLocations();
        AggregationResults<HaasLocation> mockMainResults = new AggregationResults<>(mockLocations,
                new Document());
        AggregationResults<Document> mockInactiveResults = new AggregationResults<>(new ArrayList<>(),
                new Document());

        // Stub both aggregate calls that happen when activeOnly = true
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(HaasLocation.class)))
                .thenReturn(mockMainResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(Document.class)))
                .thenReturn(mockInactiveResults);

        HaasLocationResult result = repository.findWithLimit(activeOnly, startTime, endTime, limit);

        assertNotNull(result);
        assertEquals(mockLocations.size(), result.getLocations().size());
        assertFalse(result.isHasMoreResults());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"),
                eq(HaasLocation.class));
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(Document.class));
    }

    @Test
    void testFindWithLimit_NotActiveOnly() {
        boolean activeOnly = false;
        Long startTime = 1000L;
        Long endTime = 2000L;
        int limit = 5;

        List<HaasLocation> mockLocations = MockHaasGenerator.getHaasLocations();
        AggregationResults<HaasLocation> mockMainResults = new AggregationResults<>(mockLocations,
                new Document());

        // When activeOnly = false, only one aggregate call is made
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(HaasLocation.class)))
                .thenReturn(mockMainResults);

        HaasLocationResult result = repository.findWithLimit(activeOnly, startTime, endTime, limit);

        assertNotNull(result);
        assertEquals(mockLocations.size(), result.getLocations().size());
        assertFalse(result.isHasMoreResults());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"),
                eq(HaasLocation.class));
    }

    @Test
    void testFindWithLimit_WithTruncation() {
        boolean activeOnly = true;
        Long startTime = 1000L;
        Long endTime = 2000L;
        int limit = 1;

        List<HaasLocation> mockLocations = MockHaasGenerator.getHaasLocations();
        // Add one more location than the limit to simulate truncation
        mockLocations.add(MockHaasGenerator.getHaasLocations().getFirst());

        AggregationResults<HaasLocation> mockMainResults = new AggregationResults<>(mockLocations,
                new Document());
        AggregationResults<Document> mockInactiveResults = new AggregationResults<>(new ArrayList<>(),
                new Document());

        // Stub both aggregate calls that happen when activeOnly = true
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(HaasLocation.class)))
                .thenReturn(mockMainResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(Document.class)))
                .thenReturn(mockInactiveResults);

        HaasLocationResult result = repository.findWithLimit(activeOnly, startTime, endTime, limit);

        assertNotNull(result);
        assertEquals(limit, result.getLocations().size());
        assertTrue(result.isHasMoreResults());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"),
                eq(HaasLocation.class));
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(Document.class));
    }

    @Test
    void testAdd() {
        HaasLocation location = MockHaasGenerator.getHaasLocations().getFirst();

        repository.add(location);

        verify(mongoTemplate).insert(location, "HaasAlertLocation");
    }

    @Test
    void testFindWithLimit_NoTimeWindow() {
        boolean activeOnly = true;
        Long startTime = null;
        Long endTime = null;
        int limit = 10;

        List<HaasLocation> mockLocations = MockHaasGenerator.getHaasLocations();
        AggregationResults<HaasLocation> mockMainResults = new AggregationResults<>(mockLocations,
                new Document());
        AggregationResults<Document> mockInactiveResults = new AggregationResults<>(new ArrayList<>(),
                new Document());

        // Stub both aggregate calls that happen when activeOnly = true
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(HaasLocation.class)))
                .thenReturn(mockMainResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(Document.class)))
                .thenReturn(mockInactiveResults);

        HaasLocationResult result = repository.findWithLimit(activeOnly, startTime, endTime, limit);

        assertNotNull(result);
        assertEquals(mockLocations.size(), result.getLocations().size());
        assertFalse(result.isHasMoreResults());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"),
                eq(HaasLocation.class));
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("HaasAlertLocation"), eq(Document.class));
    }
}