package us.dot.its.jpo.ode.api.accessors.events.aggregations.spattimechangedetails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatTimeChangeDetailsEventAggregationRepository extends DataLoader<TimeChangeDetailsEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<TimeChangeDetailsEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<TimeChangeDetailsEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}