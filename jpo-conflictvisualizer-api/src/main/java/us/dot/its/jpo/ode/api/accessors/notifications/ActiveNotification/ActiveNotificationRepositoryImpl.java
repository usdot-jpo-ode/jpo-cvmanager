package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

@Component
public class ActiveNotificationRepositoryImpl implements ActiveNotificationRepository{
    
    @Autowired
    private MongoTemplate mongoTemplate;

    private String collectionName = "CmNotification";

    public Query getQuery(Integer intersectionID, Integer roadRegulatorID, String notificationType, String key){
        Query query = new Query();

        if(intersectionID != null){
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        // if(roadRegulatorID != null){
        //     query.addCriteria(Criteria.where("roadRegulatorID").is(roadRegulatorID));
        // }

        if(notificationType != null){
            query.addCriteria(Criteria.where("notificationType").is(notificationType));
        }

        if(key != null){
            System.out.println(key);
            query.addCriteria(Criteria.where("key").is(key));
        }

        return query;
    }

    public long getQueryResultCount(Query query){
        return mongoTemplate.count(query, Notification.class, "CmNotification");
    }

    public long getQueryFullCount(Query query){
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
            if(type.equals("ConnectionOfTravelNotification")){
                notifications.add(mongoTemplate.getConverter().read(ConnectionOfTravelNotification.class, dbObject));
            }
            else if(type.equals("IntersectionReferenceAlignmentNotification")){
                notifications.add(mongoTemplate.getConverter().read(IntersectionReferenceAlignmentNotification.class, dbObject));
            }
            else if(type.equals("LaneDirectionOfTravelAssessmentNotification")){
                notifications.add(mongoTemplate.getConverter().read(LaneDirectionOfTravelNotification.class, dbObject));
            }
            else if(type.equals("SignalGroupAlignmentNotification")){
                notifications.add(mongoTemplate.getConverter().read(SignalGroupAlignmentNotification.class, dbObject));
            }
            else if(type.equals("SignalStateConflictNotification")){
                notifications.add(mongoTemplate.getConverter().read(SignalStateConflictNotification.class, dbObject));
            }
            else if(type.equals("TimeChangeDetailsNotification")){
                notifications.add(mongoTemplate.getConverter().read(TimeChangeDetailsNotification.class, dbObject));
            }
            else if(type.equals("AppHealthNotification")){
                notifications.add(mongoTemplate.getConverter().read(KafkaStreamsAnomalyNotification.class, dbObject));
            }
        }

        return notifications;


    }

    public long delete(Query query){
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