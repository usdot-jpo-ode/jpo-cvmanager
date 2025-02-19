package us.dot.its.jpo.ode.api.accessorTests.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification.ConnectionOfTravelNotificationRepositoryImpl;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class ConnectionOfTravelNotificationRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ConnectionOfTravelNotificationRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ConnectionOfTravelNotificationRepositoryImpl(mongoTemplate);
    }

    @Test
    public void testGetQueryResultCount() {
        long expectedCount = 10;

        Mockito.when(mongoTemplate.count(Mockito.any(Query.class), Mockito.eq(ConnectionOfTravelNotification.class),
                Mockito.anyString()))
                .thenReturn(expectedCount);

        long resultCount = repository.getQueryResultCount(1, 0L, 0L, false);

        assertThat(resultCount).isEqualTo(expectedCount);
        Mockito.verify(mongoTemplate).count(Mockito.any(Query.class), Mockito.eq(ConnectionOfTravelNotification.class),
                Mockito.anyString());
    }

    @Test
    public void testFindConnectionOfTravelNotifications() {
        List<ConnectionOfTravelNotification> expected = List.of(new ConnectionOfTravelNotification());

        Mockito.when(mongoTemplate.find(Mockito.any(Query.class), Mockito.eq(ConnectionOfTravelNotification.class),
                Mockito.anyString()))
                .thenReturn(expected);

        List<ConnectionOfTravelNotification> results = repository.find(1, 0L, 0L, false, PageRequest.of(1, 1))
                .getContent();

        assertThat(results).isEqualTo(expected);
    }

}