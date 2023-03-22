package us.dot.its.jpo.ode.api.accessors.notifications.SignalStateConflictNotification;


import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;

public interface SignalStateConflictNotificationRepository{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);
    
    List<SignalStateConflictNotification> find(Query query);  
}