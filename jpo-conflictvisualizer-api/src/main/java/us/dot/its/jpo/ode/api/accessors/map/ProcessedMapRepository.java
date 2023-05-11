package us.dot.its.jpo.ode.api.accessors.map;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.IntersectionReferenceData;
import us.dot.its.jpo.ode.api.models.IDCount;

public interface ProcessedMapRepository{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime,boolean latest);

    long getQueryResultCount(Query query);

    List<ProcessedMap> findProcessedMaps(Query query); 
    
    List<IntersectionReferenceData> getIntersectionIDs();

    List<IDCount> getMapBroadcastRates(int intersectionID, Long startTime, Long endTime);
}