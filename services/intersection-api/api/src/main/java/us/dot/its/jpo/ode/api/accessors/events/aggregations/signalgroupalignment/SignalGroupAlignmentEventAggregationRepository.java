package us.dot.its.jpo.ode.api.accessors.events.aggregations.signalgroupalignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalGroupAlignmentEventAggregationRepository
        extends DataLoader<SignalGroupAlignmentEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SignalGroupAlignmentEventAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}