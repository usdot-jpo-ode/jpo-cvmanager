package us.dot.its.jpo.ode.api.accessorTests.notifications;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.time_change_details_notification.TimeChangeDetailsNotificationRepositoryImpl;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class TimeChangeDetailsNotificationRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Page<TimeChangeDetailsNotification> mockPage;

    @InjectMocks
    private TimeChangeDetailsNotificationRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1624640400000L; // June 26, 2021 00:00:00 GMT
    Long endTime = 1624726799000L; // June 26, 2021 23:59:59 GMT
    boolean latest = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new TimeChangeDetailsNotificationRepositoryImpl(mongoTemplate);
    }

    @Test
    public void testCount() {
        long expectedCount = 10;

        when(mongoTemplate.count(any(),
                Mockito.<String>any())).thenReturn(expectedCount);

        long resultCount = repository.count(1, null, null);

        assertThat(resultCount).isEqualTo(expectedCount);
        verify(mongoTemplate).count(any(Query.class), anyString());
    }

    @Test
    public void testFind() {
        TimeChangeDetailsNotificationRepositoryImpl repo = mock(TimeChangeDetailsNotificationRepositoryImpl.class);

        when(repo.findPage(
                any(),
                any(),
                any(PageRequest.class),
                any(Criteria.class),
                any(Sort.class),
                any(),
                eq(TimeChangeDetailsNotification.class))).thenReturn(mockPage);
        PageRequest pageRequest = PageRequest.of(0, 1);
        doCallRealMethod().when(repo).find(1, null, null, pageRequest);

        Page<TimeChangeDetailsNotification> results = repo.find(1, null, null, pageRequest);

        assertThat(results).isEqualTo(mockPage);
    }

    @Test
    void testFindLatest() {
        TimeChangeDetailsNotification event = new TimeChangeDetailsNotification();
        event.setIntersectionID(intersectionID);

        doReturn(event).when(mongoTemplate).findOne(any(Query.class), eq(TimeChangeDetailsNotification.class),
                anyString());

        Page<TimeChangeDetailsNotification> page = repository.findLatest(intersectionID, startTime, endTime);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getIntersectionID()).isEqualTo(intersectionID);
        verify(mongoTemplate).findOne(any(Query.class), eq(TimeChangeDetailsNotification.class),
                eq("CmTimeChangeDetailsNotification"));
    }
}