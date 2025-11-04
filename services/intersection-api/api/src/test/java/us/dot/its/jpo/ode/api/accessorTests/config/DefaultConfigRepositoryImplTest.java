package us.dot.its.jpo.ode.api.accessorTests.config;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
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
        DefaultConfig<?> config = mock(DefaultConfig.class);
        when(mongoTemplate.find(eq(query), eq(DefaultConfig.class), anyString())).thenReturn(List.of(config));

        List<DefaultConfig> result = repository.find(query);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(config);
        verify(mongoTemplate).find(eq(query), eq(DefaultConfig.class), eq("CmDefaultConfig"));
    }

    @Test
    void testSaveIntegerType() {
        DefaultConfig<String> config = mock(DefaultConfig.class);
        when(config.getKey()).thenReturn("intKey");
        when(config.getType()).thenReturn("java.lang.Integer");
        when(config.getValue()).thenReturn("42");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value")).isEqualTo(42);
    }

    @Test
    void testSaveDoubleType() {
        DefaultConfig<String> config = mock(DefaultConfig.class);
        when(config.getKey()).thenReturn("doubleKey");
        when(config.getType()).thenReturn("java.lang.Double");
        when(config.getValue()).thenReturn("3.14");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value")).isEqualTo(3.14);
    }

    @Test
    void testSaveLongType() {
        DefaultConfig<String> config = mock(DefaultConfig.class);
        when(config.getKey()).thenReturn("longKey");
        when(config.getType()).thenReturn("java.lang.Long");
        when(config.getValue()).thenReturn("123456789");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value"))
                .isEqualTo(123456789L);
    }

    @Test
    void testSaveStringType() {
        DefaultConfig<String> config = mock(DefaultConfig.class);
        when(config.getKey()).thenReturn("stringKey");
        when(config.getType()).thenReturn("java.lang.String");
        when(config.getValue()).thenReturn("testValue");

        doReturn(null).when(mongoTemplate).upsert(any(Query.class), any(Update.class), anyString());

        repository.save(config);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq("CmDefaultConfig"));
        assertThat(((Document) updateCaptor.getValue().getUpdateObject().get("$set")).get("value"))
                .isEqualTo("testValue");
    }
}