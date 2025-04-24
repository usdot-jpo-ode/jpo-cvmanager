package us.dot.its.jpo.ode.api.accessorTests;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.models.AggregationResult2;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith( org.mockito.junit.jupiter.MockitoExtension.class)
public class PageableQueryTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private PageableQuery paginatedQueryInterface;
    @Mock
    DbRefResolver dbRefResolver;
    @Mock
    MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;

    @Test
    void testFindPaginatedData() {
        Pageable pageable = PageRequest.of(0, 10);
        Criteria criteria = new Criteria();
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");

        List<ConnectionOfTravelNotification> expectedData = Arrays.asList(new ConnectionOfTravelNotification(),
                new ConnectionOfTravelNotification());
        AggregationResult2 aggregationResult = new AggregationResult2();
        Document dataDoc = new Document();
        dataDoc.put("results", expectedData);
        aggregationResult.setResults(List.of(dataDoc));
        AggregationResultCount count = new AggregationResultCount();
        count.setCount(2L);
        aggregationResult.setMetadata(List.of(count));

        AggregationResults<AggregationResult2> aggregationResults = new AggregationResults<>(
                List.of(aggregationResult), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult2.class)))
                .thenReturn(aggregationResults);

        Page<ConnectionOfTravelNotification> result = paginatedQueryInterface.findPage2(mongoTemplate,
                "collectionName", pageable, criteria, sort, null, ConnectionOfTravelNotification.class);

        assertThat(result.getContent()).isEqualTo(expectedData);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testFindPaginatedDataWithEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Criteria criteria = new Criteria();
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");

        @SuppressWarnings("rawtypes")
        AggregationResults<AggregationResult2> aggregationResults = new AggregationResults<>(Collections.emptyList(),
                new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult2.class)))
                .thenReturn(aggregationResults);

        Page<ConnectionOfTravelNotification> result = paginatedQueryInterface.findPage2(mongoTemplate,
                "collectionName", pageable, criteria, sort, null, ConnectionOfTravelNotification.class);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testFindPaginatedDataWithNullResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Criteria criteria = new Criteria();
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");

        AggregationResults<AggregationResult2> aggregationResults = new AggregationResults<>(
                Collections.emptyList(), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult2.class)))
                .thenReturn(aggregationResults);

        Page<ConnectionOfTravelNotification> result = paginatedQueryInterface.findPage2(mongoTemplate,
                "collectionName", pageable, criteria, sort, null, ConnectionOfTravelNotification.class);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testWrapSingleResultWithPage() {
        String latest = "latestData";
        Page<String> page = paginatedQueryInterface.wrapSingleResultWithPage(latest);

        assertThat(page.getContent()).containsExactly(latest);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void testWrapSingleResultWithPageNull() {
        Page<String> page = paginatedQueryInterface.wrapSingleResultWithPage(null);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void testCreateNullablePageWithValidPageAndSize() {
        // Test with valid page and size
        Integer page = 2;
        Integer size = 10;

        Pageable pageable = paginatedQueryInterface.createNullablePage(page, size);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    public void testCreateNullablePageWithNullPage() {
        // Test with null page and valid size
        Integer page = null;
        Integer size = 10;

        Pageable pageable = paginatedQueryInterface.createNullablePage(page, size);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageNumber()).isEqualTo(0); // Default page is 0
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    public void testCreateNullablePageWithNullSize() {
        // Test with null size
        Integer page = 2;
        Integer size = null;

        Pageable pageable = paginatedQueryInterface.createNullablePage(page, size);

        assertThat(pageable).isNull(); // Should return null when size is null
    }

    @Test
    public void testCreateNullablePageWithNullPageAndSize() {
        // Test with both page and size as null
        Integer page = null;
        Integer size = null;

        Pageable pageable = paginatedQueryInterface.createNullablePage(page, size);

        assertThat(pageable).isNull(); // Should return null when size is null
    }

    @Test
    public void testCreateNullablePageWithZeroPageAndSize() {
        // Test with page as 0 and size as a valid value
        Integer page = 0;
        Integer size = 5;

        Pageable pageable = paginatedQueryInterface.createNullablePage(page, size);

        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }
}