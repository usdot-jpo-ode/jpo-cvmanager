package us.dot.its.jpo.ode.api.accessors.bsm;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.utils.GeographyCalculator;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

@Component
public class OdeBsmJsonRepositoryImpl implements OdeBsmJsonRepository, PageableQuery {

	private final MongoTemplate mongoTemplate;
	public static final ObjectMapper mapper = DateJsonMapper.getInstance()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private final String collectionName = "OdeBsmJson";
	private final String DATE_FIELD = "metadata.odeReceivedAt";
	private final String ORIGIN_IP_FIELD = "metadata.originIp";
	private final String VEHICLE_ID_FIELD = "payload.data.coreData.id";
	private final String LONGITUDE_FIELD = "payload.data.coreData.position.longitude";
	private final String LATITUDE_FIELD = "payload.data.coreData.position.latitude";
	private final String RECORD_GENERATED_AT_FIELD = "recordGeneratedAt";

	@Autowired
	public OdeBsmJsonRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
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
	public Page<OdeMessageFrameData> find(String originIp, String vehicleId, Long startTime, Long endTime,
			Double centerLng, Double centerLat, Double distance, Pageable pageable) {

		Criteria criteria = new IntersectionCriteria()
				.whereOptional(ORIGIN_IP_FIELD, originIp)
				.whereOptional(VEHICLE_ID_FIELD, vehicleId)
				.withinTimeWindow(DATE_FIELD, startTime, endTime, true);

		if (centerLng != null && centerLat != null && distance != null) {
			ReferencedEnvelope boundingBox = GeographyCalculator.calculateBoundingBox(centerLng, centerLat, distance);
			criteria = criteria.and(LATITUDE_FIELD)
					.gte(boundingBox.getMinX())
					.lte(boundingBox.getMaxX())
					.and(LONGITUDE_FIELD)
					.gte(boundingBox.getMinY())
					.lte(boundingBox.getMaxY());
		}
		Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
		List<String> excludedFields = List.of(RECORD_GENERATED_AT_FIELD);

		Page<Document> aggregationResult = findDocumentsWithPagination(mongoTemplate, collectionName, pageable,
				criteria, sort, excludedFields);

		List<OdeMessageFrameData> bsms = aggregationResult.getContent().stream()
				.map(document -> mapper.convertValue(document, OdeMessageFrameData.class)).toList();

		return new PageImpl<>(bsms, pageable, aggregationResult.getTotalElements());
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
			Double distance) {

		Criteria criteria = new IntersectionCriteria()
				.whereOptional(ORIGIN_IP_FIELD, originIp)
				.whereOptional(VEHICLE_ID_FIELD, vehicleId)
				.withinTimeWindow(DATE_FIELD, startTime, endTime, true);

		if (centerLng != null && centerLat != null && distance != null) {
			ReferencedEnvelope boundingBox = GeographyCalculator.calculateBoundingBox(centerLng, centerLat, distance);
			criteria = criteria.and(LATITUDE_FIELD)
					.gte(boundingBox.getMinX())
					.lte(boundingBox.getMaxX())
					.and(LONGITUDE_FIELD)
					.gte(boundingBox.getMinY())
					.lte(boundingBox.getMaxY());
		}
		Query query = Query.query(criteria);
		return mongoTemplate.count(query, Map.class, collectionName);
	}
}
