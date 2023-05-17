package us.dot.its.jpo.ode.api.accessors.spat;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.models.IDCount;
import org.springframework.data.domain.Sort;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

@Component
public class ProcessedSpatRepositoryImpl implements ProcessedSpatRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionId").is(intersectionID));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }

        query.addCriteria(Criteria.where("odeReceivedAt").gte(startTimeString).lte(endTimeString));
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, ProcessedSpat.class, "ProcessedSpat");
    }

    public List<ProcessedSpat> findProcessedMaps(Query query) {
        return mongoTemplate.find(query, ProcessedSpat.class, "ProcessedSpat");
    }

    public List<IDCount> getSpatBroadcastRates(int intersectionID, Long startTime, Long endTime){

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("intersectionId").is(intersectionID)),
            Aggregation.match(Criteria.where("utcTimeStamp").gte(startTimeString).lte(endTimeString)),
            Aggregation.project("utcTimeStamp"),
            Aggregation.project()
                .and(DateOperators.DateFromString.fromStringOf("utcTimeStamp")).as("date"),
            Aggregation.project()
                .and(DateOperators.DateToString.dateOf("date").toString("%Y-%m-%d-%H:%M:%S")).as("dateStr"),
            Aggregation.group("dateStr").count().as("count"),
            Aggregation.sort(Sort.Direction.ASC, "_id")
        );

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, "ProcessedSpat", IDCount.class);
        List<IDCount> results = result.getMappedResults();
        return results;
    }

}