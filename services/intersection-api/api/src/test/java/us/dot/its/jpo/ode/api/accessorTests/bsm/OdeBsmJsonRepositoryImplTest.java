package us.dot.its.jpo.ode.api.accessorTests.bsm;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
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

    @Mock
    private ConflictMonitorApiProperties props;

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
    }

    @Test
    public void testFindBsmsGeo() {
        List<Map> expectedBsms = new ArrayList<>();

        Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.eq(Map.class), Mockito.eq("OdeBsmJson"))).thenReturn(expectedBsms);       
        List<OdeBsmData> resultBsms = repository.findOdeBsmDataGeo("ip","id",startTime,endTime, longitude, latitude, distance);

        assertThat(resultBsms).isEqualTo(expectedBsms);
    }
}