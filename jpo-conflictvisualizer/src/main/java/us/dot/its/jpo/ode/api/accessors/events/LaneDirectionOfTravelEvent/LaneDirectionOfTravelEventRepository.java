
    package us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;

    public interface LaneDirectionOfTravelEventRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<LaneDirectionOfTravelEvent> find(Query query);  
    }

