package us.dot.its.jpo.ode.api.accessorTests.config;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.UnitsEnum;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.UpdateType;
import us.dot.its.jpo.ode.api.accessors.config.intersection_config.IntersectionConfigRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IntersectionConfigRepositoryImplTest {

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    IntersectionConfigRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new IntersectionConfigRepositoryImpl(mongoTemplate);
    }

    @Test
    void testGetQueryWithKeyAndIntersectionId() {
        String key = "testKey";
        Integer intersectionID = 123;
        Query query = repository.getQuery(key, intersectionID);
        assertThat(query.getQueryObject().get("_id")).isEqualTo(key);
        assertThat(query.getQueryObject().get("intersectionID")).isEqualTo(intersectionID);
    }

    @Test
    void testGetQueryWithKeyOnly() {
        String key = "testKey";
        Query query = repository.getQuery(key, null);
        assertThat(query.getQueryObject().get("_id")).isEqualTo(key);
        assertThat(query.getQueryObject().containsKey("intersectionID")).isFalse();
    }

    @Test
    void testGetQueryWithIntersectionIdOnly() {
        Integer intersectionID = 123;
        Query query = repository.getQuery(null, intersectionID);
        assertThat(query.getQueryObject().get("intersectionID")).isEqualTo(intersectionID);
        assertThat(query.getQueryObject().containsKey("_id")).isFalse();
    }

    @Test
    void testGetQueryResultCount() {
        Query query = new Query();
        when(mongoTemplate.count(eq(query), eq(IntersectionConfig.class), anyString())).thenReturn(7L);

        long count = repository.getQueryResultCount(query);

        assertThat(count).isEqualTo(7L);
        verify(mongoTemplate).count(eq(query), eq(IntersectionConfig.class), eq("CmIntersectionConfig"));
    }

    @Test
    void testFind() {
        Query query = new Query();
        IntersectionConfig<?> config = mock(IntersectionConfig.class);
        when(mongoTemplate.find(eq(query), eq(IntersectionConfig.class), anyString())).thenReturn(List.of(config));

        List<IntersectionConfig> result = repository.find(query);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(config);
        verify(mongoTemplate).find(eq(query), eq(IntersectionConfig.class), eq("CmIntersectionConfig"));
    }

    @Test
    void testDelete() {
        Query query = new Query();
        doReturn(null).when(mongoTemplate).remove(eq(query), eq(IntersectionConfig.class), anyString());

        repository.delete(query);

        verify(mongoTemplate).remove(eq(query), eq(IntersectionConfig.class), eq("CmIntersectionConfig"));
    }

    @Test
    void testSave() {
        IntersectionConfig<String> config = mock(IntersectionConfig.class);
        when(config.getKey()).thenReturn("key1");
        when(config.getCategory()).thenReturn("cat1");
        when(config.getValue()).thenReturn("val1");
        when(config.getType()).thenReturn("java.lang.String");
        when(config.getUnits()).thenReturn(UnitsEnum.CENTIMETERS);
        when(config.getDescription()).thenReturn("desc1");
        when(config.getUpdateType()).thenReturn(UpdateType.INTERSECTION);
        when(config.getIntersectionID()).thenReturn(321);

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmIntersectionConfig"));
        Document updateDoc = (Document) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(updateDoc.get("key")).isEqualTo("key1");
        assertThat(updateDoc.get("category")).isEqualTo("cat1");
        assertThat(updateDoc.get("value")).isEqualTo("val1");
        assertThat(updateDoc.get("type")).isEqualTo("java.lang.String");
        assertThat(updateDoc.get("units")).isEqualTo(UnitsEnum.CENTIMETERS);
        assertThat(updateDoc.get("description")).isEqualTo("desc1");
        assertThat(updateDoc.get("updateType")).isEqualTo(UpdateType.INTERSECTION);
        assertThat(updateDoc.get("intersectionID")).isEqualTo(321);
    }
}