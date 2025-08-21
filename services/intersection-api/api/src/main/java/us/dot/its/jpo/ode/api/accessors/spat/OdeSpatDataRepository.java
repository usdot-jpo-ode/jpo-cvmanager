package us.dot.its.jpo.ode.api.accessors.spat;

import org.springframework.data.domain.Page;

import us.dot.its.jpo.asn.j2735.r2024.SPAT.SPATMessageFrame;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface OdeSpatDataRepository extends DataLoader<SPATMessageFrame> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SPATMessageFrame> findLatest(Integer intersectionID, Long startTime, Long endTime);
}