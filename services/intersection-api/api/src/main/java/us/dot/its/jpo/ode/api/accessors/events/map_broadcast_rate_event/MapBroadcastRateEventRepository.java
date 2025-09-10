package us.dot.its.jpo.ode.api.accessors.events.map_broadcast_rate_event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface MapBroadcastRateEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailyMapBroadcastRateEventCounts(int intersectionID, Long startTime, Long endTime);
}