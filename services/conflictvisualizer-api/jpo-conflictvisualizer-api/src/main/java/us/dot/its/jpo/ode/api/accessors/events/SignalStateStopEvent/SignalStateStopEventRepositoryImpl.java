
package us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.IDCount;

@Component
public class SignalStateStopEventRepositoryImpl implements SignalStateStopEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private String collectionName = "CmStopLineStopEvent";

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
            query.with(Sort.by(Sort.Direction.DESC, "eventGeneratedAt"));
            query.limit(1);
        }else{
            query.limit(props.getMaximumResponseSize());
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, StopLineStopEvent.class, collectionName);
    }

    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, StopLineStopEvent.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<StopLineStopEvent> find(Query query) {
        return mongoTemplate.find(query, StopLineStopEvent.class, collectionName);
    }

    public List<IDCount> getSignalStateStopEventsByDay(int intersectionID, Long startTime, Long endTime){
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
                .and(DateOperators.DateToString.dateOf("eventGeneratedAt").toString("%Y-%m-%d")).as("dateStr"),
            Aggregation.group("dateStr").count().as("count")
        );

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        return results;
    }

    @Override
    public void add(StopLineStopEvent item) {
        mongoTemplate.save(item, collectionName);
    }

}
