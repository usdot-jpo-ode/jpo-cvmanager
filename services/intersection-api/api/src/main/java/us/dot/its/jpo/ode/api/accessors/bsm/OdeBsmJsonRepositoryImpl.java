package us.dot.its.jpo.ode.api.accessors.bsm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Component
public class OdeBsmJsonRepositoryImpl  implements OdeBsmJsonRepository{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private ObjectMapper mapper = DateJsonMapper.getInstance();

    private String collectionName = "OdeBsmJson";

    public static Double[] calculateLatitudes(Double centerLatitude, Double radiusInMeters) {
        Double latDiff = radiusInMeters / 111319.9; // Approximate degrees latitude per meter
        Double[] latitudes = {
            centerLatitude - latDiff,

            centerLatitude + latDiff
        };
        return latitudes;
    }

    public static Double[] calculateLongitudes(Double centerLongitude, Double centerLatitude, Double radiusInMeters) {
        Double lonDiff = radiusInMeters / (111319.9 * Math.cos(Math.toRadians(centerLatitude)));
        Double[] longitudes = {
            centerLongitude - lonDiff,
            centerLongitude + lonDiff
        };
        return longitudes;
    }

    public List<OdeBsmData> findOdeBsmDataGeo(String originIp, String vehicleId, Long startTime, Long endTime, Double longitude, Double latitude, Double distance){
        Query query = new Query();

        if(originIp != null){
            query.addCriteria(Criteria.where("metadata.originIp").is(originIp));
        }

        if(vehicleId != null){
            query.addCriteria(Criteria.where("payload.data.coreData.id").is(vehicleId));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if(startTime != null){
            startTimeString = Instant.ofEpochMilli(startTime).toString(); 
        }
        if(endTime != null){
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }
	    query.limit(props.getMaximumResponseSize());
        query.addCriteria(Criteria.where("metadata.odeReceivedAt").gte(startTimeString).lte(endTimeString));
        query.fields().exclude("recordGeneratedAt");
        
        if (longitude!=null && latitude!=null && distance!=null){
            Double[] latitudes = calculateLatitudes(latitude, distance);
            Double[] longitudes = calculateLongitudes(longitude, latitude, distance);

            query.addCriteria(Criteria.where("payload.data.coreData.position.latitude").gte(Math.min(latitudes[0], latitudes[1])).lte(Math.max(latitudes[0], latitudes[1])));
            query.addCriteria(Criteria.where("payload.data.coreData.position.longitude").gte(Math.min(longitudes[0], longitudes[1])).lte(Math.max(longitudes[0], longitudes[1])));
        }

        List<Map> documents = mongoTemplate.find(query, Map.class, collectionName);
        List<OdeBsmData> convertedList = new ArrayList<>();

        for(Map document : documents){
            document.remove("_id");
            OdeBsmData bsm = mapper.convertValue(document, OdeBsmData.class);
            convertedList.add(bsm);
        }
        
        return convertedList;
    }

    public long countOdeBsmDataGeo(String originIp, String vehicleId, Long startTime, Long endTime, Double longitude, Double latitude, Double distance){
        Query query = new Query();

        if(originIp != null){
            query.addCriteria(Criteria.where("metadata.originIp").is(originIp));
        }

        if(vehicleId != null){
            query.addCriteria(Criteria.where("payload.data.coreData.id").is(vehicleId));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if(startTime != null){
            startTimeString = Instant.ofEpochMilli(startTime).toString(); 
        }
        if(endTime != null){
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }
        query.addCriteria(Criteria.where("metadata.odeReceivedAt").gte(startTimeString).lte(endTimeString));
        query.fields().exclude("recordGeneratedAt");
        query.limit(-1);
        
        if (longitude!=null && latitude!=null && distance!=null){
            Double[] latitudes = calculateLatitudes(latitude, distance);
            Double[] longitudes = calculateLongitudes(longitude, latitude, distance);

            query.addCriteria(Criteria.where("payload.data.coreData.position.latitude").gte(Math.min(latitudes[0], latitudes[1])).lte(Math.max(latitudes[0], latitudes[1])));
            query.addCriteria(Criteria.where("payload.data.coreData.position.longitude").gte(Math.min(longitudes[0], longitudes[1])).lte(Math.max(longitudes[0], longitudes[1])));
        }

        return mongoTemplate.count(query, Map.class, collectionName);
    }

    @Override
    public void add(OdeBsmData item) {
        mongoTemplate.save(item, collectionName);
    }

}
