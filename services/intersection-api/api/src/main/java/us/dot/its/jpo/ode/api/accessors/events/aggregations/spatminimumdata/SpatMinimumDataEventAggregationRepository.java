package us.dot.its.jpo.ode.api.accessors.events.aggregations.spatminimumdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatMinimumDataEventAggregationRepository extends DataLoader<SpatMinimumDataEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SpatMinimumDataEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMinimumDataEventAggregation> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}