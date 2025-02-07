package us.dot.its.jpo.ode.api.accessors.spat;

import java.util.List;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.models.DataLoader;

import org.springframework.data.mongodb.core.query.Query;

public interface ProcessedSpatRepository extends DataLoader<ProcessedSpat>{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest, boolean compact);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<ProcessedSpat> findProcessedSpats(Query query);

}