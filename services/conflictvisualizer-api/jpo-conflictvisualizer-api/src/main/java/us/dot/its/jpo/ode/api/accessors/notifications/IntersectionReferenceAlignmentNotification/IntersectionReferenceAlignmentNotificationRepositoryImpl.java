package us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;

@Component
public class IntersectionReferenceAlignmentNotificationRepositoryImpl implements IntersectionReferenceAlignmentNotificationRepository{
    
    @Autowired
    private MongoTemplate mongoTemplate;

    private String collectionName = "CmIntersectionReferenceAlignmentNotification";

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest){
        Query query = new Query();

        if(intersectionID != null){
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

        if(latest){
            query.with(Sort.by(Sort.Direction.DESC, "notificationGeneratedAt"));
            query.limit(1);
        }
        return query;
    }

    public long getQueryResultCount(Query query){
        return mongoTemplate.count(query, IntersectionReferenceAlignmentNotification.class, collectionName);
    }

    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, IntersectionReferenceAlignmentNotification.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<IntersectionReferenceAlignmentNotification> find(Query query) {
        return mongoTemplate.find(query, IntersectionReferenceAlignmentNotification.class, collectionName);
    }
    
    @Override
    public void add(IntersectionReferenceAlignmentNotification item) {
        mongoTemplate.save(item, collectionName);
    }

}