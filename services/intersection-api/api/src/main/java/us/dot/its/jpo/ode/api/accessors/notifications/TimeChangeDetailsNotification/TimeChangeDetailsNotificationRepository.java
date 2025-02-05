package us.dot.its.jpo.ode.api.accessors.notifications.TimeChangeDetailsNotification;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface TimeChangeDetailsNotificationRepository extends DataLoader<TimeChangeDetailsNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<TimeChangeDetailsNotification> find(Query query);  
}