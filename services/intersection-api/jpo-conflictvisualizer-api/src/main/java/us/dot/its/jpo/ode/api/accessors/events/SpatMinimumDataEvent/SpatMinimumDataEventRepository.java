package us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface SpatMinimumDataEventRepository extends DataLoader<SpatMinimumDataEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<SpatMinimumDataEvent> find(Query query);

    List<IDCount> getSpatMinimumDataEventsByDay(int intersectionID, Long startTime, Long endTime);
}