package us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ConnectionOfTravelNotificationRepository extends DataLoader<ConnectionOfTravelNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<ConnectionOfTravelNotification> find(Query query);  
}