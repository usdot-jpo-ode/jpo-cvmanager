package us.dot.its.jpo.ode.api.accessors.map;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.model.OdeMapData;

public interface OdeMapDataRepository extends DataLoader<OdeMapData>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime,boolean latest);

    long getQueryResultCount(Query query);

    List<OdeMapData> findMaps(Query query); 
    
}