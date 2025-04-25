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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.models.AggregationResult;
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

    @Test
    void testFindPaginatedData() {
        Pageable pageable = PageRequest.of(0, 10);
        Criteria criteria = new Criteria();
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");

        List<ConnectionOfTravelNotification> expectedData = Arrays.asList(new ConnectionOfTravelNotification(),
                new ConnectionOfTravelNotification());
        AggregationResult aggregationResult = new AggregationResult();
        Document dataDoc = new Document();
        dataDoc.put("results", expectedData);
        aggregationResult.setResults(List.of(dataDoc));
        AggregationResultCount count = new AggregationResultCount();
        count.setCount(2L);
        aggregationResult.setMetadata(List.of(count));

        AggregationResults<AggregationResult> aggregationResults = new AggregationResults<>(
                List.of(aggregationResult), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult.class)))
                .thenReturn(aggregationResults);

        Page<ConnectionOfTravelNotification> result = paginatedQueryInterface.findPage(mongoTemplate,
                "collectionName", pageable, criteria, sort, Collections.emptyList(), ConnectionOfTravelNotification.class);

        assertThat(result.getContent()).isEqualTo(expectedData);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testFindPaginatedDataWithEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Criteria criteria = new Criteria();
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");

        AggregationResults<AggregationResult> aggregationResults = new AggregationResults<>(Collections.emptyList(),
                new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult.class)))
                .thenReturn(aggregationResults);

        Page<ConnectionOfTravelNotification> result = paginatedQueryInterface.findPage(mongoTemplate,
                "collectionName", pageable, criteria, sort, Collections.emptyList(), ConnectionOfTravelNotification.class);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testFindPaginatedDataWithNullResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Criteria criteria = new Criteria();
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");

        AggregationResults<AggregationResult> aggregationResults = new AggregationResults<>(
                Collections.emptyList(), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult.class)))
                .thenReturn(aggregationResults);

        Page<ConnectionOfTravelNotification> result = paginatedQueryInterface.findPage(mongoTemplate,
                "collectionName", pageable, criteria, sort, Collections.emptyList(), ConnectionOfTravelNotification.class);

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
}