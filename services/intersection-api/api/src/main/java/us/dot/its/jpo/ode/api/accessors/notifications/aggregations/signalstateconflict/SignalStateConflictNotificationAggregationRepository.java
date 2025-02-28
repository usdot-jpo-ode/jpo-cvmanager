package us.dot.its.jpo.ode.api.accessors.notifications.aggregations.signalstateconflict;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotificationAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalStateConflictNotificationAggregationRepository
        extends DataLoader<SignalStateConflictNotificationAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SignalStateConflictNotificationAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalStateConflictNotificationAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}