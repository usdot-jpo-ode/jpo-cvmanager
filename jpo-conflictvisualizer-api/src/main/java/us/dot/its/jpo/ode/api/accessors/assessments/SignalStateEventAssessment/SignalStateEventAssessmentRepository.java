
    package us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateEventAssessment;

    public interface SignalStateEventAssessmentRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<SignalStateEventAssessment> find(Query query);  
    }

