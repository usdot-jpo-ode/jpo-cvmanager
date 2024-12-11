package us.dot.its.jpo.ode.api.accessors.notifications.SignalStateConflictNotification;


import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalStateConflictNotificationRepository extends DataLoader<SignalStateConflictNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<SignalStateConflictNotification> find(Query query);  
}