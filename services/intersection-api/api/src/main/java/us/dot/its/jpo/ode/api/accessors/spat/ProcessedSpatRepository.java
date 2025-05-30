package us.dot.its.jpo.ode.api.accessors.spat;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.models.DataLoader;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessedSpatRepository extends DataLoader<ProcessedSpat> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<ProcessedSpat> findLatest(Integer intersectionID, Long startTime, Long endTime, boolean compact);

    Page<ProcessedSpat> find(Integer intersectionID, Long startTime, Long endTime, boolean compact, Pageable pageable);
}