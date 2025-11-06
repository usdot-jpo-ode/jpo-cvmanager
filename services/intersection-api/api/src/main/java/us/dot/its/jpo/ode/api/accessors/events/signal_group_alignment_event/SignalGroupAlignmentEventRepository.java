
package us.dot.its.jpo.ode.api.accessors.events.signal_group_alignment_event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface SignalGroupAlignmentEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailySignalGroupAlignmentEventCounts(int intersectionID, Long startTime, Long endTime);

}
