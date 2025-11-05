package us.dot.its.jpo.ode.api.accessors.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.ode.model.OdeMessageFrameData;

public interface OdeMapDataRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<OdeMessageFrameData> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<OdeMessageFrameData> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}