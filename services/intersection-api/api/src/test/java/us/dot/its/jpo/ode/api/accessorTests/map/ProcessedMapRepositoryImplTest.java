package us.dot.its.jpo.ode.api.accessorTests.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.CoordinateXY;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapBoundingBox;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapIndex;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapRefPoint;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapSharedProperties;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;

import org.springframework.data.mongodb.core.aggregation.Aggregation;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class ProcessedMapRepositoryImplTest {

    @MockitoSpyBean
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<AggregationResult> mockAggregationResult;

    @Mock
    private Page<Document> mockDocumentPage;

    @InjectMocks
    private ProcessedMapRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ProcessedMapRepositoryImpl(mongoTemplate);
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
    public void testFindWithData() throws IOException {
        // Load sample JSON data
        String json = new String(
                Files.readAllBytes(Paths
                        .get("src/test/resources/json/ConflictMonitor.ProcessedMap.json")));
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
        Page<ProcessedMap<LineString>> findResponse = repository.find(intersectionID, startTime, endTime,
                false, pageRequest);

        // Extract the captured Aggregation
        Aggregation capturedAggregation = aggregationCaptor.getValue();

        // Extract the MatchOperation from the Aggregation pipeline
        Document pipeline = capturedAggregation.toPipeline(Aggregation.DEFAULT_CONTEXT).get(0);

        // Assert the Match operation Criteria
        assertThat(pipeline.toJson())
                .isEqualTo(String.format(
                        "{\"$match\": {\"properties.intersectionId\": %s, \"properties.timeStamp\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}}}",
                        intersectionID, startTimeString, endTimeString));

        // Serialize results to JSON and compare with the original JSON
        String resultJson = objectMapper.writeValueAsString(findResponse.getContent().get(0));

        // Remove unused fields from each entry
        List<Document> expectedResult = sampleDocuments.stream().map(doc -> {
            doc.remove("_id");
            doc.remove("recordGeneratedAt");
            return doc;
        }).toList();
        String expectedJson = objectMapper.writeValueAsString(expectedResult.get(0));

        // Compare JSON with ignored fields
        JSONAssert.assertEquals(expectedJson, resultJson, new CustomComparator(
                JSONCompareMode.LENIENT, // Allows different key orders
                new Customization("properties.timeStamp", (o1, o2) -> true),
                new Customization("properties.odeReceivedAt", (o1, o2) -> true)));
    }

    @Test
    void testGetIntersectionsContainingPoint() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        ProcessedMapRepositoryImpl repo = new ProcessedMapRepositoryImpl(mongoTemplate);
        ProcessedMapRepositoryImpl spyRepo = Mockito.spy(repo);

        MongoCollection<Document> mockCollection = mock(MongoCollection.class);
        DistinctIterable<Integer> mockDistinct = mock(DistinctIterable.class);
        MongoCursor<Integer> mockCursor = mock(MongoCursor.class);

        when(mongoTemplate.getCollection(anyString())).thenReturn(mockCollection);
        when(mockCollection.distinct(anyString(), eq(Integer.class))).thenReturn(mockDistinct);
        when(mockDistinct.iterator()).thenReturn(mockCursor);

        // Simulate two intersection IDs
        when(mockCursor.hasNext()).thenReturn(true, true, false);
        when(mockCursor.next()).thenReturn(1, 2);

        // Mock findLatest to return a page with one ProcessedMap for each intersection
        ProcessedMap<LineString> map1 = mock(ProcessedMap.class);
        ProcessedMap<LineString> map2 = mock(ProcessedMap.class);

        // Mock properties for map1
        MapSharedProperties props1 = mock(MapSharedProperties.class);
        when(map1.getProperties()).thenReturn(props1);
        when(props1.getIntersectionId()).thenReturn(1);
        when(props1.getOriginIp()).thenReturn("1.1.1.1");
        when(props1.getIntersectionName()).thenReturn("Intersection1");
        MapRefPoint refPoint1 = mock(MapRefPoint.class);
        when(props1.getRefPoint()).thenReturn(refPoint1);
        when(refPoint1.getLatitude()).thenReturn(10.0);
        when(refPoint1.getLongitude()).thenReturn(20.0);

        // Mock properties for map2
        MapSharedProperties props2 = mock(MapSharedProperties.class);
        when(map2.getProperties()).thenReturn(props2);
        when(props2.getIntersectionId()).thenReturn(2);
        when(props2.getOriginIp()).thenReturn("2.2.2.2");
        when(props2.getIntersectionName()).thenReturn("Intersection2");
        MapRefPoint refPoint2 = mock(MapRefPoint.class);
        when(props2.getRefPoint()).thenReturn(refPoint2);
        when(refPoint2.getLatitude()).thenReturn(30.0);
        when(refPoint2.getLongitude()).thenReturn(40.0);

        // Return a page with the map for each intersection
        Page<ProcessedMap<LineString>> page1 = new PageImpl<>(List.of(map1));
        Page<ProcessedMap<LineString>> page2 = new PageImpl<>(List.of(map2));
        doReturn(page1).when(spyRepo).findLatest(eq(1), any(), any(), eq(true));
        doReturn(page2).when(spyRepo).findLatest(eq(2), any(), any(), eq(true));

        // Mock MapIndex and MapBoundingBox logic
        MapIndex mockIndex = mock(MapIndex.class);
        MapBoundingBox box1 = mock(MapBoundingBox.class);
        MapBoundingBox box2 = mock(MapBoundingBox.class);
        when(box1.getIntersectionId()).thenReturn(1);
        when(box2.getIntersectionId()).thenReturn(2);

        // Only box1 contains the point
        when(mockIndex.mapsContainingPoint(any(CoordinateXY.class))).thenReturn(List.of(box1, box2));

        try (MockedConstruction<MapIndex> mocked = Mockito.mockConstruction(MapIndex.class,
                (mock, context) -> {
                    when(mock.mapsContainingPoint(any(CoordinateXY.class)))
                            .thenReturn(List.of(box1));
                })) {

            // Call the method under test
            List<IntersectionReferenceData> result = spyRepo.getIntersectionsContainingPoint(20.0, 10.0);

            assertThat(result).hasSize(1);
            IntersectionReferenceData data = result.get(0);
            assertThat(data.getIntersectionID()).isEqualTo(1);
            assertThat(data.getRsuIP()).isEqualTo("1.1.1.1");
            assertThat(data.getIntersectionName()).isEqualTo("Intersection1");
            assertThat(data.getLatitude()).isEqualTo(10.0);
            assertThat(data.getLongitude()).isEqualTo(20.0);
        }
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

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults,
                new Document());
        doReturn(aggregationResults).when(
                mongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.anyString(),
                        Mockito.eq(IDCount.class));

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

        AggregationResults<IDCount> aggregationResults = new AggregationResults<>(aggregatedResults,
                new Document());
        doReturn(aggregationResults).when(
                mongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.anyString(),
                        Mockito.eq(IDCount.class));

        List<IDCount> actualResults = repository.getMapBroadcastRateDistribution(intersectionID, startTime,
                endTime);

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults.get(0).getId()).isEqualTo("15");
        assertThat(actualResults.get(0).getCount()).isEqualTo(3600);
        assertThat(actualResults.get(1).getId()).isEqualTo("8");
        assertThat(actualResults.get(1).getCount()).isEqualTo(7200);
    }
}
