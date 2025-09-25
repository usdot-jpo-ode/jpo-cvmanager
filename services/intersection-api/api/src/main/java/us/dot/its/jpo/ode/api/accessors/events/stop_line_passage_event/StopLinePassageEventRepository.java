
package us.dot.its.jpo.ode.api.accessors.events.stop_line_passage_event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface StopLinePassageEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailyStopLinePassageEventCounts(int intersectionID, Long startTime, Long endTime);
}
