package us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageWrapper;
import us.dot.its.jpo.ode.api.accessors.PaginatedQueryInterface;

@Component
public class ConnectionOfTravelNotificationRepositoryImpl
        implements ConnectionOfTravelNotificationRepository, PageWrapper, PaginatedQueryInterface {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmConnectionOfTravelNotification";
    private final String DATE_FIELD = "notificationGeneratedAt";
    private final String INTERSECTION_ID_FIELD = "IntersectionID";

    @Autowired
    public ConnectionOfTravelNotificationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long getQueryResultCount(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime)
                .build();
        return mongoTemplate.count(Query
                .query(criteria).with(pageable), collectionName);
    }

    public Page<ConnectionOfTravelNotification> findLatest(
            Integer intersectionID,
            Long startTime,
            Long endTime) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime)
                .build();
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return wrapSingleResultWithPage(mongoTemplate.findOne(
                Query.query(criteria).with(sort),
                ConnectionOfTravelNotification.class,
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
    public Page<ConnectionOfTravelNotification> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime)
                .build();
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        return findPaginatedData(mongoTemplate, collectionName, pageable, criteria, sort);
    }

    public void add(ConnectionOfTravelNotification item) {
        mongoTemplate.insert(item, collectionName);
    }
}