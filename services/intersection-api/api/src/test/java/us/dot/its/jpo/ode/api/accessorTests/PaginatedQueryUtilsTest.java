package us.dot.its.jpo.ode.api.accessorTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.accessors.PaginatedQueryUtils;
import us.dot.its.jpo.ode.api.models.AggregationMetadata;
import us.dot.its.jpo.ode.api.models.AggregationResult;

public class PaginatedQueryUtilsTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuildCriteriaWithAllParameters() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1735689600000L, 1735693200000L);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"intersectionID\": 1, \"dateField\": {\"$gte\": {\"$date\": \"2025-01-01T00:00:00Z\"}, \"$lte\": {\"$date\": \"2025-01-01T01:00:00Z\"}}}");
    }

    @Test
    void testBuildCriteriaWithNullIntersectionID() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", null, 1735689600000L, 1735693200000L);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": {\"$date\": \"2025-01-01T00:00:00Z\"}, \"$lte\": {\"$date\": \"2025-01-01T01:00:00Z\"}}}");
    }

    @Test
    void testBuildCriteriaWithNullStartTime() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, null, 1735693200000L);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"intersectionID\": 1, \"dateField\": {\"$lte\": {\"$date\": \"2025-01-01T01:00:00Z\"}}}");
    }

    @Test
    void testBuildCriteriaWithNullEndTime() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1735689600000L, null);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"intersectionID\": 1, \"dateField\": {\"$gte\": {\"$date\": \"2025-01-01T00:00:00Z\"}}}");
    }

    @Test
    void testBuildCriteriaWithNullStartTimeAndEndTime() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, null, null);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"intersectionID\": 1}");
    }

    @Test
    void testBuildCriteriaWithNullIntersectionIDAndStartTime() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", null, null, 1735693200000L);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$lte\": {\"$date\": \"2025-01-01T01:00:00Z\"}}}");
    }

    @Test
    void testBuildCriteriaWithNullIntersectionIDAndEndTime() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", null, 1735689600000L, null);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": {\"$date\": \"2025-01-01T00:00:00Z\"}}}");
    }

    @Test
    void testBuildCriteriaWithAllNullParameters() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", null, null, null);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo("{}");
    }

    @Test
    void testWrapLatestInPage() {
        String latest = "latestData";
        Page<String> page = PaginatedQueryUtils.wrapLatestInPage(latest);
        assertThat(page.getContent()).containsExactly(latest);
    }

    @Test
    void testCountPagedDataFromArgs() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        Pageable pageable = PageRequest.of(0, 10);
        when(mongoTemplate.count(any(Query.class), eq("collectionName"))).thenReturn(100L);

        long count = PaginatedQueryUtils.countPagedDataFromArgs(mongoTemplate, "collectionName", "dateField", pageable,
                1, 1624640400000L, 1624726799000L);
        assertThat(count).isEqualTo(100L);
    }

    @Test
    void testCountDataFromArgs() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        when(mongoTemplate.count(any(Query.class), eq("collectionName"))).thenReturn(100L);

        long count = PaginatedQueryUtils.countDataFromArgs(mongoTemplate, "collectionName", "dateField", 1,
                1624640400000L, 1624726799000L);
        assertThat(count).isEqualTo(100L);
    }

    @Test
    void testGetLatestDataFromArgs() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");
        String expectedData = "latestData";
        when(mongoTemplate.findOne(any(Query.class), eq(String.class), eq("collectionName"))).thenReturn(expectedData);

        String latestData = PaginatedQueryUtils.getLatestDataFromArgs(mongoTemplate, "collectionName", "dateField",
                String.class, 1, 1624640400000L, 1624726799000L);
        assertThat(latestData).isEqualTo(expectedData);
    }

    @Test
    void testGetDataFromArgs() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        Pageable pageable = PageRequest.of(0, 10);
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");
        List<String> expectedData = Arrays.asList("data1", "data2");
        AggregationResult<String> aggregationResult = new AggregationResult<>();
        aggregationResult.setData(expectedData);
        AggregationMetadata metadata = new AggregationMetadata();
        metadata.setTotalCount(2);
        aggregationResult.setMetadata(Arrays.asList(metadata));
        AggregationResults<AggregationResult> aggregationResults = new AggregationResults<>(
                Arrays.asList(aggregationResult), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult.class)))
                .thenReturn(aggregationResults);

        Page<String> page = PaginatedQueryUtils.getDataFromArgs(mongoTemplate, "collectionName", "dateField", pageable,
                1, 1624640400000L, 1624726799000L);
        assertThat(page.getContent()).isEqualTo(expectedData);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testCountData() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        when(mongoTemplate.count(any(Query.class), eq("collectionName"))).thenReturn(100L);

        long count = PaginatedQueryUtils.countData(mongoTemplate, "collectionName", criteria);
        assertThat(count).isEqualTo(100L);
    }

    @Test
    void testCountPagedData() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        Pageable pageable = PageRequest.of(0, 10);
        when(mongoTemplate.count(any(Query.class), eq("collectionName"))).thenReturn(100L);

        long count = PaginatedQueryUtils.countPagedData(mongoTemplate, "collectionName", criteria, pageable);
        assertThat(count).isEqualTo(100L);
    }

    @Test
    void testGetLatestData() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");
        String expectedData = "latestData";
        when(mongoTemplate.findOne(any(Query.class), eq(String.class), eq("collectionName"))).thenReturn(expectedData);

        String latestData = PaginatedQueryUtils.getLatestData(mongoTemplate, "collectionName", criteria, sort,
                String.class);
        assertThat(latestData).isEqualTo(expectedData);
    }

    @Test
    void testFindPaginatedData() {
        Criteria criteria = PaginatedQueryUtils.buildCriteria("dateField", 1, 1624640400000L, 1624726799000L);
        Pageable pageable = PageRequest.of(0, 10);
        Sort sort = Sort.by(Sort.Direction.DESC, "dateField");
        List<String> expectedData = Arrays.asList("data1", "data2");
        AggregationResult<String> aggregationResult = new AggregationResult<>();
        aggregationResult.setData(expectedData);
        AggregationMetadata metadata = new AggregationMetadata();
        metadata.setTotalCount(2);
        aggregationResult.setMetadata(Arrays.asList(metadata));
        AggregationResults<AggregationResult> aggregationResults = new AggregationResults<>(
                Arrays.asList(aggregationResult), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("collectionName"), eq(AggregationResult.class)))
                .thenReturn(aggregationResults);

        Page<String> page = PaginatedQueryUtils.findPaginatedData(mongoTemplate, "collectionName", pageable, criteria,
                sort);
        assertThat(page.getContent()).isEqualTo(expectedData);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}