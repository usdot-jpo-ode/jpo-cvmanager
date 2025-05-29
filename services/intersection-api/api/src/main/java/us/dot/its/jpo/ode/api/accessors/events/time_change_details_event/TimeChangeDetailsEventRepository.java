
package us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface TimeChangeDetailsEventRepository extends DataLoader<TimeChangeDetailsEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<TimeChangeDetailsEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<TimeChangeDetailsEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailyTimeChangeDetailsEventCounts(int intersectionID, Long startTime, Long endTime);
}
