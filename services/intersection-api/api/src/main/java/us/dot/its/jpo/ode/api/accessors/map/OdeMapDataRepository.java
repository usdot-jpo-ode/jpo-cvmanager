package us.dot.its.jpo.ode.api.accessors.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.asn.j2735.r2024.MapData.MapData;

public interface OdeMapDataRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapData> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapData> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}