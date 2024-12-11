package us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface LaneDirectionOfTravelNotificationRepository extends DataLoader<LaneDirectionOfTravelNotification>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<LaneDirectionOfTravelNotification> find(Query query);  
}