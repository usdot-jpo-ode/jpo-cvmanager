package us.dot.its.jpo.ode.api.accessors.events.aggregations.mapminimumdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface MapMinimumDataEventAggregationRepository extends DataLoader<MapMinimumDataEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<MapMinimumDataEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMinimumDataEventAggregation> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}