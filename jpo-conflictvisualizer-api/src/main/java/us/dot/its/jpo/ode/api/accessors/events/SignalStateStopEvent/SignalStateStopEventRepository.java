
package us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateStopEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalStateStopEventRepository extends DataLoader<SignalStateStopEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);
    
    List<SignalStateStopEvent> find(Query query);

    List<IDCount> getSignalStateStopEventsByDay(int intersectionID, Long startTime, Long endTime);
}

