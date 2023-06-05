
package us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import org.springframework.data.domain.Sort;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import us.dot.its.jpo.ode.api.models.IDCount;

@Component
public class LaneDirectionOfTravelEventRepositoryImpl implements LaneDirectionOfTravelEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final double METERS_TO_FEET = 0.3048;

    private String collectionName = "CmLaneDirectionOfTravelEvent";

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }
        Date startTimeDate = new Date(0);
        Date endTimeDate = new Date();

        if (startTime != null) {
            startTimeDate = new Date(startTime);
        }
        if (endTime != null) {
            endTimeDate = new Date(endTime);
        }

        query.addCriteria(Criteria.where("eventGeneratedAt").gte(startTimeDate).lte(endTimeDate));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "notificationGeneratedAt"));
            query.limit(1);
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, LaneDirectionOfTravelEvent.class, collectionName);
    }

    public List<LaneDirectionOfTravelEvent> find(Query query) {
        return mongoTemplate.find(query, LaneDirectionOfTravelEvent.class, collectionName);
    }

    public List<IDCount> getLaneDirectionOfTravelEventsByDay(int intersectionID, Long startTime, Long endTime){
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
                .and(DateOperators.DateToString.dateOf("date").toString("%Y-%m-%d")).as("dateStr"),
            Aggregation.group("dateStr").count().as("count")
        );

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        return results;
    }

    public List<IDCount> getMedianDistanceByFoot(int intersectionID, long startTime, long endTime){

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
            Aggregation.match(Criteria.where("timestamp").gte(startTime).lte(endTime)),
            Aggregation.project()
                .and(ArithmeticOperators.Multiply.valueOf("medianDistanceFromCenterline").multiplyBy(METERS_TO_FEET)).as("medianDistanceFromCenterlineFeet"),
            Aggregation.project()
                .and(ArithmeticOperators.Trunc.truncValueOf("medianDistanceFromCenterlineFeet")).as("medianDistanceFromCenterlineFeet"),
            Aggregation.group("medianDistanceFromCenterlineFeet").count().as("count"),
            Aggregation.sort(Sort.Direction.ASC, "medianDistanceFromCenterlineFeet")
        );

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        return results;
    }

    public List<IDCount> getMedianDistanceByDegree(int intersectionID, long startTime, long endTime){

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
            Aggregation.match(Criteria.where("timestamp").gte(startTime).lte(endTime)),
            Aggregation.project()
                .and(ArithmeticOperators.Subtract.valueOf("medianVehicleHeading").subtract("expectedHeading")).as("medianHeadingDelta"),
            Aggregation.project()
                .and(ArithmeticOperators.Trunc.truncValueOf("medianHeadingDelta")).as("medianHeadingDelta"),
            Aggregation.group("medianHeadingDelta").count().as("count"),
            Aggregation.sort(Sort.Direction.ASC, "medianHeadingDelta")
        );

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        return results;
    }

    @Override
    public void add(LaneDirectionOfTravelEvent item) {
        mongoTemplate.save(item, collectionName);
    }

}
