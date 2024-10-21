package us.dot.its.jpo.ode.api.accessors.events.SpatBroadcastRateEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatBroadcastRateEventRepository extends DataLoader<SpatBroadcastRateEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<SpatBroadcastRateEvent> find(Query query);

    List<IDCount> getSpatBroadcastRateEventsByDay(int intersectionID, Long startTime, Long endTime);
}