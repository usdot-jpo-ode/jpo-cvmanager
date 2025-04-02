package us.dot.its.jpo.ode.api.accessorTests.bsm;

import org.junit.jupiter.api.Test;
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
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepositoryImpl;
import us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification.ConnectionOfTravelNotificationRepositoryImpl;
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
        ConnectionOfTravelNotificationRepositoryImpl repo = mock(ConnectionOfTravelNotificationRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class))).thenReturn(expected);

        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<OdeBsmData> resultBsms = repository.find("ip", "id", startTime, endTime, longitude, latitude,
                distance, pageRequest);

        assertThat(resultBsms).isEqualTo(expected);
    }

    @Test
    public void testFindWithAllParameters() {
        // Arrange
        Page<OdeBsmData> expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "recordGeneratedAt"));

        Criteria expectedCriteria = new IntersectionCriteria()
                .whereOptional("metadata.originIp", originIp)
                .whereOptional("payload.data.coreData.id", vehicleId)
                .withinTimeWindow("recordGeneratedAt", startTime, endTime)
                .and("payload.data.coreData.position.latitude")
                .gte(9.995).lte(10.005)
                .and("payload.data.coreData.position.longitude")
                .gte(9.995).lte(10.005);

        when(mongoTemplate.find(any(Query.class), eq(OdeBsmData.class), eq("OdeBsmJson")))
                .thenReturn(new ArrayList<>());

        // Act
        Page<OdeBsmData> result = repository.find(originIp, vehicleId, startTime, endTime, longitude, latitude,
                distance, pageRequest);

        // Assert
        assertThat(result).isEqualTo(expectedPage);
        Mockito.verify(mongoTemplate).find(Mockito.argThat(query -> {
            org.bson.Document queryObject = query.getQueryObject();
            org.bson.Document expectedQueryObject = new org.bson.Document();
            expectedCriteria.getCriteriaObject().forEach(expectedQueryObject::put);
            return queryObject.equals(expectedQueryObject);
        }), eq(OdeBsmData.class), eq("OdeBsmJson"));
    }

    @Test
    public void testFindWithNullBoundingBox() {
        // Arrange
        Page<OdeBsmData> expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "recordGeneratedAt"));

        Criteria expectedCriteria = new IntersectionCriteria()
                .whereOptional("metadata.originIp", originIp)
                .whereOptional("payload.data.coreData.id", vehicleId)
                .withinTimeWindow("recordGeneratedAt", startTime, endTime);

        when(mongoTemplate.find(any(Query.class), eq(OdeBsmData.class), eq("OdeBsmJson")))
                .thenReturn(new ArrayList<>());

        // Act
        Page<OdeBsmData> result = repository.find(originIp, vehicleId, startTime, endTime, null, null, null,
                pageRequest);

        // Assert
        assertThat(result).isEqualTo(expectedPage);
        Mockito.verify(mongoTemplate).find(Mockito.argThat(query -> {
            org.bson.Document queryObject = query.getQueryObject();
            org.bson.Document expectedQueryObject = new org.bson.Document();
            expectedCriteria.getCriteriaObject().forEach(expectedQueryObject::put);
            return queryObject.equals(expectedQueryObject);
        }), eq(OdeBsmData.class), eq("OdeBsmJson"));
    }

    @Test
    public void testFindWithNullOptionalParameters() {
        // Arrange
        Page<OdeBsmData> expectedPage = mock(Page.class);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "recordGeneratedAt"));

        Criteria expectedCriteria = new IntersectionCriteria()
                .withinTimeWindow("recordGeneratedAt", startTime, endTime);

        when(mongoTemplate.find(any(Query.class), eq(OdeBsmData.class), eq("OdeBsmJson")))
                .thenReturn(new ArrayList<>());

        // Act
        Page<OdeBsmData> result = repository.find(null, null, startTime, endTime, null, null, null, pageRequest);

        // Assert
        assertThat(result).isEqualTo(expectedPage);
        Mockito.verify(mongoTemplate).find(Mockito.argThat(query -> {
            org.bson.Document queryObject = query.getQueryObject();
            org.bson.Document expectedQueryObject = new org.bson.Document();
            expectedCriteria.getCriteriaObject().forEach(expectedQueryObject::put);
            return queryObject.equals(expectedQueryObject);
        }), eq(OdeBsmData.class), eq("OdeBsmJson"));
    }
}