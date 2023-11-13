package us.dot.its.jpo.ode.api.accessors.notifications.StopLinePassageNotification;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;

@Component
public class StopLinePassageNotificationRepositoryImpl implements StopLinePassageNotificationRepository{
    
    @Autowired
    private MongoTemplate mongoTemplate;

    private String collectionName = "CmStopLinePassageNotification";


    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest){
        Query query = new Query();

        if(intersectionID != null){
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        if(startTime == null){
            startTime = Instant.ofEpochMilli(0).toEpochMilli();
        }
        if(endTime == null){
            endTime = Instant.now().toEpochMilli(); 
        }

        if(latest){
            query.with(Sort.by(Sort.Direction.DESC, "notificationGeneratedAt"));
            query.limit(1);
        }

        query.addCriteria(Criteria.where("notificationGeneratedAt").gte(startTime).lte(endTime));
        return query;
    }

    public long getQueryResultCount(Query query){
        return mongoTemplate.count(query, StopLinePassageNotification.class, collectionName);
    }

    public List<StopLinePassageNotification> find(Query query) {
        return mongoTemplate.find(query, StopLinePassageNotification.class, collectionName);
    }

    @Override
    public void add(StopLinePassageNotification item) {
        mongoTemplate.save(item, collectionName);
    }

}