package us.dot.its.jpo.ode.api.accessors.events.bsm_event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface BsmEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<BsmEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<BsmEvent> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);

    List<IDCount> getAggregatedDailyBsmEventCounts(int intersectionID, Long startTime, Long endTime);
}
