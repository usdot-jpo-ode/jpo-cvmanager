package us.dot.its.jpo.ode.api.accessorTests.events;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent.SpatMinimumDataEventRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.ode.api.models.IDCount;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class SpatMinimumDataEventRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SpatMinimumDataEventRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new SpatMinimumDataEventRepositoryImpl(mongoTemplate);
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

        @SuppressWarnings("rawtypes")
        Page expected = Mockito.mock(Page.class);
        SpatMinimumDataEventRepositoryImpl repo = mock(SpatMinimumDataEventRepositoryImpl.class);

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

        Page<SpatMinimumDataEvent> results = repo.find(1, null, null, pageRequest);

        assertThat(results).isEqualTo(expected);
    }

    @Test
    public void testGetSpatMinimumDataEventsByDay() {

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

        List<IDCount> actualResults = repository.getAggregatedDailySpatMinimumDataEventCounts(intersectionID, startTime,
                endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
        assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    }

    @Test
    @Disabled("TODO: Update for use with typesafe implementation")
    public void testFindWithData() throws IOException {
        // Load sample JSON data
//        TypeReference<List<LinkedHashMap<String, Object>>> hashMapList = new TypeReference<>() {
//        };
//        String json = new String(
//                Files.readAllBytes(
//                        Paths.get("src/test/resources/json/ConflictMonitor.CmSpatMinimumDataEvents.json")));
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Register
//                                                                                                 // JavaTimeModule
//
//        List<LinkedHashMap<String, Object>> sampleDocuments = objectMapper.readValue(json, hashMapList);
//
//        // Mock dependencies
//        Page<LinkedHashMap<String, Object>> mockHashMapPage = Mockito.mock(Page.class);
//        when(mockHashMapPage.getContent()).thenReturn(sampleDocuments);
//        when(mockHashMapPage.getTotalElements()).thenReturn(1L);
//
//        AggregationResult<LinkedHashMap<String, Object>> aggregationResult = new AggregationResult<>();
//        aggregationResult.setResults(sampleDocuments);
//        AggregationResultCount aggregationResultCount = new AggregationResultCount();
//        aggregationResultCount.setCount(1L);
//        aggregationResult.setMetadata(List.of(aggregationResultCount));
//
//        @SuppressWarnings("rawtypes")
//        AggregationResults mockAggregationResult = Mockito.mock(AggregationResults.class);
//        when(mockAggregationResult.getUniqueMappedResult()).thenReturn(aggregationResult);
//
//        ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
//        when(mongoTemplate.aggregate(aggregationCaptor.capture(), Mockito.<String>any(), any()))
//                .thenReturn(mockAggregationResult);
//
//        // Call the repository find method
//        PageRequest pageRequest = PageRequest.of(0, 1);
//        Page<SpatMinimumDataEvent> findResponse = repository.find(intersectionID, startTime, endTime,
//                pageRequest);
//
//        // Extract the captured Aggregation
//        Aggregation capturedAggregation = aggregationCaptor.getValue();
//
//        // Extract the MatchOperation from the Aggregation pipeline
//        Document pipeline = capturedAggregation.toPipeline(Aggregation.DEFAULT_CONTEXT).get(0);
//
//        // Assert the Match operation Criteria
//        assertThat(pipeline.toJson())
//                .isEqualTo(String.format(
//                        "{\"$match\": {\"intersectionID\": %s, \"eventGeneratedAt\": {\"$gte\": {\"$date\": \"%s\"}, \"$lte\": {\"$date\": \"%s\"}}}}",
//                        intersectionID, startTimeString, endTimeString));
//
//        // Serialize results to JSON and compare with the original JSON
//        String resultJson = objectMapper.writeValueAsString(findResponse.getContent().get(0));
//
//        // Remove unused fields from each entry
//        List<LinkedHashMap<String, Object>> expectedResult = sampleDocuments.stream().map(doc -> {
//            doc.remove("_id");
//            doc.remove("recordGeneratedAt");
//            return doc;
//        }).toList();
//        String expectedJson = objectMapper.writeValueAsString(expectedResult.get(0));
//
//        // Compare JSON with ignored fields
//        JSONAssert.assertEquals(expectedJson, resultJson, new CustomComparator(
//                JSONCompareMode.LENIENT, // Allows different key orders
//                new Customization("properties.timeStamp", (o1, o2) -> true),
//                new Customization("properties.odeReceivedAt", (o1, o2) -> true)));
    }

}