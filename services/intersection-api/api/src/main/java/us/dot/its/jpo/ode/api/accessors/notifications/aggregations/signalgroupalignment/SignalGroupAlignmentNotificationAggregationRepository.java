package us.dot.its.jpo.ode.api.accessors.notifications.aggregations.signalgroupalignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotificationAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalGroupAlignmentNotificationAggregationRepository
        extends DataLoader<SignalGroupAlignmentNotificationAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SignalGroupAlignmentNotificationAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentNotificationAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}