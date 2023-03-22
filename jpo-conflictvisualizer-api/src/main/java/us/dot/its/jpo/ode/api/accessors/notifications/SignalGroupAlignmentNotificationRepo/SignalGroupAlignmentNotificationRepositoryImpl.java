package us.dot.its.jpo.ode.api.accessors.notifications.SignalGroupAlignmentNotificationRepo;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;

@Component
public class SignalGroupAlignmentNotificationRepositoryImpl implements SignalGroupAlignmentNotificationRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("event.intersectionID").is(intersectionID));
        }

        if (startTime == null) {
            startTime = Instant.ofEpochMilli(0).toEpochMilli();
        }
        if (endTime == null) {
            endTime = Instant.now().toEpochMilli();
        }

        query.addCriteria(Criteria.where("notificationGeneratedAt").gte(startTime).lte(endTime));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "notificationGeneratedAt"));
            query.limit(1);
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, SignalGroupAlignmentNotification.class);
    }

    public List<SignalGroupAlignmentNotification> find(Query query) {
        return mongoTemplate.find(query, SignalGroupAlignmentNotification.class);
    }

}