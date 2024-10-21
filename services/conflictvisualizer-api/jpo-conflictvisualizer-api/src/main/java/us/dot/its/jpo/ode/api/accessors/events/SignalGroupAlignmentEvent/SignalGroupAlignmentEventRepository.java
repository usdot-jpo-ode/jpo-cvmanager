
package us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SignalGroupAlignmentEventRepository extends DataLoader<SignalGroupAlignmentEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<SignalGroupAlignmentEvent> find(Query query);

    List<IDCount> getSignalGroupAlignmentEventsByDay(int intersectionID, Long startTime, Long endTime);

}
