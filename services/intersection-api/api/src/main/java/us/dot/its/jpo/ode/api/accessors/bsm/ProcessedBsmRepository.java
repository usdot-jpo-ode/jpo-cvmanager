package us.dot.its.jpo.ode.api.accessors.bsm;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessedBsmRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<ProcessedBsm<Point>> findLatest(Integer intersectionID, Long startTime, Long endTime, boolean compact);

    Page<ProcessedBsm<Point>> find(Integer intersectionID, Long startTime, Long endTime, boolean compact,
            Pageable pageable);
}