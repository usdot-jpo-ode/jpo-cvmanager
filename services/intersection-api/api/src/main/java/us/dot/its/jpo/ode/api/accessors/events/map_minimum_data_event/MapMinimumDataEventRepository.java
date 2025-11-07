package us.dot.its.jpo.ode.api.accessors.events.map_minimum_data_event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface MapMinimumDataEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMinimumDataEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMinimumDataEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailyMapMinimumDataEventCounts(int intersectionID, Long startTime, Long endTime);
}