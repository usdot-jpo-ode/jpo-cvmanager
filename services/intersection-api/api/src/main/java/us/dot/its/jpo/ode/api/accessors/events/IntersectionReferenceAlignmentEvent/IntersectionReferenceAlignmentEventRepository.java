
package us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface IntersectionReferenceAlignmentEventRepository extends DataLoader<IntersectionReferenceAlignmentEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<IntersectionReferenceAlignmentEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<IntersectionReferenceAlignmentEvent> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);

    List<IDCount> getAggregatedDailyIntersectionReferenceAlignmentEventCounts(int intersectionID, Long startTime,
            Long endTime);
}
