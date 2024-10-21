
package us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ConnectionOfTravelEventRepository extends DataLoader<ConnectionOfTravelEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<ConnectionOfTravelEvent> find(Query query);
    
    List<IDCount> getConnectionOfTravelEventsByDay(int intersectionID, Long startTime, Long endTime);

    List<LaneConnectionCount> getConnectionOfTravelEventsByConnection(int intersectionID, Long startTime, Long endTime);
}

