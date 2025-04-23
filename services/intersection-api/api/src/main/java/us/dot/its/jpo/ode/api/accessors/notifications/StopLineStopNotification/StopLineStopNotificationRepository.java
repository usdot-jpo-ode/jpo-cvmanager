package us.dot.its.jpo.ode.api.accessors.notifications.StopLineStopNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLineStopNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLineStopNotificationRepository extends DataLoader<StopLineStopNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLineStopNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLineStopNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}