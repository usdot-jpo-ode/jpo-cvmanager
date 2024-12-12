
package us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface TimeChangeDetailsEventRepository extends DataLoader<TimeChangeDetailsEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<TimeChangeDetailsEvent> find(Query query);

    List<IDCount> getTimeChangeDetailsEventsByDay(int intersectionID, Long startTime, Long endTime);
}

