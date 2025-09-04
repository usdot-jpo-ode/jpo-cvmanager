package us.dot.its.jpo.ode.api.accessors.bsm;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProcessedBsmRepositoryImpl implements ProcessedBsmRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "ProcessedBsm";
    private final String DATE_FIELD = "utcTimeStamp";
    private final String INTERSECTION_ID_FIELD = "intersectionId";
    private final String RECORD_GENERATED_AT_FIELD = "recordGeneratedAt";
    private final String VALIDATION_MESSAGES_FIELD = "properties.validationMessages";

    TypeReference<ProcessedBsm<Point>> processedBsmTypeReference = new TypeReference<>() {
    };

    private ObjectMapper mapper = DateJsonMapper.getInstance()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ProcessedBsmRepositoryImpl(MongoTemplate mongoTemplate) {
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
    public Page<ProcessedBsm<Point>> findLatest(
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
        ProcessedBsm<Point> message = mapper.convertValue(document, processedBsmTypeReference);
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
    public Page<ProcessedBsm<Point>> find(
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
        List<ProcessedBsm<Point>> processedBsms = hashMap.getContent().stream()
                .map(document -> mapper.convertValue(document, processedBsmTypeReference)).toList();
        return new PageImpl<>(processedBsms, pageable, hashMap.getTotalElements());
    }
}
