package us.dot.its.jpo.ode.api.accessors.notifications.aggregations.intersectionreferencealignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotificationAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface IntersectionReferenceAlignmentNotificationAggregationRepository
        extends DataLoader<IntersectionReferenceAlignmentNotificationAggregation> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<IntersectionReferenceAlignmentNotificationAggregation> findLatest(Integer intersectionID,
            Long startTime, Long endTime);

    Page<IntersectionReferenceAlignmentNotificationAggregation> find(Integer intersectionID,
            Long startTime, Long endTime, Pageable pageable);
}