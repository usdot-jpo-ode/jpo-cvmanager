package us.dot.its.jpo.ode.api.accessors.notifications.stop_line_stop_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLineStopNotification;

public interface StopLineStopNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLineStopNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLineStopNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}