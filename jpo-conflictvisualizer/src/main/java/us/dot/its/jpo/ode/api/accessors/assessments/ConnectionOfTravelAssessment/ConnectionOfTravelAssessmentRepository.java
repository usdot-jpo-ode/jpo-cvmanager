
    package us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;

    public interface ConnectionOfTravelAssessmentRepository{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);
        
        List<ConnectionOfTravelAssessment> find(Query query);  
    }

