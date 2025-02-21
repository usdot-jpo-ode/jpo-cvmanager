package us.dot.its.jpo.ode.api.accessorTests.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import us.dot.its.jpo.ode.api.accessors.PaginatedQueryUtils;
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

        Mockito.when(mongoTemplate.count(Mockito.any(Query.class),
                Mockito.anyString()))
                .thenReturn(expectedCount);

        long resultCount = repository.getQueryResultCount(1, 0L, 0L, PageRequest.of(0, 1));

        assertThat(resultCount).isEqualTo(expectedCount);
        Mockito.verify(mongoTemplate).count(Mockito.any(Query.class),
                Mockito.anyString());
    }

    @Test
    public void testFindConnectionOfTravelNotifications() {
        try (MockedStatic<PaginatedQueryUtils> mockedStatic = mockStatic(PaginatedQueryUtils.class)) {
            Page<ConnectionOfTravelNotification> expected = Mockito.mock(Page.class);

            mockedStatic.when(() -> PaginatedQueryUtils.getDataFromArgs(
                    Mockito.eq(mongoTemplate),
                    Mockito.eq("CmConnectionOfTravelNotification"),
                    Mockito.eq("notificationGeneratedAt"),
                    Mockito.any(PageRequest.class),
                    Mockito.eq(1), Mockito.eq(0L), Mockito.eq(0L))).thenReturn(expected);

            Page<ConnectionOfTravelNotification> results = repository.find(1, 0L, 0L, PageRequest.of(1, 1));

            assertThat(results).isEqualTo(expected);
        }
    }

}