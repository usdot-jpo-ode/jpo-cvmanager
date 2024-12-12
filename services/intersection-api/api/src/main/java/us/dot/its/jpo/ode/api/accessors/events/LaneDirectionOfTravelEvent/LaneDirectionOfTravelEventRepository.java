
package us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface LaneDirectionOfTravelEventRepository extends DataLoader<LaneDirectionOfTravelEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);

    List<LaneDirectionOfTravelEvent> find(Query query);

    List<IDCount> getLaneDirectionOfTravelEventsByDay(int intersectionID, Long startTime, Long endTime);

    List<IDCount> getMedianDistanceByFoot(int intersectionID, long startTime, long endTime);

    List<IDCount> getMedianDistanceByDegree(int intersectionID, long startTime, long endTime);
}
