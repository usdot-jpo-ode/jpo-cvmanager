
    package us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateEvent;
import us.dot.its.jpo.ode.api.models.IDCount;

    public interface SignalStateEventRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<SignalStateEvent> find(Query query);

        List<IDCount> getSignalStateEventsByDay(int intersectionID, Long startTime, Long endTime);
    }

