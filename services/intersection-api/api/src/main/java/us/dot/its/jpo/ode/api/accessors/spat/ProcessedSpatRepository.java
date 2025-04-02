package us.dot.its.jpo.ode.api.accessors.spat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ProcessedSpatRepository extends DataLoader<ProcessedSpat> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<ProcessedSpat> findLatest(Integer intersectionID, Long startTime, Long endTime, boolean compact);

    Page<ProcessedSpat> find(Integer intersectionID, Long startTime, Long endTime, boolean compact,
            Pageable pageable);
}