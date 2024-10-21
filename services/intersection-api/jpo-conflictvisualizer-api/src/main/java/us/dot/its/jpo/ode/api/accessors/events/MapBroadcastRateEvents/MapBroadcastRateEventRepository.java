package us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface MapBroadcastRateEventRepository extends DataLoader<MapBroadcastRateEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<MapBroadcastRateEvent> find(Query query);

    List<IDCount> getMapBroadcastRateEventsByDay(int intersectionID, Long startTime, Long endTime);
}