package us.dot.its.jpo.ode.api.accessors.notifications.StopLineStopNotification;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLineStopNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLineStopNotificationRepository extends DataLoader<StopLineStopNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<StopLineStopNotification> find(Query query);  
}