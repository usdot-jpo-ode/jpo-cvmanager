
package us.dot.its.jpo.ode.api.accessors.assessments.stop_line_passage_assessment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;

public interface StopLinePassageAssessmentRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageAssessment> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLinePassageAssessment> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}
