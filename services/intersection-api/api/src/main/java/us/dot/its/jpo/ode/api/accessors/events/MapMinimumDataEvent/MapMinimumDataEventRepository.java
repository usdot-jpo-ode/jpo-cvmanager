package us.dot.its.jpo.ode.api.accessors.events.MapMinimumDataEvent;
import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface MapMinimumDataEventRepository extends DataLoader<MapMinimumDataEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<MapMinimumDataEvent> find(Query query);

    List<IDCount> getMapMinimumDataEventsByDay(int intersectionID, Long startTime, Long endTime);
}