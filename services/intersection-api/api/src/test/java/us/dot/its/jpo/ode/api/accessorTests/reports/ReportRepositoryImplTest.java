package us.dot.its.jpo.ode.api.accessorTests.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import us.dot.its.jpo.ode.api.accessors.reports.ReportRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportRepositoryImplTest {

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    ReportRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ReportRepositoryImpl(mongoTemplate);
    }

    @Test
    void testCount() {
        when(mongoTemplate.count(any(Query.class), eq("CmReport"))).thenReturn(42L);

        long count = repository.count("report1", 123, 1000L, 2000L);

        assertThat(count).isEqualTo(42L);
        verify(mongoTemplate).count(any(Query.class), eq("CmReport"));
    }

    @Test
    void testFindLatest() {
        ReportDocument doc = mock(ReportDocument.class);
        when(mongoTemplate.findOne(any(Query.class), eq(ReportDocument.class), eq("CmReport"))).thenReturn(doc);

        Page<ReportDocument> page = repository.findLatest("report1", 123, 1000L, 2000L, true);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst()).isEqualTo(doc);
        verify(mongoTemplate).findOne(any(Query.class), eq(ReportDocument.class), eq("CmReport"));
    }

    @Test
    void testFind() {
        ReportDocument doc = mock(ReportDocument.class);
        Pageable pageable = mock(Pageable.class);
        // Simulate findPage returns a PageImpl with one doc
        ReportRepositoryImpl spyRepo = Mockito.spy(repository);
        doReturn(new PageImpl<>(List.of(doc))).when(spyRepo)
                .findPage(any(), anyString(), eq(pageable), any(), any(Sort.class), anyList(),
                        eq(ReportDocument.class));

        Page<ReportDocument> page = spyRepo.find("report1", 123, 1000L, 2000L, false, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst()).isEqualTo(doc);
    }

    @Test
    void testAdd() {
        ReportDocument doc = mock(ReportDocument.class);

        repository.add(doc);

        verify(mongoTemplate).insert(doc, "CmReport");
    }
}