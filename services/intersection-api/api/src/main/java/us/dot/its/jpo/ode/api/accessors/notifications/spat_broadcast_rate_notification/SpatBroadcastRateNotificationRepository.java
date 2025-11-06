package us.dot.its.jpo.ode.api.accessors.notifications.spat_broadcast_rate_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;

public interface SpatBroadcastRateNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatBroadcastRateNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatBroadcastRateNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}