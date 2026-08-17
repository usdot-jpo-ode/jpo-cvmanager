package us.dot.its.jpo.ode.api.accessorTests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;

public class IntersectionCriteriaTest {

    @Test
    void testWithinTimeWindowWithBothTimes() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", 1735689600000L, 1735693200000L,
                IntersectionCriteria.TimeStampFormat.DATE);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": {\"$date\": \"2025-01-01T00:00:00Z\"}, \"$lte\": {\"$date\": \"2025-01-01T01:00:00Z\"}}}");
    }

    @Test
    void testWithinTimeWindowWithStartTimeOnly() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();

        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", 1735689600000L, null,
                IntersectionCriteria.TimeStampFormat.DATE);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": {\"$date\": \"2025-01-01T00:00:00Z\"}}}");
    }

    @Test
    void testWithinTimeWindowWithEndTimeOnly() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", null, 1735693200000L,
                IntersectionCriteria.TimeStampFormat.DATE);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$lte\": {\"$date\": \"2025-01-01T01:00:00Z\"}}}");
    }

    @Test
    void testWithinTimeWindowWithNoTimes() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", null, null,
                IntersectionCriteria.TimeStampFormat.DATE);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo("{}");
    }

    @Test
    void testWithinTimeWindowWithBothTimesStringFormat() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", 1735689600000L, 1735693200000L,
                IntersectionCriteria.TimeStampFormat.STRING);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": \"2025-01-01T00:00:00Z\", \"$lte\": \"2025-01-01T01:00:00Z\"}}");
    }

    @Test
    void testWithinTimeWindowWithStartTimeOnlyStringFormat() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", 1735689600000L, null,
                IntersectionCriteria.TimeStampFormat.STRING);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": \"2025-01-01T00:00:00Z\"}}");
    }

    @Test
    void testWithinTimeWindowWithBothTimesLongFormat() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", 1735689600000L, 1735693200000L,
                IntersectionCriteria.TimeStampFormat.LONG);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$gte\": 1735689600000, \"$lte\": 1735693200000}}");
    }

    @Test
    void testWithinTimeWindowWithEndTimeOnlyLongFormat() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.withinTimeWindow("dateField", null, 1735693200000L,
                IntersectionCriteria.TimeStampFormat.LONG);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"dateField\": {\"$lte\": 1735693200000}}");
    }

    @Test
    void testWhereOptionalWithValue() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.whereOptional("fieldName", "value");
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo(
                "{\"fieldName\": \"value\"}");
    }

    @Test
    void testWhereOptionalWithNullValue() {
        IntersectionCriteria intersectionCriteria = new IntersectionCriteria();
        Criteria criteria = intersectionCriteria.whereOptional("fieldName", null);
        assertThat(criteria.getCriteriaObject().toJson()).isEqualTo("{}");
    }
}