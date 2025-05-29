package us.dot.its.jpo.ode.api.accessors.notifications.MapBroadcastRateNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface MapBroadcastRateNotificationRepository extends DataLoader<MapBroadcastRateNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}