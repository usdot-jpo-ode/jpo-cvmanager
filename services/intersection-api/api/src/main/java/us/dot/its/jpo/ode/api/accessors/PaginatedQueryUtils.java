package us.dot.its.jpo.ode.api.accessors;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.models.AggregationMetadata;
import us.dot.its.jpo.ode.api.models.AggregationResult;

public class PaginatedQueryUtils {

    /**
     * Build a criteria object based on the given parameters
     * 
     * @param dateField      the name of the date field to query by
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       filtered
     * @param startTime      the start time to query by, if null will not be
     *                       filtered
     * @param endTime        the end time to query by, if null will not be filtered
     * @return the criteria object to use for querying
     */
    private static Criteria buildCriteria(@Nonnull String dateField, @Nullable Integer intersectionID,
            @Nullable Long startTime,
            @Nullable Long endTime) {
        Criteria criteria = new Criteria();

        if (intersectionID != null) {
            criteria = criteria.and("intersectionID").is(intersectionID);
        }

        if (startTime != null) {
            criteria = criteria.and(dateField).gte(new Date(startTime));
        }
        if (endTime != null) {
            criteria = criteria.and(dateField).lte(new Date(endTime));
        }
        return criteria;
    }

    /**
     * Wrap the given object in a page object. Intended to be used when applying a
     * limit of 1 to a query, while attempting to return a Paged response
     * 
     * @param <T>    the type of the object to wrap
     * @param latest the object to wrap
     * @return a page object containing the given object
     */
    public static <T> Page<T> wrapLatestInPage(T latest) {
        return new PageImpl<>(Arrays.asList(latest));
    }

    /**
     * Count data from a given intersectionID, startTime, and endTime, with
     * pagination
     * 
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param dateField      the name of the date field to query by
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       filtered
     * @param startTime      the start time to query by, if null will not be
     *                       filtered
     * @param endTime        the end time to query by, if null will not be filtered
     * @param pageable       the pageable object to use for pagination
     * @return the count of data that matches the given criteria
     */
    public static long countPagedDataFromArgs(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull String dateField,
            @Nonnull Pageable pageable,
            @Nullable Integer intersectionID,
            @Nullable Long startTime,
            @Nullable Long endTime) {
        Criteria criteria = buildCriteria(dateField, intersectionID, startTime, endTime);
        return countPagedData(mongoTemplate, collectionName, criteria, pageable);
    }

    /**
     * Count data from a given intersectionID, startTime, and endTime, without
     * pagination
     * 
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param dateField      the name of the date field to query by
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be
     *                       applied
     * @param endTime        the end time to query by, if null will not be applied
     * @return the count of data that matches the given criteria
     */
    public static long countDataFromArgs(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull String dateField,
            @Nullable Integer intersectionID,
            @Nullable Long startTime,
            @Nullable Long endTime) {
        Criteria criteria = buildCriteria(dateField, intersectionID, startTime, endTime);
        return countData(mongoTemplate, collectionName, criteria);
    }

    /**
     * Get the latest document from a given intersectionID, startTime, and endTime
     * 
     * @param <T>            the type of the entity to return
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param dateField      the name of the date field to query by
     * @param entityClass    the class of the entity to return
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @return the latest document that matches the given criteria
     */
    public static <T> T getLatestDataFromArgs(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull String dateField,
            @Nonnull Class<T> entityClass,
            @Nullable Integer intersectionID,
            @Nullable Long startTime,
            @Nullable Long endTime) {
        Criteria criteria = buildCriteria(dateField, intersectionID, startTime, endTime);
        Sort sort = Sort.by(Sort.Direction.DESC, dateField);
        return getLatestData(mongoTemplate, collectionName, criteria, sort, entityClass);
    }

    /**
     * Get paginated data from a given intersectionID, startTime, and endTime
     * 
     * @param <T>            the type of the entity to return
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param dateField      the name of the date field to query by
     * @param pageable       the pageable object to use for pagination
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @return the paginated data that matches the given criteria
     */
    public static <T> Page<T> getDataFromArgs(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull String dateField,
            @Nonnull Pageable pageable,
            @Nullable Integer intersectionID,
            @Nullable Long startTime,
            @Nullable Long endTime) {
        Criteria criteria = buildCriteria(dateField, intersectionID, startTime, endTime);
        Sort sort = Sort.by(Sort.Direction.DESC, dateField);

        return findPaginatedData(mongoTemplate, collectionName, pageable, criteria, sort);
    }

    /**
     * Count data based on the given criteria
     * 
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param criteria       the criteria object to use for querying
     * @return the count of data that matches the given criteria
     */
    public static long countData(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Criteria criteria) {
        return mongoTemplate.count(Query
                .query(criteria), collectionName);
    }

    /**
     * Count paged data based on the given criteria and pageable object
     * 
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param criteria       the criteria object to use for querying
     * @param pageable       the pageable object to use for pagination
     * @return the count of data that matches the given criteria
     */
    public static long countPagedData(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Criteria criteria,
            @Nonnull Pageable pageable) {
        return mongoTemplate.count(Query
                .query(criteria).with(pageable), collectionName);
    }

    /**
     * Find the latest document based on the given criteria
     * 
     * @param <T>            the type of the entity to return
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param criteria       the criteria object to use for querying
     * @param sort           the sort object to use for sorting
     * @param entityClass    the class of the entity to return
     * @return the latest document that matches the given criteria
     */
    public static <T> T getLatestData(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Criteria criteria,
            @Nonnull Sort sort,
            @Nonnull Class<T> entityClass) {
        return mongoTemplate.findOne(Query.query(criteria).with(sort), entityClass, collectionName);
    }

    /**
     * Find paginated data based on the given criteria and pageable object
     * 
     * @param <T>            the type of the entity to return
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param pageable       the pageable object to use for pagination
     * @param criteria       the criteria object to use for querying
     * @param sort           the sort object to use for sorting
     * @return the paginated data that matches the given criteria
     */
    public static <T> Page<T> findPaginatedData(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Pageable pageable,
            @Nonnull Criteria criteria,
            @Nonnull Sort sort) {
        MatchOperation matchOperation = Aggregation.match(criteria);

        SortOperation sortOperation = Aggregation.sort(sort);
        AggregationOperation facetOperation = context -> new Document("$facet",
                new Document("metadata", Arrays.asList(new Document("$count", "totalCount")))
                        .append("data",
                                Arrays.asList(
                                        new Document("$skip", pageable.getPageNumber() * pageable.getPageSize()),
                                        new Document("$limit", pageable.getPageSize()))));

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, sortOperation, facetOperation);

        // Execute the aggregation
        AggregationResults<AggregationResult> results = mongoTemplate
                .aggregate(aggregation, collectionName, AggregationResult.class);

        // Extract the results
        List<T> data = results.getUniqueMappedResult().getData();
        long totalElements = results.getUniqueMappedResult().getMetadata().isEmpty() ? 0
                : (((List<AggregationMetadata>) results.getUniqueMappedResult().getMetadata()).get(0)).getTotalCount();

        return new PageImpl<>(data, pageable, totalElements);
    }
}
