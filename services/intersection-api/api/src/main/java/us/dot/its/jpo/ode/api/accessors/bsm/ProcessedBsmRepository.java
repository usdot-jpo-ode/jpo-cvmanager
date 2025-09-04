package us.dot.its.jpo.ode.api.accessors.bsm;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessedBsmRepository {
    Page<ProcessedBsm<Point>> find(String originIp, String vehicleId, Long startTime, Long endTime,
            Double longitude, Double latitude, Double distance, Pageable pageable);

    long count(String originIp, String vehicleId, Long startTime, Long endTime, Double longitude,
            Double latitude, Double distance);
}