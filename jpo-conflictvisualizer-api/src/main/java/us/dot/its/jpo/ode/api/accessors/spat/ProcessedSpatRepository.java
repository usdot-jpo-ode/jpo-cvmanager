package us.dot.its.jpo.ode.api.accessors.spat;

import java.util.List;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;

import org.springframework.data.mongodb.core.query.Query;

public interface ProcessedSpatRepository extends DataLoader<ProcessedSpat>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime);

    long getQueryResultCount(Query query);
    
    List<ProcessedSpat> findProcessedMaps(Query query);

    List<IDCount> getSpatBroadcastRates(int intersectionID, Long startTime, Long endTime);
}