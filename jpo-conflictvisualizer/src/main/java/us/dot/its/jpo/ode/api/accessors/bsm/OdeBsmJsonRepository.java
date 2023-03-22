package us.dot.its.jpo.ode.api.accessors.bsm;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.model.OdeBsmData;

public interface OdeBsmJsonRepository{
    Query getQuery(String originIp, String vehicle_id, Long startTime, Long endTime);

    long getQueryResultCount(Query query);
    
    List<OdeBsmData> findOdeBsmData(Query query);  
}