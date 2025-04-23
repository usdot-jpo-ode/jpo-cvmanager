package us.dot.its.jpo.ode.api.accessors.spat;

import java.util.List;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;

@Component
public class ProcessedSpatRepositoryImpl implements ProcessedSpatRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "ProcessedSpat";
    private final String DATE_FIELD = "utcTimeStamp";
    private final String INTERSECTION_ID_FIELD = "intersectionId";
    private final String RECORD_GENERATED_AT_FIELD = "recordGeneratedAt";
    private final String VALIDATION_MESSAGES_FIELD = "properties.validationMessages";

    public ProcessedSpatRepositoryImpl(MongoTemplate mongoTemplate) {
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
     * @param pageable       the pageable object to use for pagination
     * @return the paginated data that matches the given criteria
     */
    public long count(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            @Nullable Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
        Query query = Query.query(criteria);
        if (pageable != null) {
            query = query.with(pageable);
        }
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
    public Page<ProcessedSpat> findLatest(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            boolean compact) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
        Query query = Query.query(criteria);
        List<String> excludedFields = List.of(RECORD_GENERATED_AT_FIELD);
        if (compact) {
            excludedFields.add(VALIDATION_MESSAGES_FIELD);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return wrapSingleResultWithPage(
                mongoTemplate.findOne(
                        query.with(sort),
                        ProcessedSpat.class,
                        collectionName));
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
    public Page<ProcessedSpat> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            boolean compact,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
        List<String> excludedFields = List.of(RECORD_GENERATED_AT_FIELD);
        if (compact) {
            excludedFields.add(VALIDATION_MESSAGES_FIELD);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return findPage(mongoTemplate, collectionName, pageable, criteria, sort, List.of(), ProcessedSpat.class);
    }

    @Override
    public void add(ProcessedSpat item) {
        mongoTemplate.insert(item, collectionName);
    }
}
