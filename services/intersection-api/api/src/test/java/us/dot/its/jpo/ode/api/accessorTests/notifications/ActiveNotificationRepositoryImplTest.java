package us.dot.its.jpo.ode.api.accessorTests.notifications;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.app_health.KafkaStreamsAnomalyNotification;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepositoryImpl;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class ActiveNotificationRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ActiveNotificationRepositoryImpl repository;

    Integer intersectionID = 123;
    String notificationType = "IntersectionReferenceAlignmentNotification";
    String key = "IntersectionReferenceAlignmentNotification_123_0";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ActiveNotificationRepositoryImpl(mongoTemplate);
    }

    @Test
    public void testCount() {
        long expectedCount = 10;

        when(mongoTemplate.count(any(),
                Mockito.<String>any())).thenReturn(expectedCount);

        PageRequest pageRequest = PageRequest.of(0, 1);
        long resultCount = repository.count(1, null, null, pageRequest);

        assertThat(resultCount).isEqualTo(expectedCount);
        verify(mongoTemplate).count(any(Query.class), anyString());
    }

    @Test
    public void testFindWithAllNotificationTypes() {
        MongoTemplate mockMongoTemplate = mock(MongoTemplate.class);
        ActiveNotificationRepositoryImpl repo = spy(new ActiveNotificationRepositoryImpl(mockMongoTemplate));

        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Document connectionOfTravelDoc = new Document("notificationType", "ConnectionOfTravelNotification");
        Document intersectionReferenceAlignmentDoc = new Document("notificationType",
                "IntersectionReferenceAlignmentNotification");
        Document laneDirectionOfTravelDoc = new Document("notificationType",
                "LaneDirectionOfTravelAssessmentNotification");
        Document signalGroupAlignmentDoc = new Document("notificationType", "SignalGroupAlignmentNotification");
        Document signalStateConflictDoc = new Document("notificationType", "SignalStateConflictNotification");
        Document timeChangeDetailsDoc = new Document("notificationType", "TimeChangeDetailsNotification");
        Document appHealthDoc = new Document("notificationType", "AppHealthNotification");

        List<Document> documents = List.of(
                connectionOfTravelDoc,
                intersectionReferenceAlignmentDoc,
                laneDirectionOfTravelDoc,
                signalGroupAlignmentDoc,
                signalStateConflictDoc,
                timeChangeDetailsDoc,
                appHealthDoc);

        Page<Document> dbObjects = new PageImpl<>(documents, pageable, documents.size());

        // Mock the MongoTemplate and MongoConverter
        MongoConverter mockConverter = mock(MongoConverter.class);
        when(mockMongoTemplate.getConverter()).thenReturn(mockConverter);

        when(mockConverter.read(ConnectionOfTravelNotification.class, connectionOfTravelDoc))
                .thenReturn(new ConnectionOfTravelNotification());
        when(mockConverter.read(IntersectionReferenceAlignmentNotification.class,
                intersectionReferenceAlignmentDoc))
                .thenReturn(new IntersectionReferenceAlignmentNotification());
        when(mockConverter.read(LaneDirectionOfTravelNotification.class, laneDirectionOfTravelDoc))
                .thenReturn(new LaneDirectionOfTravelNotification());
        when(mockConverter.read(SignalGroupAlignmentNotification.class, signalGroupAlignmentDoc))
                .thenReturn(new SignalGroupAlignmentNotification());
        when(mockConverter.read(SignalStateConflictNotification.class, signalStateConflictDoc))
                .thenReturn(new SignalStateConflictNotification());
        when(mockConverter.read(TimeChangeDetailsNotification.class, timeChangeDetailsDoc))
                .thenReturn(new TimeChangeDetailsNotification());
        when(mockConverter.read(KafkaStreamsAnomalyNotification.class, appHealthDoc))
                .thenReturn(new KafkaStreamsAnomalyNotification());

        doReturn(dbObjects).when(repo).findPage(eq(mockMongoTemplate), anyString(), eq(pageable),
                any(), any());

        // Act
        Page<Notification> result = repo.find(null, null, null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(7);
        assertThat(result.getContent().get(0)).isInstanceOf(ConnectionOfTravelNotification.class);
        assertThat(result.getContent().get(0)).isInstanceOf(ConnectionOfTravelNotification.class);
        assertThat(result.getContent().get(1)).isInstanceOf(IntersectionReferenceAlignmentNotification.class);
        assertThat(result.getContent().get(2)).isInstanceOf(LaneDirectionOfTravelNotification.class);
        assertThat(result.getContent().get(3)).isInstanceOf(SignalGroupAlignmentNotification.class);
        assertThat(result.getContent().get(4)).isInstanceOf(SignalStateConflictNotification.class);
        assertThat(result.getContent().get(5)).isInstanceOf(TimeChangeDetailsNotification.class);
        assertThat(result.getContent().get(6)).isInstanceOf(KafkaStreamsAnomalyNotification.class);

        verify(repo).findPage(any(), any(), eq(pageable), any(), any());
        verify(mockConverter).read(ConnectionOfTravelNotification.class, connectionOfTravelDoc);
        verify(mockConverter).read(IntersectionReferenceAlignmentNotification.class,
                intersectionReferenceAlignmentDoc);
        verify(mockConverter).read(LaneDirectionOfTravelNotification.class, laneDirectionOfTravelDoc);
        verify(mockConverter).read(SignalGroupAlignmentNotification.class, signalGroupAlignmentDoc);
        verify(mockConverter).read(SignalStateConflictNotification.class, signalStateConflictDoc);
        verify(mockConverter).read(TimeChangeDetailsNotification.class, timeChangeDetailsDoc);
        verify(mockConverter).read(KafkaStreamsAnomalyNotification.class, appHealthDoc);
    }
}