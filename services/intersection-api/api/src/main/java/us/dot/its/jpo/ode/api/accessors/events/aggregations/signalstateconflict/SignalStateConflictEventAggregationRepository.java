package us.dot.its.jpo.ode.api.accessors.events.aggregations.signalstateconflict;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalStateConflictEventAggregationRepository extends DataLoader<SignalStateConflictEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SignalStateConflictEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalStateConflictEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}