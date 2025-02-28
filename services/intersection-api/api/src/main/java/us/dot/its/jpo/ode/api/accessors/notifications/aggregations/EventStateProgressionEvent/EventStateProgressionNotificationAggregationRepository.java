package us.dot.its.jpo.ode.api.accessors.notifications.aggregations.EventStateProgressionEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.EventStateProgressionNotificationAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface EventStateProgressionNotificationAggregationRepository
        extends DataLoader<EventStateProgressionNotificationAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<EventStateProgressionNotificationAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<EventStateProgressionNotificationAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}