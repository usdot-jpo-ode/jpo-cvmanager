
    package us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment;

    import java.util.List;

    import org.springframework.data.mongodb.core.query.Query;
    import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
    import us.dot.its.jpo.ode.api.models.DataLoader;

    public interface LaneDirectionOfTravelAssessmentRepository extends DataLoader<LaneDirectionOfTravelAssessment>{
        Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

        long getQueryResultCount(Query query);

        long getQueryFullCount(Query query);
        
        List<LaneDirectionOfTravelAssessment> find(Query query);

        List<LaneDirectionOfTravelAssessment> getLaneDirectionOfTravelOverTime(int intersectionID, long startTime, long endTime);
    }

