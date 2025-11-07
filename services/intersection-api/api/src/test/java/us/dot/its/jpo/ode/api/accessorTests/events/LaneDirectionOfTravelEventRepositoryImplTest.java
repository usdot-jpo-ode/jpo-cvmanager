package us.dot.its.jpo.ode.api.accessorTests.events;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.ode.api.accessors.events.lane_direction_of_travel_event.LaneDirectionOfTravelEventRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.ode.api.models.IDCount;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class LaneDirectionOfTravelEventRepositoryImplTest {

    @MockitoSpyBean
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<AggregationResult> mockAggregationResult;

    @Mock
    private Page<Document> mockDocumentPage;

    @Mock
    private Page<LaneDirectionOfTravelEvent> mockPage;

    @InjectMocks
    private LaneDirectionOfTravelEventRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new LaneDirectionOfTravelEventRepositoryImpl(mongoTemplate);
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
        LaneDirectionOfTravelEventRepositoryImpl repo = mock(LaneDirectionOfTravelEventRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                eq(LaneDirectionOfTravelEvent.class))).thenReturn(mockPage);
        PageRequest pageRequest = PageRequest.of(0, 1);
        doCallRealMethod().when(repo).find(1, null, null, pageRequest);

        Page<LaneDirectionOfTravelEvent> results = repo.find(1, null, null, pageRequest);

        assertThat(results).isEqualTo(mockPage);
    }

    @Test
    public void testGetLaneDirectionOfTravelEventsByDay() {

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
        doReturn(aggregationResults).when(
                mongoTemplate)
                .aggregate(Mockito.any(Aggregation.class), Mockito.anyString(),
                        Mockito.eq(IDCount.class));

        List<IDCount> actualResults = repository.getAggregatedDailyLaneDirectionOfTravelEventCounts(
                intersectionID,
                startTime, endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
        assertThat(actualResults.get(1).getId()).isEqualTo("2023-06-26");
        assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    }

    @Test
    public void testFindWithData() throws IOException {
        // Load sample JSON data
        String json = new String(
                Files.readAllBytes(
                        Paths.get("src/test/resources/json/ConflictMonitor.CmLaneDirectionOfTravelEvent.json")));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Register
                                                                                                 // JavaTimeModule

        List<Document> sampleDocuments = List.of(Document.parse(json));

        // Mock dependencies
        when(mockDocumentPage.getContent()).thenReturn(sampleDocuments);
        when(mockDocumentPage.getTotalElements()).thenReturn(1L);

        AggregationResult aggregationResult = new AggregationResult();
        aggregationResult.setResults(sampleDocuments);
        AggregationResultCount aggregationResultCount = new AggregationResultCount();
        aggregationResultCount.setCount(1L);
        aggregationResult.setMetadata(List.of(aggregationResultCount));

        when(mockAggregationResult.getUniqueMappedResult()).thenReturn(aggregationResult);

        ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
        doReturn(mockAggregationResult).when(mongoTemplate).aggregate(aggregationCaptor.capture(),
                anyString(),
                eq(AggregationResult.class));

        // Call the repository find method
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<LaneDirectionOfTravelEvent> findResponse = repository.find(intersectionID, startTime, endTime,
                pageRequest);

        // Extract the captured Aggregation
        Aggregation capturedAggregation = aggregationCaptor.getValue();

        // Extract the MatchOperation from the Aggregation pipeline
        Document pipeline = capturedAggregation.toPipeline(Aggregation.DEFAULT_CONTEXT).getFirst();

        // Assert the Match operation Criteria
        assertThat(pipeline.toJson())
                .isEqualTo(String.format(
                        "{\"$match\": {\"intersectionID\": %s, \"eventGeneratedAt\": {\"$gte\": {\"$date\": \"%s\"}, \"$lte\": {\"$date\": \"%s\"}}}}",
                        intersectionID, startTimeString, endTimeString));

        // Serialize results to JSON and compare with the original JSON
        String resultJson = objectMapper.writeValueAsString(findResponse.getContent().getFirst());

        // Remove unused fields from each entry
        List<Document> expectedResult = sampleDocuments.stream().map(doc -> {
            doc.remove("_id");
            doc.remove("recordGeneratedAt");
            return doc;
        }).toList();
        String expectedJson = objectMapper.writeValueAsString(expectedResult.getFirst());

        // Compare JSON with ignored fields
        JSONAssert.assertEquals(expectedJson, resultJson, new CustomComparator(
                JSONCompareMode.LENIENT, // Allows different key orders
                new Customization("properties.timeStamp", (_, _) -> true),
                new Customization("properties.odeReceivedAt", (_, _) -> true)));
    }

    @Test
    void testCountEventsByCenterlineDistance() {
        List<IDCount> expectedCounts = new ArrayList<>();
        IDCount count1 = new IDCount();
        count1.setId("5.0");
        count1.setCount(10);
        expectedCounts.add(count1);

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(expectedCounts,
                new Document());
        doReturn(aggregationResults).when(mongoTemplate).aggregate(any(Aggregation.class), anyString(),
                eq(IDCount.class));

        List<IDCount> result = repository.countEventsByCenterlineDistance(intersectionID, startTime, endTime);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("5.0");
        assertThat(result.getFirst().getCount()).isEqualTo(10);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("CmLaneDirectionOfTravelEvent"),
                eq(IDCount.class));
    }

    @Test
    void testGetMedianDistanceByDegree() {
        List<IDCount> expectedCounts = new ArrayList<>();
        IDCount count1 = new IDCount();
        count1.setId("15.0");
        count1.setCount(7);
        expectedCounts.add(count1);

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(expectedCounts,
                new Document());
        doReturn(aggregationResults).when(mongoTemplate).aggregate(any(Aggregation.class), anyString(),
                eq(IDCount.class));

        List<IDCount> result = repository.getMedianDistanceByDegree(intersectionID, startTime, endTime);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("15.0");
        assertThat(result.getFirst().getCount()).isEqualTo(7);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("CmLaneDirectionOfTravelEvent"),
                eq(IDCount.class));
    }

    @Test
    void testFindLatest() {
        LaneDirectionOfTravelEvent event = new LaneDirectionOfTravelEvent();
        event.setIntersectionID(intersectionID);

        doReturn(event).when(mongoTemplate).findOne(any(Query.class), eq(LaneDirectionOfTravelEvent.class),
                anyString());

        Page<LaneDirectionOfTravelEvent> page = repository.findLatest(intersectionID, startTime, endTime);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getIntersectionID()).isEqualTo(intersectionID);
        verify(mongoTemplate).findOne(any(Query.class), eq(LaneDirectionOfTravelEvent.class),
                eq("CmLaneDirectionOfTravelEvent"));
    }
}