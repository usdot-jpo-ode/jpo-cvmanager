package us.dot.its.jpo.ode.api.accessors.srm;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.srm.ProcessedSrm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessedSrmRepository {
    Page<ProcessedSrm> find(Integer intersectionID, String vehicleId, Long startTime, Long endTime,
                    Pageable pageable);

    Page<ProcessedSrm> findByLocation(String vehicleId, Long startTime, Long endTime,
            Double longitude, Double latitude, Double distance, Pageable pageable);

    long count(Integer intersectionID, String vehicleId, Long startTime, Long endTime);
}