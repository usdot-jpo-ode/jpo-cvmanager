package us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;

@Component
public class ConnectionOfTravelEventRepositoryImpl
        implements ConnectionOfTravelEventRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmConnectionOfTravelEvent";
    private final String DATE_FIELD = "eventGeneratedAt";
    private final String INTERSECTION_ID_FIELD = "intersectionID";

    @Autowired
    public ConnectionOfTravelEventRepositoryImpl(MongoTemplate mongoTemplate) {
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
     * @param pageable       the pageable object to use for pagination
     * @return the paginated data that matches the given criteria
     */
    public long count(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            @Nullable Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Query query = Query.query(criteria);
        if (pageable != null) {
            query = query.with(pageable);
        }
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
    public Page<ConnectionOfTravelEvent> findLatest(
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
                        ConnectionOfTravelEvent.class,
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
    public Page<ConnectionOfTravelEvent> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return findPage(mongoTemplate, collectionName, pageable, criteria, sort);
    }

    public List<IDCount> getAggregatedDailyConnectionOfTravelEventCounts(int intersectionID, Long startTime,
            Long endTime) {
        Date startTimeDate = new Date(0);
        Date endTimeDate = new Date();

        if (startTime != null) {
            startTimeDate = new Date(startTime);
        }
        if (endTime != null) {
            endTimeDate = new Date(endTime);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
                Aggregation.match(Criteria.where("eventGeneratedAt").gte(startTimeDate).lte(endTimeDate)),
                Aggregation.project()
                        .and(DateOperators.DateToString.dateOf("eventGeneratedAt").toString("}%Y-%m-%d")).as("dateStr"),
                Aggregation.group("dateStr").count().as("count"));

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);

        return result.getMappedResults();
    }

    public List<LaneConnectionCount> getConnectionOfTravelEventsByConnection(int intersectionID, Long startTime,
            Long endTime) {
        Date startTimeDate = new Date(0);
        Date endTimeDate = new Date();

        if (startTime != null) {
            startTimeDate = new Date(startTime);
        }
        if (endTime != null) {
            endTimeDate = new Date(endTime);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
                Aggregation.match(Criteria.where("eventGeneratedAt").gte(startTimeDate).lte(endTimeDate)),
                Aggregation.project("ingressLaneID", "egressLaneID"),
                Aggregation.group("ingressLaneID", "egressLaneID").count().as("count"),
                Aggregation.sort(Sort.Direction.ASC, "ingressLaneID", "egressLaneID"),
                Aggregation.project("ingressLaneID", "egressLaneID", "count"));

        AggregationResults<LaneConnectionCount> result = mongoTemplate.aggregate(aggregation, collectionName,
                LaneConnectionCount.class);

        return result.getMappedResults();
    }

    @Override
    public void add(ConnectionOfTravelEvent item) {
        mongoTemplate.insert(item, collectionName);
    }
}
