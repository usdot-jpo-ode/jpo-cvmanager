
    package us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;

    public interface SignalStateConflictEventRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<SignalStateConflictEvent> find(Query query);  
    }

