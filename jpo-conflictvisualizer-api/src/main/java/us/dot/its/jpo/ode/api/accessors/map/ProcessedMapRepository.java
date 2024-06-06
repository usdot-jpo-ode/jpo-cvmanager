package us.dot.its.jpo.ode.api.accessors.map;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;

public interface ProcessedMapRepository extends DataLoader<ProcessedMap<LineString>>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest, boolean compact);

    long getQueryResultCount(Query query);

    List<ProcessedMap<LineString>> findProcessedMaps(Query query); 
    
    List<IntersectionReferenceData> getIntersectionIDs();

    List<IDCount> getMapBroadcastRates(int intersectionID, Long startTime, Long endTime);

    List<IDCount> getMapBroadcastRateDistribution(int intersectionID, Long startTime, Long endTime);

    List<IntersectionReferenceData> getIntersectionsContainingPoint(double longitude, double latitude);
}