package us.dot.its.jpo.ode.api.accessors.bsm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.referencing.GeodeticCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Component
public class OdeBsmJsonRepositoryImpl implements OdeBsmJsonRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private final ObjectMapper mapper = DateJsonMapper.getInstance()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String collectionName = "OdeBsmJson";

    /**
     * Calculate the latitude range for a given center point and distance
     * 
     * @param centerLng the center longitude
     * @param centerLat the center latitude
     * @param distance  the distance in meters
     * @return double[] containing the min and max latitudes
     */
    private double[] calculateLatitudes(double centerLng, double centerLat, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(centerLng, centerLat);

        calculator.setDirection(0, distance);
        double maxLat = calculator.getDestinationGeographicPoint().getY();

        calculator.setDirection(180, distance);
        double minLat = calculator.getDestinationGeographicPoint().getY();

        return new double[] { minLat, maxLat };
    }

    /**
     * Calculate the longitude range for a given center point and distance
     * 
     * @param centerLng the center longitude
     * @param centerLat the center latitude
     * @param distance  the distance in meters
     * @return double[] containing the min and max longitudes
     */
    private double[] calculateLongitudes(double centerLng, double centerLat, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(centerLng, centerLat);

        calculator.setDirection(90, distance);
        double maxLng = calculator.getDestinationGeographicPoint().getX();

        calculator.setDirection(270, distance);
        double minLng = calculator.getDestinationGeographicPoint().getX();

        return new double[] { minLng, maxLng };
    }

    /**
     * Filter OdeBsmData by originIp, vehicleId, startTime, endTime, and a bounding
     * box
     * 
     * @param originIp  the origin IP
     * @param vehicleId the vehicle ID
     * @param startTime the start time
     * @param endTime   the end time
     * @param centerLng the longitude (in degrees) of the center of the bounding box
     * @param centerLat the latitude (in degrees) of the center of the bounding box
     * @param distance  the "radius" of the bounding box, in meters (total width is
     *                  2x distance)
     */
    public List<OdeBsmData> findOdeBsmDataGeo(String originIp, String vehicleId, Long startTime, Long endTime,
            Double centerLng, Double centerLat, Double distance) {
        Query query = new Query();

        if (originIp != null) {
            query.addCriteria(Criteria.where("metadata.originIp").is(originIp));
        }

        if (vehicleId != null) {
            query.addCriteria(Criteria.where("payload.data.coreData.id").is(vehicleId));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }
        query.limit(props.getMaximumResponseSize());
        query.addCriteria(Criteria.where("metadata.odeReceivedAt").gte(startTimeString).lte(endTimeString));
        query.fields().exclude("recordGeneratedAt");

        if (centerLng != null && centerLat != null && distance != null) {
            double[] latitudes = calculateLatitudes(centerLng, centerLat, distance);
            double[] longitudes = calculateLongitudes(centerLng, centerLat, distance);

            query.addCriteria(Criteria.where("payload.data.coreData.position.latitude")
                    .gte(Math.min(latitudes[0], latitudes[1])).lte(Math.max(latitudes[0], latitudes[1])));
            query.addCriteria(Criteria.where("payload.data.coreData.position.longitude")
                    .gte(Math.min(longitudes[0], longitudes[1])).lte(Math.max(longitudes[0], longitudes[1])));
        }

        List<Map> documents = mongoTemplate.find(query, Map.class, collectionName);
        return documents.stream()
                .map(document -> mapper.convertValue(document, OdeBsmData.class)).toList();
    }

    /**
     * Count filtered OdeBsmData by originIp, vehicleId, startTime, endTime, and a
     * bounding box
     * 
     * @param originIp  the origin IP
     * @param vehicleId the vehicle ID
     * @param startTime the start time
     * @param endTime   the end time
     * @param centerLng the longitude (in degrees) of the center of the bounding box
     * @param centerLat the latitude (in degrees) of the center of the bounding box
     * @param distance  the "radius" of the bounding box, in meters (total width is
     *                  2x distance)
     */
    public long countOdeBsmDataGeo(String originIp, String vehicleId, Long startTime, Long endTime, Double centerLng,
            Double centerLat, Double distance) {
        Query query = new Query();

        if (originIp != null) {
            query.addCriteria(Criteria.where("metadata.originIp").is(originIp));
        }

        if (vehicleId != null) {
            query.addCriteria(Criteria.where("payload.data.coreData.id").is(vehicleId));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }
        query.addCriteria(Criteria.where("metadata.odeReceivedAt").gte(startTimeString).lte(endTimeString));
        query.fields().exclude("recordGeneratedAt");
        query.limit(-1);

        if (centerLng != null && centerLat != null && distance != null) {
            double[] latitudes = calculateLatitudes(centerLng, centerLat, distance);
            double[] longitudes = calculateLongitudes(centerLng, centerLat, distance);

            query.addCriteria(Criteria.where("payload.data.coreData.position.latitude")
                    .gte(Math.min(latitudes[0], latitudes[1])).lte(Math.max(latitudes[0], latitudes[1])));
            query.addCriteria(Criteria.where("payload.data.coreData.position.longitude")
                    .gte(Math.min(longitudes[0], longitudes[1])).lte(Math.max(longitudes[0], longitudes[1])));
        }

        return mongoTemplate.count(query, Map.class, collectionName);
    }

    @Override
    public void add(OdeBsmData item) {
        mongoTemplate.insert(item, collectionName);
    }

}
