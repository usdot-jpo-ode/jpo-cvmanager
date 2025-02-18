package us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@Component
public class ConnectionOfTravelNotificationRepositoryImpl implements ConnectionOfTravelNotificationRepository {

    private final MongoTemplate mongoTemplate;
    private final ConflictMonitorApiProperties props;

    private final String collectionName = "CmConnectionOfTravelNotification";

    @Autowired
    public ConnectionOfTravelNotificationRepositoryImpl(MongoTemplate mongoTemplate,
            ConflictMonitorApiProperties props) {
        this.mongoTemplate = mongoTemplate;
        this.props = props;
    }

    private Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        Date startTimeDate = new Date(0);
        Date endTimeDate = new Date();

        if (startTime != null) {
            startTimeDate = new Date(startTime);
        }
        if (endTime != null) {
            endTimeDate = new Date(endTime);
        }

        query.addCriteria(Criteria.where("notificationGeneratedAt").gte(startTimeDate).lte(endTimeDate));

        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "notificationGeneratedAt"));
            query.limit(1);
        } else {
            query.limit(props.getMaximumResponseSize());
        }
        return query;
    }

    public long getQueryResultCount(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = getQuery(intersectionID, startTime, endTime, latest);
        return mongoTemplate.count(query, ConnectionOfTravelNotification.class, collectionName);
    }

    public long getQueryFullCount(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = getQuery(intersectionID, startTime, endTime, latest);
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, ConnectionOfTravelNotification.class, collectionName);
        query.limit(limit);
        return count;
    }

    public Page<ConnectionOfTravelNotification> find(Integer intersectionID, Long startTime, Long endTime,
            boolean latest,
            Pageable pageable) {
        Query query = getQuery(intersectionID, startTime, endTime, latest);
        query.with(pageable);
        List<ConnectionOfTravelNotification> notifications = mongoTemplate.find(query,
                ConnectionOfTravelNotification.class, collectionName);
        Long total = (notifications.size() == pageable.getPageSize())
                ? mongoTemplate.count(query, ConnectionOfTravelNotification.class, collectionName)
                : (long) notifications.size() + pageable.getOffset();
        return new PageImpl<>(notifications, pageable, total);
    }

    @Override
    public void add(ConnectionOfTravelNotification item) {
        mongoTemplate.insert(item, collectionName);
    }

}