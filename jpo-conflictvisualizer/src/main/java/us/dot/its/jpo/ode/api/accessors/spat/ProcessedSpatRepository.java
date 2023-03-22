package us.dot.its.jpo.ode.api.accessors.spat;

import java.util.List;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;

import org.springframework.data.mongodb.core.query.Query;

public interface ProcessedSpatRepository{
    Query getQuery(Integer intersectionID, Long startTime, Long endTime);

    long getQueryResultCount(Query query);
    
    List<ProcessedSpat> findProcessedMaps(Query query); 
}