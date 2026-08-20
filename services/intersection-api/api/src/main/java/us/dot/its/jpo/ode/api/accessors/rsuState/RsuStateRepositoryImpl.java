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
    private final String collectionName = "RmMonitoringStatusRecords";

    @Autowired
    public RsuStateRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<RsuState> retrieveRsuStateWithinTimeInterval(String rsuIP, long start, long end) {
        Criteria criteria = Criteria.where("rsuIP").is(rsuIP)
                .and("timestamp").gte(new Date(start)).lte(new Date(end))
                .andOperator(
                        Criteria.where("uptime").ne(-1),
                        Criteria.where("temperature").ne(-1));
        Query query = Query.query(criteria).with(Sort.by(Sort.Direction.ASC, "timestamp"));
        return mongoTemplate.find(query, RsuState.class, collectionName);
    }

    @Override
    public List<RsuState> retrieveRsuStateWithinTimeInterval(String rsuIP, long start, long end, int intervalMinutes) {
        long intervalMs = intervalMinutes * 60 * 1000; // Convert minutes to milliseconds

        Criteria criteria = Criteria.where("rsuIP").is(rsuIP)
                .and("timestamp").gte(new Date(start)).lte(new Date(end))
                .andOperator(
                        Criteria.where("uptime").ne(-1),
                        Criteria.where("temperature").ne(-1));

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

        List<RsuState> results = mongoTemplate.aggregate(aggregation, collectionName, RsuState.class)
                .getMappedResults();

        return results;
    }

    @Override
    public RsuState findLatestByRsuIP(String rsuIP) {
        Criteria criteria = Criteria.where("rsuIP").is(rsuIP);
        Query query = Query.query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(1); // Limit the results to just one record

        return mongoTemplate.findOne(query, RsuState.class, collectionName);
    }
}