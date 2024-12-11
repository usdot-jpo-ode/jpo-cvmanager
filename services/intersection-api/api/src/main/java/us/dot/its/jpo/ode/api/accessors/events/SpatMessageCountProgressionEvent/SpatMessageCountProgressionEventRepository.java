package us.dot.its.jpo.ode.api.accessors.events.SpatMessageCountProgressionEvent;


import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;

public interface SpatMessageCountProgressionEventRepository extends DataLoader<SpatMessageCountProgressionEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<SpatMessageCountProgressionEvent> find(Query query);

    List<IDCount> getSpatBroadcastRateEventsByDay(int intersectionID, Long startTime, Long endTime);
}