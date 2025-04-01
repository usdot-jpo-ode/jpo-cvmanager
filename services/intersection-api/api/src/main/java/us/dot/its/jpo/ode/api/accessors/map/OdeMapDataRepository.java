package us.dot.its.jpo.ode.api.accessors.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.model.OdeMapData;

public interface OdeMapDataRepository extends DataLoader<OdeMapData> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<OdeMapData> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<OdeMapData> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

}