package us.dot.its.jpo.ode.api.accessorTests.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;
import us.dot.its.jpo.ode.api.accessors.events.spat_message_count_progression_event.SpatMessageCountProgressionRepositoryImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpatMessageCountProgressionRepositoryImplTest {

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    SpatMessageCountProgressionRepositoryImpl repository;

    Integer intersectionID = 123;
    Long startTime = 1724170658205L;
    Long endTime = 1724170778205L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new SpatMessageCountProgressionRepositoryImpl(mongoTemplate);
    }

    @Test
    void testCount() {
        long expectedCount = 10;
        when(mongoTemplate.count(any(Query.class), eq("CmSpatMessageCountProgressionEvents")))
                .thenReturn(expectedCount);

        long resultCount = repository.count(intersectionID, startTime, endTime);

        assertThat(resultCount).isEqualTo(expectedCount);
        verify(mongoTemplate).count(any(Query.class), eq("CmSpatMessageCountProgressionEvents"));
    }

    @Test
    void testFindLatest() {
        SpatMessageCountProgressionEvent event = new SpatMessageCountProgressionEvent();
        event.setIntersectionID(intersectionID);

        when(mongoTemplate.findOne(any(Query.class), eq(SpatMessageCountProgressionEvent.class),
                eq("CmSpatMessageCountProgressionEvents")))
                .thenReturn(event);

        Page<SpatMessageCountProgressionEvent> page = repository.findLatest(intersectionID, startTime, endTime);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getIntersectionID()).isEqualTo(intersectionID);
        verify(mongoTemplate).findOne(any(Query.class), eq(SpatMessageCountProgressionEvent.class),
                eq("CmSpatMessageCountProgressionEvents"));
    }

    @Test
    void testFind() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        SpatMessageCountProgressionRepositoryImpl spyRepo = Mockito.spy(repository);
        Page<SpatMessageCountProgressionEvent> mockPage = mock(Page.class);

        doReturn(mockPage).when(spyRepo).findPage(
                any(MongoTemplate.class),
                eq("CmSpatMessageCountProgressionEvents"),
                eq(pageRequest),
                any(),
                any(Sort.class),
                isNull(),
                eq(SpatMessageCountProgressionEvent.class));

        doCallRealMethod().when(spyRepo).find(intersectionID, startTime, endTime, pageRequest);

        Page<SpatMessageCountProgressionEvent> results = spyRepo.find(intersectionID, startTime, endTime, pageRequest);

        assertThat(results).isEqualTo(mockPage);
    }
}