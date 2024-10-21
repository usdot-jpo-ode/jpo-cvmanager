package us.dot.its.jpo.ode.api.accessors.events.BsmEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;

import org.springframework.data.domain.Sort;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.IDCount;

@Component
public class BsmEventRepositoryImpl implements BsmEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private String collectionName = "CmBsmEvents";

    private ObjectMapper mapper = DateJsonMapper.getInstance();

    @Autowired
    ConflictMonitorApiProperties props;

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        if (startTime == null) {
            startTime = 0L;
        }
        if (endTime == null) {
            endTime = Instant.now().toEpochMilli();
        }

        query.addCriteria(Criteria.where("startingBsmTimestamp").gte(startTime).lte(endTime));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "startingBsmTimestamp"));
            query.limit(1);
        }else{
            query.limit(props.getMaximumResponseSize());
        }
        query.fields().exclude("recordGeneratedAt");
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, BsmEvent.class, collectionName);
    }

    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, BsmEvent.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<BsmEvent> find(Query query) {

        List<Map> documents = mongoTemplate.find(query, Map.class, collectionName);
        List<BsmEvent> convertedList = new ArrayList<>();

        for(Map document : documents){
            document.remove("_id");
            BsmEvent event = mapper.convertValue(document, BsmEvent.class);
            convertedList.add(event);
        }

        return convertedList;
    }

    @Override
    public void add(BsmEvent item) {
        mongoTemplate.save(item, collectionName);
    }

    @Override
    public List<IDCount> getBsmEventsByDay(int intersectionID, Long startTime, Long endTime) {
        if (startTime == null) {
            startTime = 0L;
        }
        if (endTime == null) {
            endTime = Instant.now().toEpochMilli();
        }

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
            Aggregation.match(Criteria.where("").gte(startTime).lte(endTime)),
            Aggregation.project("startingBsmTimestamp"),
            Aggregation.project()
                .and(ConvertOperators.ToDate.toDate("$startingBsmTimestamp")).as("date"),
            Aggregation.project()
                .and(DateOperators.DateToString.dateOf("date").toString("%Y-%m-%d")).as("dateStr"),
            Aggregation.group("dateStr").count().as("count")
        );

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        return results;
    }
}
