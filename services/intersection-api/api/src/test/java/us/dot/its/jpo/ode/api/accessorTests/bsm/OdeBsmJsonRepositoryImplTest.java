package us.dot.its.jpo.ode.api.accessorTests.bsm;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepositoryImpl;
import us.dot.its.jpo.ode.model.OdeBsmData;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
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
        Page expected = Mockito.mock(Page.class);
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
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
        // Arrange
        Page expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "recordGeneratedAt"));
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
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
                            .get("recordGeneratedAt");
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
                eq(Sort.by(Sort.Direction.DESC, "recordGeneratedAt")),
                any());
    }

    @Test
    public void testFindWithNullBoundingBox() {
        // Arrange
        Page expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "recordGeneratedAt"));
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
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
                            .get("recordGeneratedAt");
                    assertThat(dateField.get("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.latitude")).isNull();

                    // Verify longitude with tolerance
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.longitude")).isNull();

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "recordGeneratedAt")),
                any());
    }

    @Test
    public void testFindWithNullOptionalParameters() {
        // Arrange
        Page expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "recordGeneratedAt"));
        OdeBsmJsonRepositoryImpl repo = mock(OdeBsmJsonRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
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
                            .get("recordGeneratedAt");
                    assertThat(dateField.get("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
                    assertThat(dateField.get("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

                    // Verify latitude
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.latitude")).isNull();

                    // Verify longitude with tolerance
                    assertThat(criteria.getCriteriaObject().get("payload.data.coreData.position.longitude")).isNull();

                    return true;
                }),
                eq(Sort.by(Sort.Direction.DESC, "recordGeneratedAt")),
                any());
    }
}