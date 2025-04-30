package us.dot.its.jpo.ode.api.accessors.notifications.SpatBroadcastRateNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatBroadcastRateNotificationRepository extends DataLoader<SpatBroadcastRateNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatBroadcastRateNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatBroadcastRateNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}