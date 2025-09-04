package us.dot.its.jpo.ode.api.accessors.notifications.signal_group_alignment_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;

public interface SignalGroupAlignmentNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentNotification> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}