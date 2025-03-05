
package us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface LaneDirectionOfTravelAssessmentRepository extends DataLoader<LaneDirectionOfTravelAssessment> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<LaneDirectionOfTravelAssessment> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<LaneDirectionOfTravelAssessment> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    List<LaneDirectionOfTravelAssessment> getLaneDirectionOfTravelOverTime(int intersectionID, long startTime,
            long endTime);
}