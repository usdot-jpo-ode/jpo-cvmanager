
package us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalStateEventRepository extends DataLoader<StopLinePassageEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<StopLinePassageEvent> find(Query query);

    List<IDCount> getSignalStateEventsByDay(int intersectionID, Long startTime, Long endTime);
}

