package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ActiveNotificationRepository extends DataLoader<Notification> {
    long count(Integer intersectionID, String notificationType, String key, Pageable pageable);

    Page<Notification> find(Integer intersectionID, String notificationType, String key,
            Pageable pageable);

    long delete(Integer intersectionID, String notificationType, String key);
}