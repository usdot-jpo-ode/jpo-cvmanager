
package us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ConnectionOfTravelAssessmentRepository extends DataLoader<ConnectionOfTravelAssessment>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<ConnectionOfTravelAssessment> find(Query query);  

    
}

