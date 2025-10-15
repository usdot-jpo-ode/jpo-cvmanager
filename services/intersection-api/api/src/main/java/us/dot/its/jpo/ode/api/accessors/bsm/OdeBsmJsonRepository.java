package us.dot.its.jpo.ode.api.accessors.bsm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

public interface OdeBsmJsonRepository {
        Page<OdeMessageFrameData> find(String originIp, String vehicleId, Long startTime, Long endTime,
                        Double longitude, Double latitude, Double distance, Pageable pageable);

        long count(String originIp, String vehicleId, Long startTime, Long endTime, Double longitude,
                        Double latitude, Double distance);
}