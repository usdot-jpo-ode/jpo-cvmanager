package us.dot.its.jpo.ode.api.accessors.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.asn.j2735.r2024.MapData.MapDataMessageFrame;

public interface OdeMapDataRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapDataMessageFrame> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapDataMessageFrame> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}