package us.dot.its.jpo.ode.api.accessors.map;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.IntersectionReferenceData;

@Component
public class ProcessedMapRepositoryImpl implements ProcessedMapRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("properties.intersectionId").is(intersectionID));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }

        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "notificationGeneratedAt"));
            query.limit(1);
        }

        query.addCriteria(Criteria.where("properties.timeStamp").gte(startTimeString).lte(endTimeString));
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, ProcessedMap.class, "ProcessedMap");
    }

    public List<ProcessedMap> findProcessedMaps(Query query) {
        // return mongoTemplate.find(query, ProcessedMap.class, "OdeMapJson1234");
        return mongoTemplate.find(query, ProcessedMap.class, "ProcessedMap");
    }

    public List<IntersectionReferenceData> getIntersectionIDs() {
        GroupOperation groupOperator = Aggregation.group("properties.intersectionId", "properties.originIp")
                .first("properties.intersectionId").as("intersectionID")
                .first("properties.originIp").as("rsuIP");

        Aggregation aggregation = Aggregation.newAggregation(groupOperator);

        AggregationResults<IntersectionReferenceData> output = mongoTemplate.aggregate(aggregation, "ProcessedMap",
                IntersectionReferenceData.class);
        List<IntersectionReferenceData> referenceData = output.getMappedResults();
        return referenceData;
    }

}