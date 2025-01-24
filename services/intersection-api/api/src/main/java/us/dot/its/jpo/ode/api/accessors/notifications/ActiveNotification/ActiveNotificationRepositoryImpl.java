package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.app_health.KafkaStreamsAnomalyNotification;

@Slf4j
@Component
public class ActiveNotificationRepositoryImpl implements ActiveNotificationRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final String collectionName = "CmNotification";

    public Query getQuery(Integer intersectionID, Integer roadRegulatorID, String notificationType, String key) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        if (notificationType != null) {
            query.addCriteria(Criteria.where("notificationType").is(notificationType));
        }

        if (key != null) {
            query.addCriteria(Criteria.where("key").is(key));
        }

        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, Notification.class, "CmNotification");
    }

    public long getQueryFullCount(Query query) {
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, Notification.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<Notification> find(Query query) {
        List<Bson> dbObjects = mongoTemplate.find(query, Bson.class, collectionName);

        List<Notification> notifications = new ArrayList<>();
        for (Bson dbObject : dbObjects) {
            String type = dbObject.toBsonDocument().getString("notificationType").getValue();
            switch (type) {
                case "ConnectionOfTravelNotification" ->
                        notifications.add(mongoTemplate.getConverter().read(ConnectionOfTravelNotification.class, dbObject));
                case "IntersectionReferenceAlignmentNotification" -> notifications.add(
                        mongoTemplate.getConverter().read(IntersectionReferenceAlignmentNotification.class, dbObject));
                case "LaneDirectionOfTravelAssessmentNotification" ->
                        notifications.add(mongoTemplate.getConverter().read(LaneDirectionOfTravelNotification.class, dbObject));
                case "SignalGroupAlignmentNotification" ->
                        notifications.add(mongoTemplate.getConverter().read(SignalGroupAlignmentNotification.class, dbObject));
                case "SignalStateConflictNotification" ->
                        notifications.add(mongoTemplate.getConverter().read(SignalStateConflictNotification.class, dbObject));
                case "TimeChangeDetailsNotification" ->
                        notifications.add(mongoTemplate.getConverter().read(TimeChangeDetailsNotification.class, dbObject));
                case "AppHealthNotification" ->
                        notifications.add(mongoTemplate.getConverter().read(KafkaStreamsAnomalyNotification.class, dbObject));
                default ->
                        log.warn("Attempted to find unknown notificationType: {}", type);
            }
        }

        return notifications;

    }

    public long delete(Query query) {
        long count = getQueryResultCount(query);

        mongoTemplate.findAndRemove(query, Map.class, "CmNotification");
        return count;
    }

    @Override
    public void add(Notification item) {
        item.setId(item.getNotificationType() + "_" + item.getIntersectionID() + "_" + item.getRoadRegulatorID());
        mongoTemplate.save(item, collectionName);
    }

}