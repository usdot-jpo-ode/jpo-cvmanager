
package us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalGroupAlignmentEventRepository extends DataLoader<SignalGroupAlignmentEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SignalGroupAlignmentEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SignalGroupAlignmentEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailySignalGroupAlignmentEventCounts(int intersectionID, Long startTime, Long endTime);

}
