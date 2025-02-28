package us.dot.its.jpo.ode.api.accessors.events.aggregations.eventstateprogressionevent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.EventStateProgressionEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface EventStateProgressionEventAggregationRepository
        extends DataLoader<EventStateProgressionEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<EventStateProgressionEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<EventStateProgressionEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}