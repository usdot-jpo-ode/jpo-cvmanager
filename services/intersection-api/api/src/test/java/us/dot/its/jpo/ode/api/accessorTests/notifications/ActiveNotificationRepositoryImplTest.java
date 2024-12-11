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
import java.util.List;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepositoryImpl;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ActiveNotificationRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ActiveNotificationRepositoryImpl repository;

    Integer intersectionID = 123;
    Integer roadRegulatorID = 0;
    String notificationType = "IntersectionReferenceAlignmentNotification";
    String key = "IntersectionReferenceAlignmentNotification_123_0";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuery() {
        Query query = repository.getQuery(intersectionID, roadRegulatorID, notificationType, key);

        // Assert IntersectionID
        assertThat(query.getQueryObject().get("intersectionID")).isEqualTo(intersectionID);
        // Road Regulator ID is not being enforced yet. Therefore not verified here.
        // assertThat(query.getQueryObject().get("roadRegulatorID")).isEqualTo(roadRegulatorID);
        assertThat(query.getQueryObject().get("notificationType")).isEqualTo(notificationType);
        assertThat(query.getQueryObject().get("key")).isEqualTo(key);

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
    public void testFindActiveNotifications() {
        Query query = new Query();
        List<Notification> expected = new ArrayList<>();

        Mockito.doReturn(expected).when(mongoTemplate).find(query, Notification.class, "CmNotification");

        List<Notification> results = repository.find(query);

        assertThat(results).isEqualTo(expected);
    }

}