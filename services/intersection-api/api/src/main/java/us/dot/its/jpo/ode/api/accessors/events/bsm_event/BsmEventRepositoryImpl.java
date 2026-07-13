package us.dot.its.jpo.ode.api.accessors.events.bsm_event;

import java.time.Instant;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.api.models.IDCount;

@Component
public class BsmEventRepositoryImpl
		implements BsmEventRepository, PageableQuery {

	private final MongoTemplate mongoTemplate;

	private final String collectionName = "CmBsmEvents";
	private final String DATE_FIELD = "startingBsmTimestamp";
	private final String INTERSECTION_ID_FIELD = "intersectionID";

	private final TypeReference<BsmEvent> bsmEventTypeReference = new TypeReference<>() {
	};
	private final ObjectMapper mapper = DateJsonMapper.getInstance()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Autowired
	public BsmEventRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * Get a page representing the count of data for a given intersectionID,
	 * startTime, and endTime
	 *
	 * @param intersectionID the intersection ID to query by, if null will not be
	 *                       applied
	 * @param startTime      the start time to query by, if null will not be applied
	 * @param endTime        the end time to query by, if null will not be applied
	 * @return the paginated data that matches the given criteria
	 */
	public long count(
			Integer intersectionID,
			Long startTime,
			Long endTime) {
		Criteria criteria = new IntersectionCriteria()
				.whereOptional(INTERSECTION_ID_FIELD, intersectionID)
				.withinTimeWindow(DATE_FIELD, startTime, endTime, IntersectionCriteria.TimeStampFormat.LONG);
		Query query = Query.query(criteria);
		return mongoTemplate.count(query, collectionName);
	}

	/**
	 * Get a page containing the single most recent record for a given
	 * intersectionID, startTime, and endTime
	 *
	 * @param intersectionID the intersection ID to query by, if null will not be
	 *                       applied
	 * @param startTime      the start time to query by, if null will not be applied
	 * @param endTime        the end time to query by, if null will not be applied
	 * @return the paginated data that matches the given criteria
	 */
	public Page<BsmEvent> findLatest(
			Integer intersectionID,
			Long startTime,
			Long endTime) {
		Criteria criteria = new IntersectionCriteria()
				.whereOptional(INTERSECTION_ID_FIELD, intersectionID)
				.withinTimeWindow(DATE_FIELD, startTime, endTime, IntersectionCriteria.TimeStampFormat.LONG);
		Query query = Query.query(criteria);
		Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
		Document document = mongoTemplate.findOne(
				query.with(sort),
				Document.class,
				collectionName);
		BsmEvent event = mapper.convertValue(document, bsmEventTypeReference);
		return wrapSingleResultWithPage(event);
	}

	/**
	 * Get paginated data from a given intersectionID, startTime, and endTime
	 *
	 * @param intersectionID the intersection ID to query by, if null will not be
	 *                       applied
	 * @param startTime      the start time to query by, if null will not be applied
	 * @param endTime        the end time to query by, if null will not be applied
	 * @param pageable       the pageable object to use for pagination
	 * @return the paginated data that matches the given criteria
	 */
	public Page<BsmEvent> find(
			Integer intersectionID,
			Long startTime,
			Long endTime,
			Pageable pageable) {
		Criteria criteria = new IntersectionCriteria()
				.whereOptional(INTERSECTION_ID_FIELD, intersectionID)
				.withinTimeWindow(DATE_FIELD, startTime, endTime, IntersectionCriteria.TimeStampFormat.LONG);
		Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);

		List<String> excludedFields = List.of("recordGeneratedAt");
		Page<Document> documentPage = findDocumentsWithPagination(
				mongoTemplate,
				collectionName,
				pageable,
				criteria,
				sort,
				excludedFields);

		List<BsmEvent> events = documentPage.getContent().stream()
				.map(document -> mapper.convertValue(document, bsmEventTypeReference))
				.toList();

		return new PageImpl<>(events, pageable, documentPage.getTotalElements());
	}

	@Override
	public List<IDCount> getAggregatedDailyBsmEventCounts(int intersectionID, Long startTime, Long endTime) {
		if (startTime == null) {
			startTime = 0L;
		}
		if (endTime == null) {
			endTime = Instant.now().toEpochMilli();
		}

		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
				Aggregation.match(Criteria.where("").gte(startTime).lte(endTime)),
				Aggregation.project("startingBsmTimestamp"),
				Aggregation.project()
						.and(ConvertOperators.ToDate.toDate("$startingBsmTimestamp"))
						.as("date"),
				Aggregation.project()
						.and(DateOperators.DateToString.dateOf("date").toString("%Y-%m-%d"))
						.as("dateStr"),
				Aggregation.group("dateStr").count().as("count"));

		AggregationResults<IDCount> result = mongoTemplate.aggregate(aggregation, collectionName,
				IDCount.class);
		List<IDCount> results = result.getMappedResults();

		return results;
	}
}
