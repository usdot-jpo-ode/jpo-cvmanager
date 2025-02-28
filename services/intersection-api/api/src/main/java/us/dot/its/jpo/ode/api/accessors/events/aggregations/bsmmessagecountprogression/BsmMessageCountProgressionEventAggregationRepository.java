package us.dot.its.jpo.ode.api.accessors.events.aggregations.bsmmessagecountprogression;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface BsmMessageCountProgressionEventAggregationRepository
        extends DataLoader<BsmMessageCountProgressionEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<BsmMessageCountProgressionEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<BsmMessageCountProgressionEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}