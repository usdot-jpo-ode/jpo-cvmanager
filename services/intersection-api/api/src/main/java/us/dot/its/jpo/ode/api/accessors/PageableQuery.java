package us.dot.its.jpo.ode.api.accessors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import us.dot.its.jpo.ode.api.models.AggregationResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface PageableQuery {

    Logger logger = LoggerFactory.getLogger(PageableQuery.class);

    /**
     * Find paginated data based on the given criteria and pageable object
     *
     * @param <T>            the type of the entity to return
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param pageable       the pageable object to use for pagination
     * @param criteria       the criteria object to use for querying
     * @param sort           the sort object to use for sorting
     * @param outputType     the class type of the output
     * @return the paginated data that matches the given criteria
     */
    default <T> Page<T> findPage(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Pageable pageable,
            @Nonnull Criteria criteria,
            @Nonnull Sort sort,
            @Nullable List<String> excludedFields,
            @Nonnull Class<T> outputType) {
        List<String> fieldsToExclude = excludedFields != null ? excludedFields : Collections.emptyList();

        AggregationResult aggregationResult = getAggregationResult(mongoTemplate, collectionName, pageable, criteria,
                sort, fieldsToExclude);
        if (aggregationResult == null || aggregationResult.getMetadata().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<T> results = aggregationResult.getResults().stream().map(doc -> {
            T convertedResult = mongoTemplate.getConverter().read(outputType, doc);
            if (convertedResult != null) {
                return convertedResult;
            } else {
                logger.error("Failed to convert document to type {}: {}", outputType.getName(), doc);
                return null;
            }
        }).filter(result -> result != null).toList();

        return new PageImpl<>(results, pageable, aggregationResult.getMetadata().getFirst().getCount());
    }

    /**
     * Find paginated data based on the given criteria and pageable object
     *
     * @param mongoTemplate  the mongo template object to query with
     * @param collectionName the collection name to query
     * @param pageable       the pageable object to use for pagination
     * @param criteria       the criteria object to use for querying
     * @param sort           the sort object to use for sorting
     * @return the paginated data that matches the given criteria
     */
    default Page<Document> findDocumentsWithPagination(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Pageable pageable,
            @Nonnull Criteria criteria,
            @Nonnull Sort sort,
            @Nullable List<String> excludedFields) {
        List<String> fieldsToExclude = excludedFields != null ? excludedFields : Collections.emptyList();

        AggregationResult result = getAggregationResult(mongoTemplate, collectionName, pageable, criteria, sort,
                fieldsToExclude);
        if (result == null || result.getMetadata().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Document> data = result.getResults();
        long totalElements = result.getMetadata().getFirst().getCount();

        return new PageImpl<>(data, pageable, totalElements);
    }

    /**
     * Wrap the given object in a page object. Intended to be used when applying a
     * limit of 1 to a query, while attempting to return a Paged response
     *
     * @param <T>    the type of the object to wrap
     * @param latest the object to wrap
     * @return a page object containing the given object
     */
    default <T> Page<T> wrapSingleResultWithPage(T latest) {
        List<T> resultList = new ArrayList<>();
        if (latest != null) {
            resultList.add(latest);
        }
        return new PageImpl<>(resultList);
    }

    private static AggregationResult getAggregationResult(@Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Pageable pageable,
            @Nonnull Criteria criteria,
            @Nonnull Sort sort,
            @Nonnull List<String> excludedFields) {
        List<AggregationOperation> operations = new ArrayList<>();

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(sort);

        operations.add(matchOperation);
        operations.add(sortOperation);

        // Add project operation if we need to exclude fields
        if (!excludedFields.isEmpty()) {
            AggregationOperation projectOperation = context -> {
                Document projectFields = new Document();
                for (String field : excludedFields) {
                    projectFields.append(field, 0); // Exclude the field by setting it to 0
                }
                return new Document("$project", projectFields);
            };
            operations.add(projectOperation);
        }

        // Create a facet operation that gets both results and count in one query
        AggregationOperation facetOperation = context -> new Document("$facet",
                new Document("metadata", List.of(new Document("$count", "count")))
                        .append("results",
                                Arrays.asList(
                                        new Document("$skip", pageable.getPageNumber() * pageable.getPageSize()),
                                        new Document("$limit", pageable.getPageSize()))));
        operations.add(facetOperation);

        // Execute the aggregation
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<AggregationResult> results = mongoTemplate
                .aggregate(aggregation, collectionName, AggregationResult.class);

        return results.getUniqueMappedResult();
    }
}
