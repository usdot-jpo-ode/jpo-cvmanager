package us.dot.its.jpo.ode.api.accessorTests.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;
import us.dot.its.jpo.ode.api.accessors.events.map_message_count_progression_event.MapMessageCountProgressionEventRepositoryImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MapMessageCountProgressionEventRepositoryImplTest {

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    MapMessageCountProgressionEventRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    Long endTime = 1724170778205L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new MapMessageCountProgressionEventRepositoryImpl(mongoTemplate);
    }

    @Test
    void testCount() {
        long expectedCount = 10;
        when(mongoTemplate.count(any(Query.class), eq("CmMapMessageCountProgressionEvents")))
                .thenReturn(expectedCount);

        long resultCount = repository.count(intersectionID, startTime, endTime);

        assertThat(resultCount).isEqualTo(expectedCount);
        verify(mongoTemplate).count(any(Query.class), eq("CmMapMessageCountProgressionEvents"));
    }

    @Test
    void testFindLatest() {
        MapMessageCountProgressionEvent event = new MapMessageCountProgressionEvent();
        event.setIntersectionID(intersectionID);

        when(mongoTemplate.findOne(any(Query.class), eq(MapMessageCountProgressionEvent.class),
                eq("CmMapMessageCountProgressionEvents")))
                .thenReturn(event);

        Page<MapMessageCountProgressionEvent> page = repository.findLatest(intersectionID, startTime, endTime);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getIntersectionID()).isEqualTo(intersectionID);
        verify(mongoTemplate).findOne(any(Query.class), eq(MapMessageCountProgressionEvent.class),
                eq("CmMapMessageCountProgressionEvents"));
    }

    @Test
    void testFind() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        MapMessageCountProgressionEventRepositoryImpl spyRepo = Mockito.spy(repository);
        Page<MapMessageCountProgressionEvent> mockPage = mock(Page.class);

        doReturn(mockPage).when(spyRepo).findPage(
                any(MongoTemplate.class),
                eq("CmMapMessageCountProgressionEvents"),
                eq(pageRequest),
                any(),
                any(Sort.class),
                isNull(),
                eq(MapMessageCountProgressionEvent.class));

        doCallRealMethod().when(spyRepo).find(intersectionID, startTime, endTime, pageRequest);

        Page<MapMessageCountProgressionEvent> results = spyRepo.find(intersectionID, startTime, endTime, pageRequest);

        assertThat(results).isEqualTo(mockPage);
    }
}