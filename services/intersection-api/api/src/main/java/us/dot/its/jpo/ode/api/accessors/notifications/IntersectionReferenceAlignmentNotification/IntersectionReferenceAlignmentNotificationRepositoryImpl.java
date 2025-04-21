package us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;

@Component
public class IntersectionReferenceAlignmentNotificationRepositoryImpl
        implements IntersectionReferenceAlignmentNotificationRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmIntersectionReferenceAlignmentNotification";
    private final String DATE_FIELD = "notificationGeneratedAt";
    private final String INTERSECTION_ID_FIELD = "intersectionID";

    @Autowired
    public IntersectionReferenceAlignmentNotificationRepositoryImpl(MongoTemplate mongoTemplate) {
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
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
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
    public Page<IntersectionReferenceAlignmentNotification> findLatest(
            Integer intersectionID,
            Long startTime,
            Long endTime) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Query query = Query.query(criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return wrapSingleResultWithPage(
                mongoTemplate.findOne(
                        query.with(sort),
                        IntersectionReferenceAlignmentNotification.class,
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
    public Page<IntersectionReferenceAlignmentNotification> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, false);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return findPage(mongoTemplate, collectionName, pageable, criteria, sort);
    }

    @Override
    public void add(IntersectionReferenceAlignmentNotification item) {
        mongoTemplate.insert(item, collectionName);
    }

}