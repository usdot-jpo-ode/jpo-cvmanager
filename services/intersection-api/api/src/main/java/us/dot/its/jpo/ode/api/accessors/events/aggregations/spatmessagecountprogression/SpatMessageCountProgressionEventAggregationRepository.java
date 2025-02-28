package us.dot.its.jpo.ode.api.accessors.events.aggregations.spatmessagecountprogression;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatMessageCountProgressionEventAggregationRepository
        extends DataLoader<SpatMessageCountProgressionEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SpatMessageCountProgressionEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMessageCountProgressionEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}