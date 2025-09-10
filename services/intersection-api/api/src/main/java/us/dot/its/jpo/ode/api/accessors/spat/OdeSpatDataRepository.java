package us.dot.its.jpo.ode.api.accessors.spat;

import org.springframework.data.domain.Page;

import us.dot.its.jpo.ode.model.OdeMessageFrameData;

public interface OdeSpatDataRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<OdeMessageFrameData> findLatest(Integer intersectionID, Long startTime, Long endTime);
}