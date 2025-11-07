package us.dot.its.jpo.ode.api.accessors.notifications.map_broadcast_rate_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;

public interface MapBroadcastRateNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}