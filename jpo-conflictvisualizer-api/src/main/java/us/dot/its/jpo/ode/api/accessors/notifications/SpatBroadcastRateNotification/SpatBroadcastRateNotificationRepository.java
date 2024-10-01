package us.dot.its.jpo.ode.api.accessors.notifications.SpatBroadcastRateNotification;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatBroadcastRateNotificationRepository extends DataLoader<SpatBroadcastRateNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<SpatBroadcastRateNotification> find(Query query);  
}