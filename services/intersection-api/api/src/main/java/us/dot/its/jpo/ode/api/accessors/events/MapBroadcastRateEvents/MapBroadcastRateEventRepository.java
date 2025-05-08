package us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface MapBroadcastRateEventRepository extends DataLoader<MapBroadcastRateEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapBroadcastRateEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailyMapBroadcastRateEventCounts(int intersectionID, Long startTime, Long endTime);
}