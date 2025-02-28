package us.dot.its.jpo.ode.api.accessors.events.aggregations.mapmessagecountprogression;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface MapMessageCountProgressionEventAggregationRepository
        extends DataLoader<MapMessageCountProgressionEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<MapMessageCountProgressionEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMessageCountProgressionEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}