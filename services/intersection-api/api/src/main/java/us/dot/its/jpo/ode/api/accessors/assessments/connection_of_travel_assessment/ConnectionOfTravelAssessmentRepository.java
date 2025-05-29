
package us.dot.its.jpo.ode.api.accessors.assessments.connection_of_travel_assessment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ConnectionOfTravelAssessmentRepository extends DataLoader<ConnectionOfTravelAssessment> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<ConnectionOfTravelAssessment> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<ConnectionOfTravelAssessment> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}