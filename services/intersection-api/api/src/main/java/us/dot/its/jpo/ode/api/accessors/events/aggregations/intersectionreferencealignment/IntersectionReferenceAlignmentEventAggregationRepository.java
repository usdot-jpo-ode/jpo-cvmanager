package us.dot.its.jpo.ode.api.accessors.events.aggregations.intersectionreferencealignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEventAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface IntersectionReferenceAlignmentEventAggregationRepository
        extends DataLoader<IntersectionReferenceAlignmentEventAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<IntersectionReferenceAlignmentEventAggregation> findLatest(Integer intersectionID, Long startTime,
            Long endTime);

    Page<IntersectionReferenceAlignmentEventAggregation> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}