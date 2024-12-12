package us.dot.its.jpo.ode.api.accessorTests.spat;

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

import org.bson.Document;

import us.dot.its.jpo.ode.api.accessors.spat.OdeSpatDataRepositoryImpl;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class OdeSpatDataRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OdeSpatDataRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuery() {
    
        boolean latest = true;

        Query query = repository.getQuery(intersectionID, startTime, endTime, latest);


        // Assert IntersectionID
        assertThat(query.getQueryObject().get("properties.intersectionId")).isEqualTo(intersectionID);
        
        
        // Assert Start and End Time
        Document queryTimeDocument = (Document)query.getQueryObject().get("properties.timeStamp");
        assertThat(queryTimeDocument.getString("$gte")).isEqualTo(Instant.ofEpochMilli(startTime).toString());
        assertThat(queryTimeDocument.getString("$lte")).isEqualTo(Instant.ofEpochMilli(endTime).toString());


        // Assert sorting and limit
        assertThat(query.getSortObject().keySet().contains("properties.timeStamp")).isTrue();
        assertThat(query.getSortObject().get("properties.timeStamp")).isEqualTo(-1);
        assertThat(query.getLimit()).isEqualTo(1);
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

    // @Test
    // public void testFindSpats() {
    //     Query query = new Query();
    //     List<OdeSpatData> expectedSpats = new ArrayList<>();

    //     Mockito.doReturn(expectedSpats).when(mongoTemplate).find(query, OdeSpatData.class, "OdeSpatJson");

    //     List<OdeSpatData> resultSpats = repository.findSpats(query);

    //     assertThat(resultSpats).isEqualTo(expectedSpats);
    // }
}
