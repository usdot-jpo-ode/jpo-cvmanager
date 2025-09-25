package us.dot.its.jpo.ode.api.accessors.notifications.signal_state_conflict_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;

public interface SignalStateConflictNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalStateConflictNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalStateConflictNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}