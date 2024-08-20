package us.dot.its.jpo.ode.api.accessors.map;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.locationtech.jts.geom.CoordinateXY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;


import static com.mongodb.client.model.Filters.eq;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapBoundingBox;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapIndex;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;

@Component
public class ProcessedMapRepositoryImpl implements ProcessedMapRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    TypeReference<ProcessedMap<LineString>> processedMapTypeReference = new TypeReference<>(){};

    private String collectionName = "ProcessedMap";
    private ObjectMapper mapper = DateJsonMapper.getInstance();
    private Logger logger = LoggerFactory.getLogger(ProcessedMapRepositoryImpl.class);

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest, boolean compact) {
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
            query.with(Sort.by(Sort.Direction.DESC, "properties.timeStamp"));
            query.limit(1);
        }else{
            query.limit(props.getMaximumResponseSize());
        }

        if (compact){
            query.fields().exclude("recordGeneratedAt", "properties.validationMessages");
        }else{
            query.fields().exclude("recordGeneratedAt");
        }

        query.addCriteria(Criteria.where("properties.timeStamp").gte(startTimeString).lte(endTimeString));
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, ProcessedMap.class, collectionName);
    }

    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, ProcessedMap.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<ProcessedMap<LineString>> findProcessedMaps(Query query) {
        List<Map> documents = mongoTemplate.find(query, Map.class, collectionName);
        List<ProcessedMap<LineString>> convertedList = new ArrayList<>();
        for (Map document : documents) {
            document.remove("_id");
            ProcessedMap<LineString> bsm = mapper.convertValue(document, processedMapTypeReference);
            convertedList.add(bsm);
        }
        return convertedList;
    }

    public List<IntersectionReferenceData> getIntersectionIDs() {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        DistinctIterable<Integer> docs = collection.distinct("properties.intersectionId", Integer.class);
        MongoCursor<Integer> results = docs.iterator();
        List<IntersectionReferenceData> referenceDataList = new ArrayList<>();
        while (results.hasNext()) {
            
            Integer intersectionId = results.next();
            if (intersectionId != null){
                
                Bson projectionFields = Projections.fields(
                        Projections.include("properties.intersectionId", "properties.originIp",
                                "properties.refPoint.latitude", "properties.refPoint.longitude", "properties.intersectionName"),
                        Projections.excludeId());
                try {
                    Document document = collection.find(eq("properties.intersectionId", intersectionId))
                        .projection(projectionFields).sort(Sorts.descending("properties.timeStamp")).maxTime(props.getMongoTimeoutMs(), TimeUnit.MILLISECONDS).first();
                
                    if(document != null){
                        IntersectionReferenceData data = new IntersectionReferenceData();
                        Document properties = document.get("properties", Document.class);

                        if (properties != null) {
                            Document refPoint = properties.get("refPoint", Document.class);
                            data.setIntersectionID(intersectionId);
                            data.setRoadRegulatorID("-1");
                            data.setRsuIP(properties.getString("originIp"));

                            if(properties.getString("intersectionName") != null && properties.getString("intersectionName").isEmpty()){
                                data.setIntersectionName(properties.getString("intersectionName"));
                            }
                            
                            if (refPoint != null) {
                                data.setLatitude(refPoint.getDouble("latitude"));
                                data.setLongitude(refPoint.getDouble("longitude"));
                            }
                        }
                        referenceDataList.add(data);
                    }
                } catch (MongoException e){
                    logger.error("MongoDB Intersection Query Did not finish in allowed time window");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                
            }
        }

        return referenceDataList;
    }

    public List<IntersectionReferenceData> getIntersectionsContainingPoint(double longitude, double latitude){
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        DistinctIterable<Integer> docs = collection.distinct("properties.intersectionId", Integer.class);
        MongoCursor<Integer> results = docs.iterator();
        MapIndex index = new MapIndex();
        Map<Integer, ProcessedMap<LineString>> mapLookup = new HashMap<>();
        while (results.hasNext()) {
            Integer intersectionId = results.next();
            if (intersectionId != null){

                
                Query query = getQuery(intersectionId,  null,  null,  true,  true);

                List<ProcessedMap<LineString>> maps = findProcessedMaps(query);

                if(maps.size() > 0){
                    MapBoundingBox box = new MapBoundingBox(maps.getFirst());
                    index.insert(box);
                    mapLookup.put(intersectionId, maps.getFirst());
                }
            }
        }

        List<MapBoundingBox> mapsContainingPoints = index.mapsContainingPoint(new CoordinateXY(longitude, latitude));
        
        List<IntersectionReferenceData> result = new ArrayList<>();
        for(MapBoundingBox box: mapsContainingPoints){
            ProcessedMap<LineString> map = mapLookup.get(box.getIntersectionId());
            IntersectionReferenceData data = new IntersectionReferenceData();
            data.setIntersectionID(map.getProperties().getIntersectionId());
            data.setRoadRegulatorID("-1");
            data.setRsuIP(map.getProperties().getOriginIp());

            if(map.getProperties().getIntersectionName() != null && map.getProperties().getIntersectionName().isEmpty()){
                data.setIntersectionName(map.getProperties().getIntersectionName());
            }
            
            if (map.getProperties().getRefPoint() != null) {
                data.setLatitude(map.getProperties().getRefPoint().getLatitude().doubleValue());
                data.setLongitude(map.getProperties().getRefPoint().getLongitude().doubleValue());
            }
            result.add(data);
        }

        return result;

    }

    public List<IDCount> getMapBroadcastRates(int intersectionID, Long startTime, Long endTime) {

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
                Aggregation.match(Criteria.where("properties.intersectionId").is(intersectionID)),
                Aggregation.match(Criteria.where("properties.timeStamp").gte(startTimeString).lte(endTimeString)),
                Aggregation.project("properties.timeStamp"),
                Aggregation.project()
                        .and(DateOperators.DateFromString.fromStringOf("timeStamp")).as("date"),
                Aggregation.project()
                        .and(DateOperators.DateToString.dateOf("date").toString("%Y-%m-%d-%H")).as("dateStr"),
                Aggregation.group("dateStr").count().as("count"),
                Aggregation.sort(Sort.Direction.ASC, "_id")).withOptions(options);

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        for (IDCount r : results) {
            r.setCount((double) r.getCount() / 3600.0);
        }

        return results;
    }

    public List<IDCount> getMapBroadcastRateDistribution(int intersectionID, Long startTime, Long endTime) {

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
                Aggregation.match(Criteria.where("properties.intersectionId").is(intersectionID)),
                Aggregation.match(Criteria.where("properties.timeStamp").gte(startTimeString).lte(endTimeString)),
                Aggregation.project("properties.timeStamp"),
                Aggregation.project()
                        .and(DateOperators.DateFromString.fromStringOf("timeStamp")).as("date"),
                Aggregation.project()
                        .and(ConvertOperators.ToLong.toLong("$date")).as("utcmillisecond"),
                Aggregation.project()
                        .and(ArithmeticOperators.Divide.valueOf("utcmillisecond").divideBy(10 * 1000)).as("decisecond"),
                Aggregation.project()
                        .and(ArithmeticOperators.Round.roundValueOf("decisecond")).as("decisecond"),
                Aggregation.group("decisecond").count().as("msgPerDecisecond"),
                Aggregation.bucket("msgPerDecisecond")
                        .withBoundaries(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
                        .withDefaultBucket(20)
                        .andOutputCount().as("count"))
                .withOptions(options);

        AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName, IDCount.class);
        List<IDCount> results = result.getMappedResults();

        return results;
    }

    

    @Override
    public void add(ProcessedMap<LineString> item) {
        mongoTemplate.save(item, collectionName);
    }

}