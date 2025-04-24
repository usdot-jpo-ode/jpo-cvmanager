package us.dot.its.jpo.ode.api.accessorTests.bsm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.bson.Document;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.ode.model.OdeBsmData;

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
public class OdeBsmJsonRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OdeBsmJsonRepositoryImpl repository;

    String originIp = "172.250.250.181";
    String vehicleId = "B0AT";
    Long startTime = 1724170658205L;
    String startTimeString = "2024-08-20T16:17:38.205Z";
    Long endTime = 1724170778205L;
    String endTimeString = "2024-08-20T16:19:38.205Z";
    Double longitude = 10.0;
    Double latitude = 10.0;
    Double distance = 500.0;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new OdeBsmJsonRepositoryImpl(mongoTemplate);
    }

    @Test
    public void testFindBsmsGeo() {

        @SuppressWarnings("rawtypes")
        Page expected = Mockito.mock(Page.class);
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                any())).thenReturn(expected);
        PageRequest pageRequest = PageRequest.of(0, 1);
        doCallRealMethod().when(repo).find("ip", "id", startTime, endTime, longitude, latitude,
                distance, pageRequest);

        Page<OdeBsmData> resultBsms = repo.find("ip", "id", startTime, endTime, longitude, latitude,
                distance, pageRequest);

        assertThat(resultBsms).isEqualTo(expected);
    }

    @Test
    public void testFindWithAllParameters() {
        @SuppressWarnings("rawtypes")
        Page expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt"));
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                any())).thenReturn(expectedPage);

        doCallRealMethod().when(repo).find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        // Act
        Page<OdeBsmData> result = repo.find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        // Assert
        assertThat(result).isEqualTo(expectedPage);

        // Verify the Criteria passed to findPage
        Mockito.verify(repo).findPage(
                any(),
                any(),
                eq(pageRequest),
                Mockito.argThat(criteria -> {
                    // Verify ORIGIN_IP_FIELD
                    assertThat(criteria.getCriteriaObject().get("metadata.originIp")).isEqualTo(originIp);

                    // Verify VEHICLE_ID_FIELD
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.id")).isEqualTo(vehicleId);

                    // Verify DATE_FIELD
                    Document dateField = (Document) criteria.getCriteriaObject()
                            .get("metadata.odeReceivedAt");
                    assertThat(dateField.get("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    Document latitudeField = (Document) criteria.getCriteriaObject()
                            .get("payload.data.coreData.position.latitude");
                    assertThat((Double) latitudeField.get("$gte")).isCloseTo(9.995, within(0.001));
                    assertThat((Double) latitudeField.get("$lte")).isCloseTo(10.005, within(0.001));

                    // Verify longitude with tolerance
                    Document longitudeField = (Document) criteria.getCriteriaObject()
                            .get("payload.data.coreData.position.longitude");
                    assertThat((Double) longitudeField.get("$gte")).isCloseTo(9.995, within(0.001));
                    assertThat((Double) longitudeField.get("$lte")).isCloseTo(10.005, within(0.001));

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt")),
                any(),
                any());
    }

    @Test
    public void testFindWithNullBoundingBox() {
        @SuppressWarnings("rawtypes")
        Page expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt"));
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                any())).thenReturn(expectedPage);

        doCallRealMethod().when(repo).find(originIp, vehicleId, startTime, endTime, null, null, null, pageRequest);
        // Act
        Page<OdeBsmData> result = repo.find(originIp, vehicleId, startTime, endTime, null, null, null,
                pageRequest);

        // Assert
        assertThat(result).isEqualTo(expectedPage);

        // Verify the Criteria passed to findPage
        Mockito.verify(repo).findPage(
                any(),
                any(),
                eq(pageRequest),
                Mockito.argThat(criteria -> {
                    // Verify ORIGIN_IP_FIELD
                    assertThat(criteria.getCriteriaObject().get("metadata.originIp")).isEqualTo(originIp);

                    // Verify VEHICLE_ID_FIELD
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.id")).isEqualTo(vehicleId);

                    // Verify DATE_FIELD
                    Document dateField = (Document) criteria.getCriteriaObject()
                            .get("metadata.odeReceivedAt");
                    assertThat(dateField.get("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.latitude")).isNull();

                    // Verify longitude with tolerance
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.longitude")).isNull();

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt")),
                any(),
                any());
    }

    @Test
    public void testFindWithNullOptionalParameters() {
        @SuppressWarnings("rawtypes")
        Page expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt"));
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                any())).thenReturn(expectedPage);

        doCallRealMethod().when(repo).find(null, null, startTime, endTime, null, null, null, pageRequest);

        // Act
        Page<OdeBsmData> result = repo.find(null, null, startTime, endTime, null, null, null, pageRequest);

        // Assert
        assertThat(result).isEqualTo(expectedPage);

        // Verify the Criteria passed to findPage
        Mockito.verify(repo).findPage(
                any(),
                any(),
                eq(pageRequest),
                Mockito.argThat(criteria -> {
                    // Verify ORIGIN_IP_FIELD
                    assertThat(criteria.getCriteriaObject().get("metadata.originIp")).isNull();

                    // Verify VEHICLE_ID_FIELD
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.id")).isNull();

                    // Verify DATE_FIELD
                    Document dateField = (Document) criteria.getCriteriaObject()
                            .get("metadata.odeReceivedAt");
                    assertThat(dateField.get("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.latitude")).isNull();

                    // Verify longitude with tolerance
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.longitude")).isNull();

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt")),
                any(),
                any());
    }

    @Test
    @Disabled("TODO: Update for use with typesafe implementation")
    public void testFindWithData() throws IOException {
//        // Load sample JSON data
//        TypeReference<List<LinkedHashMap<String, Object>>> hashMapList = new TypeReference<>() {
//        };
//        String json = new String(
//                Files.readAllBytes(Paths.get("src/test/resources/json/ConflictMonitor.OdeBsmJson.json")));
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
//        Page<OdeBsmData> findResponse = repository.find(originIp, vehicleId, startTime, endTime, -104.1, 36.8, 50.0,
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
//                        "{\"$match\": {\"metadata.originIp\": \"%s\", \"payload.data.coreData.id\": \"%s\", \"metadata.odeReceivedAt\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}, \"payload.data.coreData.position.latitude\": {\"$gte\": 36.799549443581746, \"$lte\": 36.80045055638405}, \"payload.data.coreData.position.longitude\": {\"$gte\": -104.10056026011259, \"$lte\": -104.0994397398874}}}",
//                        originIp, vehicleId, startTimeString, endTimeString));
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