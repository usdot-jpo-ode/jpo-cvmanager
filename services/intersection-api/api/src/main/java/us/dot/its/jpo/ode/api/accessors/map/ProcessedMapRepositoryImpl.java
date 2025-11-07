package us.dot.its.jpo.ode.api.accessors.map;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.locationtech.jts.geom.CoordinateXY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapBoundingBox;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapIndex;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;

@Component
public class ProcessedMapRepositoryImpl implements ProcessedMapRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "ProcessedMap";
    private final String DATE_FIELD = "properties.timeStamp";
    private final String INTERSECTION_ID_FIELD = "properties.intersectionId";
    private final String RECORD_GENERATED_AT_FIELD = "recordGeneratedAt";
    private final String VALIDATION_MESSAGES_FIELD = "properties.validationMessages";

    TypeReference<ProcessedMap<LineString>> processedMapTypeReference = new TypeReference<>() {
    };
    private ObjectMapper mapper = DateJsonMapper.getInstance()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    public ProcessedMapRepositoryImpl(MongoTemplate mongoTemplate) {
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
     * @return the paginated data that matches the given criteria
     */
    public long count(
            Integer intersectionID,
            Long startTime,
            Long endTime) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
        Query query = Query.query(criteria);
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
    public Page<ProcessedMap<LineString>> findLatest(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            boolean compact) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
        Query query = Query.query(criteria);
        List<String> excludedFields = new ArrayList<>();
        excludedFields.add(RECORD_GENERATED_AT_FIELD);
        if (compact) {
            excludedFields.add(VALIDATION_MESSAGES_FIELD);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        Document document = mongoTemplate.findOne(
                query.with(sort),
                Document.class,
                collectionName);
        ProcessedMap<LineString> message = mapper.convertValue(document, processedMapTypeReference);
        return wrapSingleResultWithPage(message);
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
    public Page<ProcessedMap<LineString>> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            boolean compact,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
        List<String> excludedFields = new ArrayList<>();
        excludedFields.add(RECORD_GENERATED_AT_FIELD);
        if (compact) {
            excludedFields.add(VALIDATION_MESSAGES_FIELD);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        Page<Document> hashMap = findDocumentsWithPagination(mongoTemplate, collectionName, pageable,
                criteria, sort, excludedFields);
        List<ProcessedMap<LineString>> processedMaps = hashMap.getContent().stream()
                .map(document -> mapper.convertValue(document, processedMapTypeReference)).toList();
        return new PageImpl<>(processedMaps, pageable, hashMap.getTotalElements());
    }

    public List<IntersectionReferenceData> getIntersectionIDs() {
        // Define the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                // Stage 1: Group by intersectionId and get the latest document for each
                // intersectionId
                Aggregation.group(INTERSECTION_ID_FIELD)
                        .first("$$ROOT").as("latestDocument"),

                // Stage 2: Project the required fields
                Aggregation.project()
                        .and("latestDocument." + INTERSECTION_ID_FIELD).as("intersectionID")
                        .and("latestDocument.properties.originIp").as("rsuIP")
                        .and("latestDocument.properties.refPoint.latitude").as("latitude")
                        .and("latestDocument.properties.refPoint.longitude").as("longitude")
                        .and("latestDocument.properties.intersectionName").as("intersectionName"));

        // Execute the aggregation query
        AggregationResults<IntersectionReferenceData> results = mongoTemplate.aggregate(aggregation, collectionName,
                IntersectionReferenceData.class);

        return results.getMappedResults();
    }

    /**
     * @deprecated This method is deprecated because it is not used within the
     *             CVManager, only used externally by a signal-head monitoring
     *             mobile application. This method is replaced by reference points
     *             and bounding boxes present in the IntersectionReferenceData type.
     *             Use {@link #getIntersectionIDs()} instead.
     * @see #getIntersectionIDs()
     */
    @Deprecated
    public List<IntersectionReferenceData> getIntersectionsContainingPoint(double longitude, double latitude) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        DistinctIterable<Integer> docs = collection.distinct(INTERSECTION_ID_FIELD, Integer.class);
        MapIndex index;
        Map<Integer, ProcessedMap<LineString>> mapLookup;
        try (MongoCursor<Integer> results = docs.iterator()) {
            index = new MapIndex();
            mapLookup = new HashMap<>();
            while (results.hasNext()) {
                Integer intersectionId = results.next();
                List<ProcessedMap<LineString>> maps = findLatest(intersectionId, null, null, true)
                        .getContent();
                if (!maps.isEmpty()) {
                    MapBoundingBox box = new MapBoundingBox(maps.getFirst());
                    index.insert(box);
                    mapLookup.put(intersectionId, maps.getFirst());
                }
            }
        }

        List<MapBoundingBox> mapsContainingPoints = index.mapsContainingPoint(new CoordinateXY(longitude, latitude));

        List<IntersectionReferenceData> result = new ArrayList<>();
        for (MapBoundingBox box : mapsContainingPoints) {
            ProcessedMap<LineString> map = mapLookup.get(box.getIntersectionId());
            IntersectionReferenceData data = new IntersectionReferenceData();
            data.setIntersectionID(map.getProperties().getIntersectionId());
            data.setRsuIP(map.getProperties().getOriginIp());

            if (map.getProperties().getIntersectionName() != null
                    && !map.getProperties().getIntersectionName().isEmpty()) {
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
                Aggregation.match(Criteria.where(INTERSECTION_ID_FIELD).is(intersectionID)),
                Aggregation.match(Criteria.where(DATE_FIELD).gte(startTimeString).lte(endTimeString)),
                Aggregation.project(DATE_FIELD),
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
                Aggregation.match(Criteria.where(INTERSECTION_ID_FIELD).is(intersectionID)),
                Aggregation.match(Criteria.where(DATE_FIELD).gte(startTimeString).lte(endTimeString)),
                Aggregation.project(DATE_FIELD),

                // Convert string timestamp to date type
                Aggregation.project()
                        .and(DateOperators.DateFromString.fromStringOf("timeStamp")).as("date"),

                // Convert date to milliseconds since epoch
                Aggregation.project()
                        .and(ConvertOperators.ToLong.toLong("$date")).as("utcmillisecond"),

                // Convert milliseconds to integer deciseconds since epoch
                Aggregation.project()
                        .and(ArithmeticOperators.Divide.valueOf("utcmillisecond").divideBy(10 * 1000)).as("decisecond"),
                Aggregation.project()
                        .and(ArithmeticOperators.Round.roundValueOf("decisecond")).as("decisecond"),

                // Aggregate message counts per unique decisecond and count number in each
                // bucket from 0-20 per decisecond
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
}