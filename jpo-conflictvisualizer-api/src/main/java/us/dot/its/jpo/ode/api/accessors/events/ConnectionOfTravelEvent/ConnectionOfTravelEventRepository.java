
    package us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;

    public interface ConnectionOfTravelEventRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<ConnectionOfTravelEvent> find(Query query);  
    }

