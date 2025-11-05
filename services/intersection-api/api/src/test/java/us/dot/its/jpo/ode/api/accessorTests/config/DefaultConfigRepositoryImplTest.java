package us.dot.its.jpo.ode.api.accessorTests.config;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.UnitsEnum;
import us.dot.its.jpo.ode.api.accessors.config.default_config.DefaultConfigRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefaultConfigRepositoryImplTest {

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    DefaultConfigRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new DefaultConfigRepositoryImpl(mongoTemplate);
    }

    @Test
    void testGetQueryWithId() {
        String id = "testId";
        Query query = repository.getQuery(id);
        assertThat(query.getQueryObject().get("_id")).isEqualTo(id);
    }

    @Test
    void testGetQueryWithoutId() {
        Query query = repository.getQuery(null);
        assertThat(query.getQueryObject().containsKey("_id")).isFalse();
    }

    @Test
    void testGetQueryResultCount() {
        Query query = new Query();
        when(mongoTemplate.count(eq(query), eq(DefaultConfig.class), anyString())).thenReturn(5L);

        long count = repository.getQueryResultCount(query);

        assertThat(count).isEqualTo(5L);
        verify(mongoTemplate).count(eq(query), eq(DefaultConfig.class), eq("CmDefaultConfig"));
    }

    @Test
    void testFind() {
        Query query = new Query();
        DefaultConfig<Integer> config = new DefaultConfig<>(
                "lane.direction.of.travel.assessment.distanceFromCenterlineToleranceCm", "category", 42,
                "java.lang.Integer",
                UnitsEnum.CENTIMETERS, "The distance from centerline tolerance.");
        when(mongoTemplate.find(eq(query), eq(DefaultConfig.class), anyString())).thenReturn(List.of(config));

        List<DefaultConfig> result = repository.find(query);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(config);
        verify(mongoTemplate).find(eq(query), eq(DefaultConfig.class), eq("CmDefaultConfig"));
    }

    @Test
    void testSaveIntegerType() {
        DefaultConfig<String> config = new DefaultConfig<>(
                "lane.direction.of.travel.assessment.distanceFromCenterlineToleranceCm", "category", "100",
                "java.lang.Integer",
                UnitsEnum.CENTIMETERS, "The distance from centerline tolerance.");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value")).isEqualTo(100);
    }

    @Test
    void testSaveDoubleType() {
        DefaultConfig<String> config = new DefaultConfig<>(
                "bsm.event.simplifyPathToleranceMeters", "category", "0.05",
                "java.lang.Double",
                UnitsEnum.CENTIMETERS, "The Douglas-Peucker simplification algorithm distance parameter in meters");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value")).isEqualTo(0.05);
    }

    @Test
    void testSaveLongType() {
        DefaultConfig<String> config = new DefaultConfig<>(
                "map.validation.gracePeriodMilliseconds", "category", "5000",
                "java.lang.Long",
                UnitsEnum.CENTIMETERS, "Window grace period");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value"))
                .isEqualTo(5000L);
    }

    @Test
    void testSaveStringType() {
        DefaultConfig<String> config = new DefaultConfig<>(
                "assessment.assessmentOutputTopicName", "category", "topic.CmAssessment",
                "java.lang.String",
                UnitsEnum.CENTIMETERS, "The name of the topic to output assessments to");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value"))
                .isEqualTo("topic.CmAssessment");
    }

    @Test
    void testSaveBooleanType() {
        DefaultConfig<Boolean> config = new DefaultConfig<>(
                "aggregation.debug", "category", true,
                "java.lang.Boolean",
                UnitsEnum.CENTIMETERS, "Whether to log diagnostic information for debugging");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value"))
                .isEqualTo(true);
    }
}