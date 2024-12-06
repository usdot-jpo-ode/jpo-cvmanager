package us.dot.its.jpo.ode.api.accessors.events.MapMessageCountProgressionEventRepository;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;

public interface MapMessageCountProgressionEventRepository extends DataLoader<MapMessageCountProgressionEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<MapMessageCountProgressionEvent> find(Query query);

    List<IDCount> getMapBroadcastRateEventsByDay(int intersectionID, Long startTime, Long endTime);
}