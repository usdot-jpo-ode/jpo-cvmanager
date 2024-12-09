package us.dot.its.jpo.ode.api.accessors.events.BsmMessageCountProgressionEventRepository;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEvent;

public interface BsmMessageCountProgressionEventRepository extends DataLoader<BsmMessageCountProgressionEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<BsmMessageCountProgressionEvent> find(Query query);

    List<IDCount> getBsmBroadcastRateEventsByDay(int intersectionID, Long startTime, Long endTime);
}