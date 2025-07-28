package us.dot.its.jpo.ode.api.accessors.haas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

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
public class HaasLocationDataRepositoryImpl
                implements HaasLocationDataRepository, PageableQuery {

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
        public Page<HaasLocation> find(
                        boolean activeOnly,
                        Long startTime,
                        Long endTime,
                        Pageable pageable) {
                Criteria timeCriteria = new IntersectionCriteria()
                                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);

                // Create base aggregation pipeline
                List<AggregationOperation> pipeline = buildBasePipeline(timeCriteria, activeOnly);

                // Add pagination operations
                List<AggregationOperation> paginatedPipeline = addPaginationOperations(pipeline, pageable);

                // Execute aggregation
                Aggregation aggregation = Aggregation.newAggregation(paginatedPipeline);
                AggregationResults<HaasLocation> results = mongoTemplate.aggregate(
                                aggregation, collectionName, HaasLocation.class);

                // Count total documents for pagination
                long total = countTotalDocuments(pipeline);

                List<HaasLocation> content = results.getMappedResults();
                return new PageImpl<>(content, pageable, total);
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

        /**
         * Adds pagination operations (skip and limit) to the pipeline
         */
        private List<AggregationOperation> addPaginationOperations(List<AggregationOperation> basePipeline,
                        Pageable pageable) {
                List<AggregationOperation> paginatedPipeline = new ArrayList<>(basePipeline);
                paginatedPipeline.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
                paginatedPipeline.add(Aggregation.limit(pageable.getPageSize()));
                return paginatedPipeline;
        }

        /**
         * Counts the total number of documents for pagination
         */
        private long countTotalDocuments(List<AggregationOperation> pipeline) {
                Aggregation countAggregation = Aggregation.newAggregation(
                                pipeline.toArray(new AggregationOperation[0]));
                AggregationResults<HaasLocation> countResults = mongoTemplate.aggregate(
                                countAggregation, collectionName, HaasLocation.class);
                return countResults.getMappedResults().size();
        }

        @Override
        public void add(HaasLocation item) {
                mongoTemplate.insert(item, collectionName);
        }

}
