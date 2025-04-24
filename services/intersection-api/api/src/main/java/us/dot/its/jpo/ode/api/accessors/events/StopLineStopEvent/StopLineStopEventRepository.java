
package us.dot.its.jpo.ode.api.accessors.events.StopLineStopEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLineStopEventRepository extends DataLoader<StopLineStopEvent> {
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);

    List<StopLineStopEvent> find(Query query);

    List<IDCount> getAggregatedDailyStopLineStopEventCounts(int intersectionID, Long startTime, Long endTime);
}
