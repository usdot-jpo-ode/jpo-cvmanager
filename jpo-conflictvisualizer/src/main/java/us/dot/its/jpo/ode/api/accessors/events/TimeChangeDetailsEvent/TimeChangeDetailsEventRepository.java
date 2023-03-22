
    package us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;

    public interface TimeChangeDetailsEventRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<TimeChangeDetailsEvent> find(Query query);  
    }

