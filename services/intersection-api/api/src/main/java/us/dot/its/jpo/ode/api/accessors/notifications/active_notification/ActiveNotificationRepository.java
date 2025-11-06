package us.dot.its.jpo.ode.api.accessors.notifications.active_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

public interface ActiveNotificationRepository {
    long count(Integer intersectionID, String notificationType, String key);

    Page<Notification> find(Integer intersectionID, String notificationType, String key,
            Pageable pageable);

    long delete(String key);
}