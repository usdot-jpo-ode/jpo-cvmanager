package us.dot.its.jpo.ode.api.accessors.haas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

public interface HaasLocationDataRepository extends DataLoader<HaasLocation> {
    long count(boolean activeOnly, Long startTime, Long endTime, Pageable pageable);

    Page<HaasLocation> find(boolean activeOnly, Long startTime, Long endTime, Pageable pageable);
}