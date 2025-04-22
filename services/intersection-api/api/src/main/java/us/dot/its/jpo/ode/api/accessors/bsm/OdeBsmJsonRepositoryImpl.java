package us.dot.its.jpo.ode.api.accessors.bsm;

import java.util.Map;

import javax.annotation.Nullable;

import org.geotools.referencing.GeodeticCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Component
public class OdeBsmJsonRepositoryImpl implements OdeBsmJsonRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "OdeBsmJson";
    private final String DATE_FIELD = "recordGeneratedAt";
    private final String ORIGIN_IP_FIELD = "metadata.originIp";
    private final String VEHICLE_ID_FIELD = "payload.data.coreData.id";

    @Autowired
    public OdeBsmJsonRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

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
    public Page<OdeBsmData> find(String originIp, String vehicleId, Long startTime, Long endTime,
            Double centerLng, Double centerLat, Double distance, Pageable pageable) {

        Criteria criteria = new IntersectionCriteria()
                .whereOptional(ORIGIN_IP_FIELD, originIp)
                .whereOptional(VEHICLE_ID_FIELD, vehicleId)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);

        if (centerLng != null && centerLat != null && distance != null) {
            double[] latitudes = calculateLatitudes(centerLng, centerLat, distance);
            double[] longitudes = calculateLongitudes(centerLng, centerLat, distance);
            criteria = criteria.and("payload.data.coreData.position.latitude")
                    .gte(Math.min(latitudes[0], latitudes[1])).lte(Math.max(latitudes[0], latitudes[1]))
                    .and("payload.data.coreData.position.longitude")
                    .gte(Math.min(longitudes[0], longitudes[1])).lte(Math.max(longitudes[0], longitudes[1]));
        }
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);

        // TODO: Consider mapping with jackson ObjectMapper
        return findPage(mongoTemplate, collectionName, pageable, criteria, sort);
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
    public long count(
            String originIp,
            String vehicleId,
            Long startTime,
            Long endTime,
            Double centerLng,
            Double centerLat,
            Double distance,
            @Nullable Pageable pageable) {

        Criteria criteria = new IntersectionCriteria()
                .whereOptional(ORIGIN_IP_FIELD, originIp)
                .whereOptional(VEHICLE_ID_FIELD, vehicleId)
                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);

        if (centerLng != null && centerLat != null && distance != null) {
            double[] latitudes = calculateLatitudes(centerLng, centerLat, distance);
            double[] longitudes = calculateLongitudes(centerLng, centerLat, distance);
            criteria = criteria.and("payload.data.coreData.position.latitude")
                    .gte(Math.min(latitudes[0], latitudes[1])).lte(Math.max(latitudes[0], latitudes[1]))
                    .and("payload.data.coreData.position.longitude")
                    .gte(Math.min(longitudes[0], longitudes[1])).lte(Math.max(longitudes[0], longitudes[1]));
        }
        Query query = Query.query(criteria);
        if (pageable != null) {
            query = query.with(pageable);
        }

        return mongoTemplate.count(query, Map.class, collectionName);
    }

    @Override
    public void add(OdeBsmData item) {
        mongoTemplate.insert(item, collectionName);
    }

}
