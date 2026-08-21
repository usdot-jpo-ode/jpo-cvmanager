package us.dot.its.jpo.ode.api.accessors.ssm;

import us.dot.its.jpo.geojsonconverter.pojos.ssm.ProcessedSsm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessedSsmRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<ProcessedSsm> findLatest(Integer intersectionID, Long startTime, Long endTime, boolean compact);

    Page<ProcessedSsm> find(Integer intersectionID, Long startTime, Long endTime, boolean compact, Pageable pageable);
}