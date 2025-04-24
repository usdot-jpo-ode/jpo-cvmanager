
package us.dot.its.jpo.ode.api.accessors.assessments.StopLinePassageAssessment;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLinePassageAssessmentRepository extends DataLoader<StopLinePassageAssessment> {
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);

    List<StopLinePassageAssessment> find(Query query);
}
