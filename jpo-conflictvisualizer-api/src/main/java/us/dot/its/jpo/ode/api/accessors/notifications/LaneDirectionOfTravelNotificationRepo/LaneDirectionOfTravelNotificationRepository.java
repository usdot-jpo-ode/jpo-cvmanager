package us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;

public interface LaneDirectionOfTravelNotificationRepository{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);
    
    List<LaneDirectionOfTravelNotification> find(Query query);  
}