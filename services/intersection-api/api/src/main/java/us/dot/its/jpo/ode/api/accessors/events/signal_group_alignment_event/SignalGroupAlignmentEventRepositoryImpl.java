
package us.dot.its.jpo.ode.api.accessors.events.signal_group_alignment_event;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import us.dot.its.jpo.ode.api.models.IDCount;

@Component
public class SignalGroupAlignmentEventRepositoryImpl
        implements SignalGroupAlignmentEventRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmSignalGroupAlignmentEvents";
    private final String DATE_FIELD = "eventGeneratedAt";
    private final String INTERSECTION_ID_FIELD = "intersectionID";

    @Autowired
    public SignalGroupAlignmentEventRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get a page representing the count of data for a given intersectionID,
     * startTime, and endTime
     *
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @return the paginated data that matches the given criteria
     */
    public long count(
            Integer intersectionID,
            Long startTime,
            Long endTime) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Query query = Query.query(criteria);
        return mongoTemplate.count(query, collectionName);
    }

    /**
     * Get a page containing the single most recent record for a given
     * intersectionID, startTime, and endTime
     *
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @return the paginated data that matches the given criteria
     */
    public Page<SignalGroupAlignmentEvent> findLatest(
            Integer intersectionID,
            Long startTime,
            Long endTime) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Query query = Query.query(criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return wrapSingleResultWithPage(
                mongoTemplate.findOne(
                        query.with(sort),
                        SignalGroupAlignmentEvent.class,
                        collectionName));
    }

    /**
     * Get paginated data from a given intersectionID, startTime, and endTime
     *
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @param pageable       the pageable object to use for pagination
     * @return the paginated data that matches the given criteria
     */
    public Page<SignalGroupAlignmentEvent> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return findPage(mongoTemplate, collectionName, pageable, criteria, sort, null, SignalGroupAlignmentEvent.class);
    }

    public List<IDCount> getAggregatedDailySignalGroupAlignmentEventCounts(int intersectionID, Long startTime,
            Long endTime) {
        if (startTime == null) {
            startTime = 0L;
        }
        if (endTime == null) {
            endTime = Instant.now().toEpochMilli();
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
                Aggregation.match(Criteria.where("timestamp").gte(startTime).lte(endTime)),
                Aggregation.project("timestamp"),
                Aggregation.project()
                        .and(ConvertOperators.ToDate.toDate("$timestamp")).as("date"),
                Aggregation.project()
                        .and(DateOperators.DateToString.dateOf("date").toString("}%Y-%m-%d")).as("dateStr"),
                Aggregation.group("dateStr").count().as("count"));

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, "CmLaneDirectionOfTravelEvent",
                IDCount.class);

        return result.getMappedResults();
    }

    @Override
    public void add(SignalGroupAlignmentEvent item) {
        mongoTemplate.insert(item, collectionName);
    }

}
