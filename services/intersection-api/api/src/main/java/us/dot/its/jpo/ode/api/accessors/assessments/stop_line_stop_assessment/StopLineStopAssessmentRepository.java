
package us.dot.its.jpo.ode.api.accessors.assessments.stop_line_stop_assessment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessment;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface StopLineStopAssessmentRepository extends DataLoader<StopLineStopAssessment> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLineStopAssessment> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<StopLineStopAssessment> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}