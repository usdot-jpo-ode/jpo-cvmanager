package us.dot.its.jpo.ode.api.accessors.rsuState;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.Date;
import org.bson.Document;

import us.dot.its.jpo.ode.api.models.snmp.RsuState;

import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class RsuStateRepositoryImpl implements RsuStateRepository {

    private final MongoTemplate mongoTemplate;
    private final String collectionName = "IntersectionApiRsuStatus";

    @Autowired
    public RsuStateRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void add(RsuState item) {
        mongoTemplate.insert(item, collectionName);
    }

    @Override
    public List<RsuState> retrieveRsuStateWithinTimeInterval(String rsuIP, long start, long end) {
        Criteria criteria = Criteria.where("rsuIP").is(rsuIP)
                .and("timestamp").gte(new Date(start)).lte(new Date(end));
        Query query = Query.query(criteria).with(Sort.by(Sort.Direction.ASC, "timestamp"));
        return mongoTemplate.find(query, RsuState.class, collectionName);
    }

    @Override
    public List<RsuState> retrieveRsuStateWithinTimeInterval(String rsuIP, long start, long end, int intervalMinutes) {
        long intervalMs = intervalMinutes * 60 * 1000; // Convert minutes to milliseconds

        Criteria criteria = Criteria.where("rsuIP").is(rsuIP)
                .and("timestamp").gte(new Date(start)).lte(new Date(end));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), // filter by rsuIP and timestamp
                Aggregation.project("timestamp", "rsuIP", "temperature", "uptime", "mode")
                        .andExpression("{$convert: {input: \"$timestamp\", to: \"long\"}}").as("timestampMillis"),
                Aggregation.project("timestamp", "rsuIP", "temperature", "uptime", "mode", "timestampMillis")
                        .andExpression("timestampMillis - (timestampMillis % " + intervalMs + ")").as("interval"),
                Aggregation.group("interval") // Group by interval
                        .avg("temperature").as("temperature")
                        .avg("uptime").as("uptime")
                        .first("rsuIP").as("rsuIP")
                        .first("mode").as("mode")
                        .first("timestamp").as("timestamp"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "timestamp")) // Sort by timestamp
        );

        System.out.println("[RSU Status] Aggregation Pipeline: " + aggregation.toString());

        List<RsuState> results = mongoTemplate.aggregate(aggregation, collectionName, RsuState.class)
                .getMappedResults();

        return results;
    }

    @Override
    public List<RsuState> findByRsuIPOrderByTimestampDesc(String rsuIP) {
        Criteria criteria = Criteria.where("rsuIP").is(rsuIP);
        Query query = Query.query(criteria).with(Sort.by(Sort.Direction.DESC, "timestamp"));

        List<RsuState> results = mongoTemplate.find(query, RsuState.class, collectionName);
        return results;
    }
}