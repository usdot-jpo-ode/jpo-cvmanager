package us.dot.its.jpo.ode.api.accessorTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import us.dot.its.jpo.ode.api.accessors.PageWrapper;

public class PageWrapperTest {

    private PageWrapper pageWrapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pageWrapper = mock(PageWrapper.class, CALLS_REAL_METHODS);
    }

    @Test
    void testWrapSingleResultWithPage() {
        String latest = "latestData";
        Page<String> page = pageWrapper.wrapSingleResultWithPage(latest);

        assertThat(page.getContent()).containsExactly(latest);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void testWrapSingleResultWithPageNull() {
        Page<String> page = pageWrapper.wrapSingleResultWithPage(null);

        assertThat(page.getContent()).containsExactly((String) null);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}