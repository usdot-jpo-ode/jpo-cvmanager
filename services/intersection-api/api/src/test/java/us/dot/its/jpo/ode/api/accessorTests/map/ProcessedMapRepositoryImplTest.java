package us.dot.its.jpo.ode.api.accessorTests.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.ode.api.models.IDCount;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

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
public class ProcessedMapRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProcessedMapRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ProcessedMapRepositoryImpl(mongoTemplate);
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
    public void testFind() throws IOException {

        TypeReference<List<LinkedHashMap<String, Object>>> hashMapList = new TypeReference<>() {
        };
        String json = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/ConflictMonitor.ProcessedMap.json")));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Register
                                                                                                 // JavaTimeModule

        List<LinkedHashMap<String, Object>> sampleDocuments = objectMapper.readValue(json, hashMapList);

        // Mock dependencies
        Page<LinkedHashMap<String, Object>> mockHashMapPage = Mockito.mock(Page.class);

        when(mockHashMapPage.getContent()).thenReturn(sampleDocuments);
        when(mockHashMapPage.getTotalElements()).thenReturn(1L);

        AggregationResult<LinkedHashMap<String, Object>> aggregationResult = new AggregationResult<>();
        aggregationResult.setResults(sampleDocuments);
        AggregationResultCount aggregationResultCount = new AggregationResultCount();
        aggregationResultCount.setCount(1L);
        aggregationResult.setMetadata(List.of(aggregationResultCount));

        @SuppressWarnings("rawtypes")
        AggregationResults mockAggregationResult = Mockito.mock(AggregationResults.class);
        when(mockAggregationResult.getUniqueMappedResult()).thenReturn(aggregationResult);

        when(mongoTemplate.aggregate(Mockito.<Aggregation>any(), Mockito.<String>any(), any()))
                .thenReturn(mockAggregationResult);

        MatchOperation matchOperation = Aggregation.match(new Criteria());
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation);
        System.out.println("MongoTemplate: " + mongoTemplate);
        @SuppressWarnings("rawtypes")
        AggregationResults<AggregationResult> results = mongoTemplate
                .aggregate(aggregation, "name", AggregationResult.class);
        System.out.println("Results: " + results.getMappedResults());

        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<ProcessedMap<LineString>> findResponse = repository.find(1, null, null, false, pageRequest);

        // serialize results to json then compare with the original json
        String resultJson = objectMapper.writeValueAsString(findResponse.getContent().get(0));

        // remove unused fields from each entry
        List<LinkedHashMap<String, Object>> expectedResult = sampleDocuments.stream().map(doc -> {
            doc.remove("_id");
            doc.remove("recordGeneratedAt");
            return doc;
        }).toList();
        String expectedJson = objectMapper.writeValueAsString(expectedResult.get(0));
        JSONAssert.assertEquals(expectedJson, resultJson, new CustomComparator(
                JSONCompareMode.LENIENT, // allows different key orders
                new Customization("properties.timeStamp", (o1, o2) -> true),
                new Customization("properties.odeReceivedAt", (o1, o2) -> true)));
    }

    @Test
    public void testGetMapBroadcastRates() {

        List<IDCount> aggregatedResults = new ArrayList<>();
        IDCount result1 = new IDCount();
        result1.setId("2023-06-26-01");
        result1.setCount(3600);
        IDCount result2 = new IDCount();
        result2.setId("2023-06-26-02");
        result2.setCount(7200);
        aggregatedResults.add(result1);
        aggregatedResults.add(result2);

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults, new Document());
        Mockito.when(
                mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(),
                        Mockito.eq(IDCount.class)))
                .thenReturn(aggregationResults);

        List<IDCount> actualResults = repository.getMapBroadcastRates(intersectionID,
                startTime, endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26-01");
        assertThat(actualResults.get(0).getCount()).isEqualTo(1);
        assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26-02");
        assertThat(actualResults.get(1).getCount()).isEqualTo(2);
    }

    @Test
    public void testGetMapBroadcastRateDistribution() {

        List<IDCount> aggregatedResults = new ArrayList<>();
        IDCount result1 = new IDCount();
        result1.setId("15");
        result1.setCount(3600);
        IDCount result2 = new IDCount();
        result2.setId("8");
        result2.setCount(7200);
        aggregatedResults.add(result1);
        aggregatedResults.add(result2);

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults, new Document());
        Mockito.when(
                mongoTemplate.aggregate(Mockito.any(Aggregation.class), Mockito.anyString(),
                        Mockito.eq(IDCount.class)))
                .thenReturn(aggregationResults);

        List<IDCount> actualResults = repository.getMapBroadcastRateDistribution(intersectionID, startTime,
                endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("15");
        assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
        assertThat(actualResults.get(1).getId()).isEqualTo("8");
        assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    }
}
