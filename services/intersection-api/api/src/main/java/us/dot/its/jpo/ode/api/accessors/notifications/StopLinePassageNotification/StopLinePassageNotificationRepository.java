package us.dot.its.jpo.ode.api.accessors.notifications.StopLinePassageNotification;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLinePassageNotificationRepository extends DataLoader<StopLinePassageNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<StopLinePassageNotification> find(Query query);  
}