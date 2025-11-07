package us.dot.its.jpo.ode.api.accessorTests.bsm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

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
public class OdeBsmJsonRepositoryImplTest {

    @MockitoSpyBean
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<AggregationResult> mockAggregationResult;

    @Mock
    private Page<Document> mockDocumentPage;

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

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new OdeBsmJsonRepositoryImpl(mongoTemplate);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Register
                                                                                                 // JavaTimeModule
    }

    @Test
    public void testFindBsmsGeo() throws IOException {
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);
        ArgumentCaptor<Criteria> criteriaCaptor = ArgumentCaptor.forClass(Criteria.class);

        PageRequest pageRequest = PageRequest.of(0, 1);

        when(repo.findDocumentsWithPagination(
                any(),
                any(),
                any(PageRequest.class),
                criteriaCaptor.capture(),
                any(Sort.class),
                any())).thenReturn(mockDocumentPage);
        doCallRealMethod().when(repo).find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        repo.find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        // Extract the captured Aggregation
        Criteria capturedCriteria = criteriaCaptor.getValue();

        // Assert the Match operation Criteria
        assertThat(capturedCriteria.getCriteriaObject().toJson())
                .isEqualTo(String.format(
                        "{\"metadata.originIp\": \"%s\", \"payload.data.value.BasicSafetyMessage.coreData.id\": \"%s\", \"metadata.odeReceivedAt\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}, \"payload.data.value.BasicSafetyMessage.coreData.lat\": {\"$gte\": 99954795, \"$lte\": 100045204}, \"payload.data.value.BasicSafetyMessage.coreData.long\": {\"$gte\": 99954395, \"$lte\": 100045604}}",
                        originIp, vehicleId, startTimeString, endTimeString));
    }

    @Test
    public void testFindWithAllParameters() throws IOException {
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);
        ArgumentCaptor<Criteria> criteriaCaptor = ArgumentCaptor.forClass(Criteria.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt"));

        when(repo.findDocumentsWithPagination(
                any(),
                any(),
                any(PageRequest.class),
                criteriaCaptor.capture(),
                any(Sort.class),
                any())).thenReturn(mockDocumentPage);

        doCallRealMethod().when(repo).find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        repo.find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        // Extract the captured Aggregation
        Criteria capturedCriteria = criteriaCaptor.getValue();

        // Assert the Match operation Criteria
        assertThat(capturedCriteria.getCriteriaObject().toJson())
                .isEqualTo(String.format(
                        "{\"metadata.originIp\": \"%s\", \"payload.data.value.BasicSafetyMessage.coreData.id\": \"%s\", \"metadata.odeReceivedAt\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}, \"payload.data.value.BasicSafetyMessage.coreData.lat\": {\"$gte\": 99954795, \"$lte\": 100045204}, \"payload.data.value.BasicSafetyMessage.coreData.long\": {\"$gte\": 99954395, \"$lte\": 100045604}}",
                        originIp, vehicleId, startTimeString, endTimeString));

        // Verify the Criteria passed to findPage
        Mockito.verify(repo).findDocumentsWithPagination(
                any(),
                any(),
                eq(pageRequest),
                Mockito.argThat(criteria -> {
                    // Verify ORIGIN_IP_FIELD
                    assertThat(criteria.getCriteriaObject().get("metadata.originIp"))
                            .isEqualTo(originIp);

                    // Verify VEHICLE_ID_FIELD
                    assertThat(criteria.getCriteriaObject().get("payload.data.value.BasicSafetyMessage.coreData.id"))
                            .isEqualTo(vehicleId);

                    // Verify DATE_FIELD
                    Document dateField = (Document) criteria.getCriteriaObject()
                            .get("metadata.odeReceivedAt");
                    assertThat(dateField.get("$gte"))
                            .isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte"))
                            .isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    Document latitudeField = (Document) criteria.getCriteriaObject()
                            .get("payload.data.value.BasicSafetyMessage.coreData.lat");
                    assertThat((Integer) latitudeField.get("$gte")).isCloseTo(99950000, within(10000));
                    assertThat((Integer) latitudeField.get("$lte")).isCloseTo(100050000, within(100000));

                    // Verify longitude with tolerance
                    Document longitudeField = (Document) criteria.getCriteriaObject()
                            .get("payload.data.value.BasicSafetyMessage.coreData.long");
                    assertThat((Integer) longitudeField.get("$gte")).isCloseTo(99950000, within(10000));
                    assertThat((Integer) longitudeField.get("$lte")).isCloseTo(100005000, within(100000));

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt")),
                any());
    }

    @Test
    public void testFindWithNullBoundingBox() throws IOException {
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);
        ArgumentCaptor<Criteria> criteriaCaptor = ArgumentCaptor.forClass(Criteria.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt"));

        when(repo.findDocumentsWithPagination(
                any(),
                any(),
                any(PageRequest.class),
                criteriaCaptor.capture(),
                any(Sort.class),
                any())).thenReturn(mockDocumentPage);

        doCallRealMethod().when(repo).find(originIp, vehicleId, startTime, endTime, null, null, null,
                pageRequest);

        repo.find(originIp, vehicleId, startTime, endTime, null, null, null,
                pageRequest);

        // Extract the captured Aggregation
        Criteria capturedCriteria = criteriaCaptor.getValue();

        // Assert the Match operation Criteria
        assertThat(capturedCriteria.getCriteriaObject().toJson())
                .isEqualTo(String.format(
                        "{\"metadata.originIp\": \"%s\", \"payload.data.value.BasicSafetyMessage.coreData.id\": \"%s\", \"metadata.odeReceivedAt\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}}",
                        originIp, vehicleId, startTimeString, endTimeString));

        // Verify the Criteria passed to findPage
        Mockito.verify(repo).findDocumentsWithPagination(
                any(),
                any(),
                eq(pageRequest),
                Mockito.argThat(criteria -> {
                    // Verify ORIGIN_IP_FIELD
                    assertThat(criteria.getCriteriaObject().get("metadata.originIp"))
                            .isEqualTo(originIp);

                    // Verify VEHICLE_ID_FIELD
                    assertThat(criteria.getCriteriaObject().get("payload.data.value.BasicSafetyMessage.coreData.id"))
                            .isEqualTo(vehicleId);

                    // Verify DATE_FIELD
                    Document dateField = (Document) criteria.getCriteriaObject()
                            .get("metadata.odeReceivedAt");
                    assertThat(dateField.get("$gte"))
                            .isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte"))
                            .isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    assertThat(criteria.getCriteriaObject()
                            .get("payload.data.value.BasicSafetyMessage.coreData.lat")).isNull();

                    // Verify longitude with tolerance
                    assertThat(criteria.getCriteriaObject()
                            .get("payload.data.value.BasicSafetyMessage.coreData.long")).isNull();

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt")),
                any());
    }

    @Test
    public void testFindWithNullOptionalParameters() throws IOException {
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);
        ArgumentCaptor<Criteria> criteriaCaptor = ArgumentCaptor.forClass(Criteria.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt"));

        when(repo.findDocumentsWithPagination(
                any(),
                any(),
                any(PageRequest.class),
                criteriaCaptor.capture(),
                any(Sort.class),
                any())).thenReturn(mockDocumentPage);

        doCallRealMethod().when(repo).find(null, null, startTime, endTime, null, null, null, pageRequest);

        repo.find(null, null, startTime, endTime, null, null, null, pageRequest);

        // Extract the captured Aggregation
        Criteria capturedCriteria = criteriaCaptor.getValue();

        // Assert the Match operation Criteria
        assertThat(capturedCriteria.getCriteriaObject().toJson())
                .isEqualTo(String.format(
                        "{\"metadata.odeReceivedAt\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}}",
                        startTimeString, endTimeString)); // Verify the Criteria passed to
                                                          // findPage
        Mockito.verify(repo).findDocumentsWithPagination(
                any(),
                any(),
                eq(pageRequest),
                Mockito.argThat(criteria -> {
                    // Verify ORIGIN_IP_FIELD
                    assertThat(criteria.getCriteriaObject().get("metadata.originIp")).isNull();

                    // Verify VEHICLE_ID_FIELD
                    assertThat(criteria.getCriteriaObject().get("payload.data.value.BasicSafetyMessage.coreData.id"))
                            .isNull();

                    // Verify DATE_FIELD
                    Document dateField = (Document) criteria.getCriteriaObject()
                            .get("metadata.odeReceivedAt");
                    assertThat(dateField.get("$gte"))
                            .isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte"))
                            .isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    assertThat(criteria.getCriteriaObject()
                            .get("payload.data.value.BasicSafetyMessage.coreData.lat")).isNull();

                    // Verify longitude with tolerance
                    assertThat(criteria.getCriteriaObject()
                            .get("payload.data.value.BasicSafetyMessage.coreData.long")).isNull();

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "metadata.odeReceivedAt")),
                any());
    }

    @Test
    public void testFindWithData() throws IOException {
        // Load sample JSON data
        String json = new String(
                Files.readAllBytes(
                        Paths.get("src/test/resources/json/ConflictMonitor.OdeBsmJson.json")));

        List<Document> sampleDocuments = new ArrayList<>();

        // List<Document> sampleDocuments = List.of(Document.parse(json));
        sampleDocuments.add(Document.parse(json));
        sampleDocuments.add(Document.parse(json));

        // Mock dependencies
        when(mockDocumentPage.getContent()).thenReturn(sampleDocuments);
        when(mockDocumentPage.getTotalElements()).thenReturn(2L);

        AggregationResult aggregationResult = new AggregationResult();
        aggregationResult.setResults(sampleDocuments);
        AggregationResultCount aggregationResultCount = new AggregationResultCount();
        aggregationResultCount.setCount(2L);
        aggregationResult.setMetadata(List.of(aggregationResultCount));

        when(mockAggregationResult.getUniqueMappedResult()).thenReturn(aggregationResult);

        ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
        doReturn(mockAggregationResult).when(mongoTemplate).aggregate(aggregationCaptor.capture(),
                anyString(),
                eq(AggregationResult.class));

        // Call the repository find method
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<OdeMessageFrameData> findResponse = repository.find(originIp, vehicleId, startTime, endTime, -104.1, 36.8,
                50.0,
                pageRequest);

        // Extract the captured Aggregation
        Aggregation capturedAggregation = aggregationCaptor.getValue();

        // Extract the MatchOperation from the Aggregation pipeline
        Document pipeline = capturedAggregation.toPipeline(Aggregation.DEFAULT_CONTEXT).get(0);

        // Assert the Match operation Criteria
        assertThat(pipeline.toJson())
                .isEqualTo(String.format(
                        "{\"$match\": {\"metadata.originIp\": \"%s\", \"payload.data.value.BasicSafetyMessage.coreData.id\": \"%s\", \"metadata.odeReceivedAt\": {\"$gte\": \"%s\", \"$lte\": \"%s\"}, \"payload.data.value.BasicSafetyMessage.coreData.lat\": {\"$gte\": 367995494, \"$lte\": 368004505}, \"payload.data.value.BasicSafetyMessage.coreData.long\": {\"$gte\": -1041005602, \"$lte\": -1040994397}}}",
                        originIp, vehicleId, startTimeString, endTimeString));

        // OdeMessageFrameData message = findResponse.getContent().get(0);

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
}