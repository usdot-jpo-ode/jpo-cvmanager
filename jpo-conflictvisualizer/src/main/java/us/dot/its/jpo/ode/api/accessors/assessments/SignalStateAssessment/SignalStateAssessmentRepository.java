
    package us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateAssessment;

    public interface SignalStateAssessmentRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<SignalStateAssessment> find(Query query);  
    }

