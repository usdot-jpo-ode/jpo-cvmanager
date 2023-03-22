package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.domain.Sort;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.deserialization.NotificationDeserializer;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;

@Component
public class ActiveNotificationsRepositoryImpl implements ActiveNotificationRepository{
    
    @Autowired
    private MongoTemplate mongoTemplate;

    private ObjectMapper mapper = DateJsonMapper.getInstance();
    private NotificationDeserializer deserializer = new NotificationDeserializer();

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

        List<Map> documents = mongoTemplate.find(query, Map.class, "CmNotification");
        List<Notification> notificationList = new ArrayList<>();
        for(Map document : documents){
            document.remove("_id");
            JsonNode node = mapper.convertValue(document, JsonNode.class);
            Notification notification = deserializer.deserializeNotification(node);
            notificationList.add(notification);
        }
        return notificationList;
    }

    public long delete(Query query){
        long count = getQueryResultCount(query);

        mongoTemplate.findAndRemove(query, Map.class, "CmNotification");
        return count;
    }

}