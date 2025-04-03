
package us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ConnectionOfTravelEventRepository extends DataLoader<ConnectionOfTravelEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<ConnectionOfTravelEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<ConnectionOfTravelEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<IDCount> getAggregatedDailyConnectionOfTravelEventCounts(int intersectionID, Long startTime, Long endTime);

    List<LaneConnectionCount> getConnectionOfTravelEventsByConnection(int intersectionID, Long startTime, Long endTime);
}
