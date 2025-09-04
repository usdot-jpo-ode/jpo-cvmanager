package us.dot.its.jpo.ode.api.accessors.spat;

import org.springframework.data.domain.Page;

import us.dot.its.jpo.asn.j2735.r2024.SPAT.SPAT;

public interface OdeSpatDataRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SPAT> findLatest(Integer intersectionID, Long startTime, Long endTime);
}