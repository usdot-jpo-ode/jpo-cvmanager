
package us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface IntersectionReferenceAlignmentEventRepository extends DataLoader<IntersectionReferenceAlignmentEvent>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<IntersectionReferenceAlignmentEvent> find(Query query);

    List<IDCount> getIntersectionReferenceAlignmentEventsByDay(int intersectionID, Long startTime, Long endTime);
}

