
    package us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;

    public interface IntersectionReferenceAlignmentEventRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<IntersectionReferenceAlignmentEvent> find(Query query);  
    }

