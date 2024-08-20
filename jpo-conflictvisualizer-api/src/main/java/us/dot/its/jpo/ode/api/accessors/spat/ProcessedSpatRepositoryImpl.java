package us.dot.its.jpo.ode.api.accessors.spat;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.IDCount;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

@Component
public class ProcessedSpatRepositoryImpl implements ProcessedSpatRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private final String collectionName = "ProcessedSpat";
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    private ObjectMapper mapper = DateJsonMapper.getInstance();

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest, boolean compact) {
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

        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "utcTimeStamp"));
            query.limit(1);
        }else{
            query.limit(props.getMaximumResponseSize());
        }

        if (compact){
            query.fields().exclude("recordGeneratedAt", "validationMessages");
        }else{
            query.fields().exclude("recordGeneratedAt");
        }

        query.addCriteria(Criteria.where("utcTimeStamp").gte(startTimeString).lte(endTimeString));
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, ProcessedSpat.class, collectionName);
    }

    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, ProcessedSpat.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<ProcessedSpat> findProcessedSpats(Query query) {
        return mongoTemplate.find(query, ProcessedSpat.class, collectionName);
    }

    public List<IDCount> getSpatBroadcastRates(int intersectionID, Long startTime, Long endTime){
        Query query = getQuery(intersectionID, startTime, endTime, false, true);

        query.fields().include("utcTimeStamp");
        List<Map> times = mongoTemplate.find(query, Map.class, collectionName);

        Map<String, IDCount> results = new HashMap<>();

        for(Map doc: times){
            ZonedDateTime time = mapper.convertValue(doc.get("utcTimeStamp"), ZonedDateTime.class);
            String key = time.format(formatter);

            if(results.containsKey(key)){
                IDCount count = results.get(key);
                count.setCount(count.getCount() +1);
            }
            else{
                IDCount count = new IDCount();
                count.setId(key);
                count.setCount(1);
                results.put(key, count);
            }
        }

        List<IDCount> outputCounts = new ArrayList<>(results.values());
        for (IDCount r : outputCounts) {
            r.setCount((double) r.getCount() / 3600.0);
        }
        return outputCounts;
  

    }

    public List<IDCount> getSpatBroadcastRateDistribution(int intersectionID, Long startTime, Long endTime){

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }

        AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("intersectionId").is(intersectionID)),
            Aggregation.match(Criteria.where("utcTimeStamp").gte(startTimeString).lte(endTimeString)),
            Aggregation.project("utcTimeStamp"),
            Aggregation.project()
                .and(DateOperators.DateFromString.fromStringOf("utcTimeStamp")).as("date"),
            Aggregation.project()
                .and(ConvertOperators.ToLong.toLong("$date")).as("utcmillisecond"),
            Aggregation.project()
                .and(ArithmeticOperators.Divide.valueOf("utcmillisecond").divideBy(10* 1000)).as("decisecond"),
            Aggregation.project()
                .and(ArithmeticOperators.Round.roundValueOf("decisecond")).as("decisecond"),
            Aggregation.group("decisecond").count().as("msgPerDecisecond"),
            Aggregation.bucket("msgPerDecisecond")
                .withBoundaries(0,10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200)
                .withDefaultBucket(200)
                .andOutputCount().as("count")
        ).withOptions(options);

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();  

        return results;
    }

    @Override
    public void add(ProcessedSpat item) {
        mongoTemplate.save(item, collectionName);
    }
}
