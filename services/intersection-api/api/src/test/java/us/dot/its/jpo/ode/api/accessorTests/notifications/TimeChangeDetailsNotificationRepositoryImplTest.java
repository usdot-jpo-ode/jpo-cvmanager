package us.dot.its.jpo.ode.api.accessorTests.notifications;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.TimeChangeDetailsNotification.TimeChangeDetailsNotificationRepositoryImpl;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TimeChangeDetailsNotificationRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TimeChangeDetailsNotificationRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuery() {

        Query query = repository.getQuery(intersectionID, startTime, endTime, latest);

        // Assert IntersectionID
        assertThat(query.getQueryObject().get("intersectionID")).isEqualTo(intersectionID);
        
        
        // Assert Start and End Time
        Document queryTimeDocument = (Document)query.getQueryObject().get("notificationGeneratedAt");
        assertThat(queryTimeDocument.getDate("$gte")).isEqualTo(new Date(startTime));
        assertThat(queryTimeDocument.getDate("$lte")).isEqualTo(new Date(endTime));


        // Assert sorting and limit
        assertThat(query.getSortObject().keySet().contains("notificationGeneratedAt")).isTrue();
        assertThat(query.getSortObject().get("notificationGeneratedAt")).isEqualTo(-1);
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

    @Test
    public void testFindStopLineStopNotifications() {
        Query query = new Query();
        List<TimeChangeDetailsNotification> expected = new ArrayList<>();

        Mockito.doReturn(expected).when(mongoTemplate).find(query, TimeChangeDetailsNotification.class, "CmTimeChangeDetailsNotifications");

        List<TimeChangeDetailsNotification> results = repository.find(query);

        assertThat(results).isEqualTo(expected);
    }

}