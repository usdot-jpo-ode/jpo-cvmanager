package us.dot.its.jpo.ode.api.accessorTests.bsm;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepositoryImpl;
import us.dot.its.jpo.ode.model.OdeBsmData;



import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class OdeBsmJsonRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OdeBsmJsonRepositoryImpl repository;

    String originIp = "172.250.250.181";
    String vehicleId = "B0AT";
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuery() {

        Query query = repository.getQuery(originIp, vehicleId, startTime, endTime);


        // Assert IntersectionID
        assertThat(query.getQueryObject().get("metadata.originIp")).isEqualTo(originIp);
        assertThat(query.getQueryObject().get("payload.data.coreData.id")).isEqualTo(vehicleId);
        
        
        // Assert Start and End Time
        Document queryTimeDocument = (Document)query.getQueryObject().get("metadata.odeReceivedAt");
        assertThat(queryTimeDocument.getString("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
        assertThat(queryTimeDocument.getString("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());

    }

    @Test
    public void testGetQueryResultCount() {
        Query query = new Query();
        long expectedCount = 10;

        Mockito.when(mongoTemplate.count(Mockito.eq(query), Mockito.any(), Mockito.anyString())).thenReturn(expectedCount);

        long resultCount = repository.getQueryResultCount(query);

        assertThat(resultCount).isEqualTo(expectedCount);
        Mockito.verify(mongoTemplate).count(Mockito.eq(query), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testFindBsms() {
        Query query = new Query();
        List<OdeBsmData> expectedBsms = new ArrayList<>();

        Mockito.doReturn(expectedBsms).when(mongoTemplate).find(query, OdeBsmData.class, "OdeBsmJson");

        List<OdeBsmData> resultBsms = repository.findOdeBsmData(query);

        assertThat(resultBsms).isEqualTo(expectedBsms);
    }
}