package us.dot.its.jpo.ode.api.accessors.notifications.StopLinePassageNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLinePassageNotificationRepository extends DataLoader<StopLinePassageNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}