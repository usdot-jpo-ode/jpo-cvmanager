package us.dot.its.jpo.ode.api.accessors.bsm;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.utils.GeographyCalculator;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProcessedBsmRepositoryImpl implements ProcessedBsmRepository, PageableQuery {

	private final MongoTemplate mongoTemplate;

	private final String collectionName = "ProcessedBsm";
	private final String DATE_FIELD = "properties.odeReceivedAt";
	private final String ORIGIN_IP_FIELD = "properties.originIp";
	private final String VEHICLE_ID_FIELD = "properties.id";
	private final String LONGITUDE_FIELD = "geometry.coordinates.0";
	private final String LATITUDE_FIELD = "geometry.coordinates.1";
	private final String RECORD_GENERATED_AT_FIELD = "recordGeneratedAt";

	TypeReference<ProcessedBsm<Point>> processedBsmTypeReference = new TypeReference<>() {
	};

	public static final ObjectMapper mapper = DateJsonMapper.getInstance()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public ProcessedBsmRepositoryImpl(MongoTemplate mongoTemplate) {
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
	public Page<ProcessedBsm<Point>> find(String originIp, String vehicleId, Long startTime, Long endTime,
			Double centerLng, Double centerLat, Double distance, Pageable pageable) {

		Criteria criteria = new IntersectionCriteria()
				.whereOptional(ORIGIN_IP_FIELD, originIp)
				.whereOptional(VEHICLE_ID_FIELD, vehicleId)
				.withinTimeWindow(DATE_FIELD, startTime, endTime, true);

		if (centerLng != null && centerLat != null && distance != null) {
			double[] boundingBox = GeographyCalculator.calculateBoundingBox(centerLng, centerLat, distance);

			criteria = criteria.and(LATITUDE_FIELD)
					.gte(Math.min(boundingBox[0], boundingBox[1]))
					.lte(Math.max(boundingBox[0], boundingBox[1]))
					.and(LONGITUDE_FIELD)
					.gte(Math.min(boundingBox[2], boundingBox[3]))
					.lte(Math.max(boundingBox[2], boundingBox[3]));
		}
		Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
		List<String> excludedFields = List.of(RECORD_GENERATED_AT_FIELD);

		Page<Document> aggregationResult = findDocumentsWithPagination(mongoTemplate, collectionName, pageable,
				criteria, sort, excludedFields);

		List<ProcessedBsm<Point>> bsms = aggregationResult.getContent().stream()
				.map(document -> mapper.convertValue(document, processedBsmTypeReference)).toList();

		return new PageImpl<ProcessedBsm<Point>>(bsms, pageable, aggregationResult.getTotalElements());
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
			double[] boundingBox = GeographyCalculator.calculateBoundingBox(centerLng, centerLat, distance);

			criteria = criteria.and(LATITUDE_FIELD)
					.gte(Math.min(boundingBox[0], boundingBox[1]))
					.lte(Math.max(boundingBox[0], boundingBox[1]))
					.and(LONGITUDE_FIELD)
					.gte(Math.min(boundingBox[2], boundingBox[3]))
					.lte(Math.max(boundingBox[2], boundingBox[3]));
		}
		Query query = Query.query(criteria);
		return mongoTemplate.count(query, Map.class, collectionName);
	}
}
