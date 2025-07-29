package us.dot.its.jpo.ode.api.accessors.haas;

import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.models.haas.HaasLocationResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

@Component
public class HaasLocationDataRepositoryImpl
                implements HaasLocationDataRepository {

        private final MongoTemplate mongoTemplate;

        private final String collectionName = "HaasAlertLocation";
        private final String DATE_FIELD = "start_time";
        private final String IS_ACTIVE_FIELD = "is_active";
        private final String ID_FIELD = "id";

        @Autowired
        public HaasLocationDataRepositoryImpl(MongoTemplate mongoTemplate) {
                this.mongoTemplate = mongoTemplate;
        }

        /**
         * Get a limited list of HAAS locations with the given criteria
         *
         * @param activeOnly the active only filter, if true will only return active
         *                   records
         * @param startTime  the start time to query by, if null will not be applied
         * @param endTime    the end time to query by, if null will not be applied
         * @param limit      the maximum number of records to return
         * @return the HaasLocationResult containing locations and truncation info
         */
        public HaasLocationResult findWithLimit(
                        boolean activeOnly,
                        Long startTime,
                        Long endTime,
                        int limit) {
                Criteria timeCriteria = new IntersectionCriteria()
                                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);

                // Create base aggregation pipeline
                List<AggregationOperation> pipeline = buildBasePipeline(timeCriteria, activeOnly);

                // Add limit operation (request one more to check if there are more results)
                pipeline.add(Aggregation.limit(limit + 1));

                // Execute aggregation
                Aggregation aggregation = Aggregation.newAggregation(pipeline);
                AggregationResults<HaasLocation> results = mongoTemplate.aggregate(
                                aggregation, collectionName, HaasLocation.class);

                List<HaasLocation> allResults = results.getMappedResults();

                // Check if there are more results available
                boolean hasMoreResults = allResults.size() > limit;

                // If we got more results than the limit, trim to the limit
                List<HaasLocation> finalResults = hasMoreResults ? allResults.subList(0, limit) : allResults;

                return new HaasLocationResult(finalResults, hasMoreResults);
        }

        /**
         * Builds the base aggregation pipeline with time filtering and active-only
         * logic
         */
        private List<AggregationOperation> buildBasePipeline(Criteria timeCriteria, boolean activeOnly) {
                List<AggregationOperation> pipeline = new ArrayList<>();

                // Match documents within time window
                pipeline.add(Aggregation.match(timeCriteria));

                if (activeOnly) {
                        addActiveOnlyFilter(pipeline, timeCriteria);
                }

                // Group by ID and get the latest record for each ID
                pipeline.add(Aggregation.sort(Sort.Direction.DESC, DATE_FIELD));
                pipeline.add(Aggregation.group(ID_FIELD).first("$$ROOT").as("latest"));
                pipeline.add(Aggregation.replaceRoot("latest"));

                return pipeline;
        }

        /**
         * Adds filtering logic for active-only records
         */
        private void addActiveOnlyFilter(List<AggregationOperation> pipeline, Criteria timeCriteria) {
                // For activeOnly, exclude IDs that have any inactive records
                List<Object> inactiveIds = getInactiveIds(timeCriteria);
                pipeline.add(Aggregation.match(
                                Criteria.where(ID_FIELD).not().in(inactiveIds)));
                pipeline.add(Aggregation.match(Criteria.where(IS_ACTIVE_FIELD).is(true)));
        }

        /**
         * Gets the list of IDs that have inactive records within the time window
         */
        private List<Object> getInactiveIds(Criteria timeCriteria) {
                Aggregation inactiveIdsAggregation = Aggregation.newAggregation(
                                Aggregation.match(new Criteria()
                                                .andOperator(
                                                                timeCriteria,
                                                                Criteria.where(IS_ACTIVE_FIELD).is(false))),
                                Aggregation.group(ID_FIELD));

                AggregationResults<Document> inactiveResults = mongoTemplate.aggregate(
                                inactiveIdsAggregation, collectionName, Document.class);

                return inactiveResults.getMappedResults().stream()
                                .map(doc -> doc.get("_id"))
                                .toList();
        }

        @Override
        public void add(HaasLocation item) {
                mongoTemplate.insert(item, collectionName);
        }

}
