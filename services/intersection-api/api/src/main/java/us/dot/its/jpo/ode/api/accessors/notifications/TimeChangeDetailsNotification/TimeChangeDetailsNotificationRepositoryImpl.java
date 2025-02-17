package us.dot.its.jpo.ode.api.accessors.notifications.TimeChangeDetailsNotification;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@Component
public class TimeChangeDetailsNotificationRepositoryImpl implements TimeChangeDetailsNotificationRepository {

    private final MongoTemplate mongoTemplate;
    private final ConflictMonitorApiProperties props;

    private final String collectionName = "CmTimeChangeDetailsNotification";

    @Autowired
    public TimeChangeDetailsNotificationRepositoryImpl(MongoTemplate mongoTemplate,
            ConflictMonitorApiProperties props) {
        this.mongoTemplate = mongoTemplate;
        this.props = props;
    }

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
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

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, TimeChangeDetailsNotification.class, collectionName);
    }

    public long getQueryFullCount(Query query) {
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, TimeChangeDetailsNotification.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<TimeChangeDetailsNotification> find(Query query) {
        return mongoTemplate.find(query, TimeChangeDetailsNotification.class, collectionName);
    }

    @Override
    public void add(TimeChangeDetailsNotification item) {
        mongoTemplate.insert(item, collectionName);
    }

}