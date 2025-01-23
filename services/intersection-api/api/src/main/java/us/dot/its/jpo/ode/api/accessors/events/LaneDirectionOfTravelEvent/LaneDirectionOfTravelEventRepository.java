
package us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface LaneDirectionOfTravelEventRepository extends DataLoader<LaneDirectionOfTravelEvent> {
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);

    List<LaneDirectionOfTravelEvent> find(Query query);

    List<IDCount> getLaneDirectionOfTravelEventsByDay(int intersectionID, Long startTime, Long endTime);

    /**
     * Get the median distance from the lane direction of travel events in FEET
     * 
     * @param intersectionID the intersection ID
     * @param startTime      start time to filter by, in milliseconds since epoch
     * @param endTime        end time to filter by, in milliseconds since epoch
     * @return a list of IDCount objects, with ID being the truncated centerline
     *         distance in feet, and count being the count in that bin
     */
    List<IDCount> countEventsByCenterlineDistance(int intersectionID, long startTime, long endTime);

    List<IDCount> getMedianDistanceByDegree(int intersectionID, long startTime, long endTime);
}
