package us.dot.its.jpo.ode.api.accessors.events.BsmEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface BsmEventRepository extends DataLoader<BsmEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<BsmEvent> find(Query query);
    
    List<IDCount> getBsmEventsByDay(int intersectionID, Long startTime, Long endTime);

}

