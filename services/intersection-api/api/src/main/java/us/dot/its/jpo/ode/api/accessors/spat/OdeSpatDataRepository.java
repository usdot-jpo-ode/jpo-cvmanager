package us.dot.its.jpo.ode.api.accessors.spat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.model.OdeSpatData;

public interface OdeSpatDataRepository extends DataLoader<OdeSpatData> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<OdeSpatData> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<OdeSpatData> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}