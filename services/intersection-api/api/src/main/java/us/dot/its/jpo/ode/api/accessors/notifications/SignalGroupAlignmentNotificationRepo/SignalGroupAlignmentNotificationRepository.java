package us.dot.its.jpo.ode.api.accessors.notifications.SignalGroupAlignmentNotificationRepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalGroupAlignmentNotificationRepository extends DataLoader<SignalGroupAlignmentNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentNotification> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}