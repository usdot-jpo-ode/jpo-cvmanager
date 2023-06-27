package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

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

        if(roadRegulatorID != null){
            query.addCriteria(Criteria.where("roadRegulatorID").is(roadRegulatorID));
        }

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

    public List<Notification> find(Query query) {
        return mongoTemplate.find(query, Notification.class, collectionName);
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