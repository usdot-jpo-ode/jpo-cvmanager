package us.dot.its.jpo.ode.api.accessorTests.haas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.accessors.haas.HaasLocationDataRepositoryImpl;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

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
    void testCount() {
        Long startTime = 1000L;
        Long endTime = 2000L;
        boolean activeOnly = true;
        PageRequest pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(Query.class), eq("HaasAlertLocation"))).thenReturn(5L);

        long count = repository.count(activeOnly, startTime, endTime, pageable);

        assertEquals(5L, count);
        verify(mongoTemplate).count(any(Query.class), eq("HaasAlertLocation"));
    }

    @Test
    void testFind_ActiveOnly() {
        Long startTime = 1000L;
        Long endTime = 2000L;
        boolean activeOnly = true;
        PageRequest pageable = PageRequest.of(0, 10);

        HaasLocation location1 = new HaasLocation();
        HaasLocation location2 = new HaasLocation();
        List<HaasLocation> mockResults = Arrays.asList(location1, location2);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockInactiveResults = mock(AggregationResults.class);
        when(mockInactiveResults.getMappedResults()).thenReturn(Arrays.asList());
        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                eq(Document.class))).thenReturn(mockInactiveResults);

        @SuppressWarnings("unchecked")
        AggregationResults<HaasLocation> mockMainResults = mock(AggregationResults.class);
        when(mockMainResults.getMappedResults()).thenReturn(mockResults);
        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                eq(HaasLocation.class))).thenReturn(mockMainResults);

        Page<HaasLocation> result = repository.find(activeOnly, startTime, endTime, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(mongoTemplate, times(3)).aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                any());
    }

    @Test
    void testFind_NotActiveOnly() {
        Long startTime = 1000L;
        Long endTime = 2000L;
        boolean activeOnly = false;
        PageRequest pageable = PageRequest.of(0, 10);

        HaasLocation location = new HaasLocation();
        List<HaasLocation> mockResults = Arrays.asList(location);

        @SuppressWarnings("unchecked")
        AggregationResults<HaasLocation> mockAggResults = mock(AggregationResults.class);
        when(mockAggResults.getMappedResults()).thenReturn(mockResults);

        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                eq(HaasLocation.class))).thenReturn(mockAggResults);

        Page<HaasLocation> result = repository.find(activeOnly, startTime, endTime, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(mongoTemplate, times(2)).aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                any());
    }

    @Test
    void testAdd() {
        HaasLocation location = new HaasLocation();

        repository.add(location);

        verify(mongoTemplate).insert(eq(location), eq("HaasAlertLocation"));
    }

    @Test
    void testFind_NoTimeWindow() {
        boolean activeOnly = true;
        PageRequest pageable = PageRequest.of(0, 10);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockInactiveResults = mock(AggregationResults.class);
        when(mockInactiveResults.getMappedResults()).thenReturn(Arrays.asList());
        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                eq(Document.class))).thenReturn(mockInactiveResults);

        HaasLocation location = new HaasLocation();
        List<HaasLocation> mockResults = Arrays.asList(location);
        @SuppressWarnings("unchecked")
        AggregationResults<HaasLocation> mockMainResults = mock(AggregationResults.class);
        when(mockMainResults.getMappedResults()).thenReturn(mockResults);
        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                eq(HaasLocation.class))).thenReturn(mockMainResults);

        Page<HaasLocation> result = repository.find(activeOnly, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(mongoTemplate, times(3)).aggregate(
                any(Aggregation.class),
                eq("HaasAlertLocation"),
                any());
    }
}