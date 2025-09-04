package us.dot.its.jpo.ode.api.accessors.notifications.stop_line_passage_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;

public interface StopLinePassageNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}