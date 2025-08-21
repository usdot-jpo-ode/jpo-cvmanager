package us.dot.its.jpo.ode.api.accessors.bsm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.asn.j2735.r2024.BasicSafetyMessage.BasicSafetyMessageMessageFrame;

public interface OdeBsmJsonRepository {
        Page<BasicSafetyMessageMessageFrame> find(String originIp, String vehicleId, Long startTime, Long endTime,
                        Double longitude, Double latitude, Double distance, Pageable pageable);

        long count(String originIp, String vehicleId, Long startTime, Long endTime, Double longitude,
                        Double latitude, Double distance);
}