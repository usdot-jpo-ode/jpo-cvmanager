package us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatMinimumDataEventRepository extends DataLoader<SpatMinimumDataEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMinimumDataEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMinimumDataEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailySpatMinimumDataEventCounts(int intersectionID, Long startTime, Long endTime);
}