package us.dot.its.jpo.ode.api.accessors.bsm;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.model.OdeBsmData;

public interface OdeBsmJsonRepository extends DataLoader<OdeBsmData>{
    List<OdeBsmData> findOdeBsmDataGeo(String originIp, String vehicleId, Long startTime, Long endTime, Double longitude, Double latitude);  
}