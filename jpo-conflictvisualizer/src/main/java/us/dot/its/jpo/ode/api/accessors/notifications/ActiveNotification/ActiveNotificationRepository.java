package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;

import java.util.List;
import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

public interface ActiveNotificationRepository{
    Query getQuery(Integer intersectionID, Integer roadRegulatorID, String notificationType, String key);

    long getQueryResultCount(Query query);
    
    List<Notification> find(Query query);

    long delete(Query query);
}