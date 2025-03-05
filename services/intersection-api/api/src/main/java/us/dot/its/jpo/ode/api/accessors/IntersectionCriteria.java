package us.dot.its.jpo.ode.api.accessors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Date;

public class IntersectionCriteria extends Criteria {

    /**
     * Build a query criteria object based on a time window
     *
     * @param fieldName        the db field to apply criteria to
     * @param startEpochMillis the nullable start time of the window, in
     *                         milliseconds since epoch
     * @param endEpochMillis   the nullable end time of the window, in milliseconds
     *                         since epoch
     * @return the criteria object to use for querying
     */
    public IntersectionCriteria withinTimeWindow(@Nonnull String fieldName,
            @Nullable Long startEpochMillis,
            @Nullable Long endEpochMillis) {
        if (startEpochMillis != null && endEpochMillis != null) {
            this.and(fieldName)
                    .gte(Date.from(Instant.ofEpochMilli(startEpochMillis)))
                    .lte(Date.from(Instant.ofEpochMilli(endEpochMillis)));
            return this;
        } else if (startEpochMillis != null) {
            this.and(fieldName).gte(new Date(startEpochMillis));
        } else if (endEpochMillis != null) {
            this.and(fieldName).lte(new Date(endEpochMillis));
        }
        return this;
    }

    /**
     * Build a query criteria object based on a time window
     *
     * @param <T>       the type of the value to compare against
     * @param fieldName the db datetime field to apply criteria to
     * @param value     the value to compare against
     * @return the criteria object to use for querying
     */
    public <T> IntersectionCriteria whereOptional(@Nonnull String fieldName,
            @Nullable T value) {
        if (value != null) {
            this.and(fieldName).is(value);
        }
        return this;
    }

    /**
     * Creates a query object based on the current criteria.
     *
     * @return a Query object with the applied criteria
     */
    public <T> Query toQuery() {
        return Query.query(this);
    }

    /**
     * Creates a query object with pagination based on the current criteria.
     *
     * @param pageable the pagination information to apply to the query, can be null
     * @return a Query object with the applied criteria and pagination
     */
    public <T> Query toQuery(@Nullable Pageable pageable) {
        if (pageable == null) {
            return Query.query(this);
        }
        return Query.query(this).with(pageable);
    }
}
