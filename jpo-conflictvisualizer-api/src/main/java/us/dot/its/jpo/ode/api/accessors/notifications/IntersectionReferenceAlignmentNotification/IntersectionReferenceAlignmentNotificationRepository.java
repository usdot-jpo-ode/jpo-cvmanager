package us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface IntersectionReferenceAlignmentNotificationRepository extends DataLoader<IntersectionReferenceAlignmentNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<IntersectionReferenceAlignmentNotification> find(Query query);  
}