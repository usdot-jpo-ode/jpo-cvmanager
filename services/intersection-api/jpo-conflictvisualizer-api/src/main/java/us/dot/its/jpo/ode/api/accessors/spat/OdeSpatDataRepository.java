package us.dot.its.jpo.ode.api.accessors.spat;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.model.OdeSpatData;

public interface OdeSpatDataRepository extends DataLoader<OdeSpatData>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime,boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);

    List<OdeSpatData> findSpats(Query query); 
    
}