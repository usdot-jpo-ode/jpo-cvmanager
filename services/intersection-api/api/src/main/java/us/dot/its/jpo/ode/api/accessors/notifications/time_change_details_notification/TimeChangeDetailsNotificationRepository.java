package us.dot.its.jpo.ode.api.accessors.notifications.time_change_details_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;

public interface TimeChangeDetailsNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<TimeChangeDetailsNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<TimeChangeDetailsNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}