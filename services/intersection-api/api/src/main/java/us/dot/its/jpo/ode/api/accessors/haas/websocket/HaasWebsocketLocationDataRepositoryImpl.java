package us.dot.its.jpo.ode.api.accessors.haas.websocket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.models.haas.websocket.HaasWebsocketLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

@Component
public class HaasWebsocketLocationDataRepositoryImpl
                implements HaasWebsocketLocationDataRepository, PageableQuery {

        private final MongoTemplate mongoTemplate;

        private final String collectionName = "HaasAlertLocation";
        private final String DATE_FIELD = "start_time";
        private final String IS_ACTIVE_FIELD = "is_active";
        private final String ID_FIELD = "id";

        @Autowired
        public HaasWebsocketLocationDataRepositoryImpl(MongoTemplate mongoTemplate) {
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
         * @param pageable       the pageable object to use for pagination
         * @return the paginated data that matches the given criteria
         */
        public long count(
                        boolean activeOnly,
                        Long startTime,
                        Long endTime,
                        Pageable pageable) {
                Criteria criteria = new IntersectionCriteria()
                                .whereOptional(IS_ACTIVE_FIELD, activeOnly)
                                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
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
        public Page<HaasWebsocketLocation> findLatest(
                        boolean activeOnly,
                        Long startTime,
                        Long endTime,
                        Pageable pageable) {
                Criteria timeCriteria = new IntersectionCriteria()
                                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);

                // Create base aggregation pipeline
                List<AggregationOperation> pipeline = new ArrayList<>();

                // Match documents within time window
                pipeline.add(Aggregation.match(timeCriteria));

                if (activeOnly) {
                        // For activeOnly, exclude IDs that have any inactive records
                        pipeline.add(Aggregation.match(
                                        Criteria.where(ID_FIELD).not().in(
                                                        mongoTemplate.aggregate(
                                                                        Aggregation.newAggregation(
                                                                                        Aggregation.match(new Criteria()
                                                                                                        .andOperator(
                                                                                                                        timeCriteria,
                                                                                                                        Criteria.where(IS_ACTIVE_FIELD)
                                                                                                                                        .is(false))),
                                                                                        Aggregation.group(ID_FIELD)),
                                                                        collectionName,
                                                                        Document.class).getMappedResults().stream()
                                                                        .map(doc -> doc.get("_id")).toList())));
                        pipeline.add(Aggregation.match(Criteria.where(IS_ACTIVE_FIELD).is(true)));
                }

                // Group by ID and get the latest record for each ID
                pipeline.add(Aggregation.sort(Sort.Direction.DESC, DATE_FIELD));
                pipeline.add(Aggregation.group(ID_FIELD).first("$$ROOT").as("latest"));
                pipeline.add(Aggregation.replaceRoot("latest"));

                // Skip and limit for pagination
                pipeline.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
                pipeline.add(Aggregation.limit(pageable.getPageSize()));

                // Execute aggregation
                Aggregation aggregation = Aggregation.newAggregation(pipeline);
                AggregationResults<HaasWebsocketLocation> results = mongoTemplate.aggregate(
                                aggregation, collectionName, HaasWebsocketLocation.class);

                // Count total documents for pagination
                long total = mongoTemplate.aggregate(
                                Aggregation.newAggregation(
                                                pipeline.subList(0, pipeline.size() - 2) // Remove skip and limit
                                                                .toArray(new AggregationOperation[0])),
                                collectionName,
                                HaasWebsocketLocation.class).getMappedResults().size();

                List<HaasWebsocketLocation> content = results.getMappedResults();
                return new PageImpl<>(content, pageable, total);
        }

        @Override
        public void add(HaasWebsocketLocation item) {
                mongoTemplate.insert(item, collectionName);
        }

}
