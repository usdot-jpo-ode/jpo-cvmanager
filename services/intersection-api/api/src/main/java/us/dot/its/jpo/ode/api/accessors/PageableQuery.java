package us.dot.its.jpo.ode.api.accessors;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import us.dot.its.jpo.ode.api.models.AggregationResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface PageableQuery {
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
    default <T> Page<T> findPage(
            @Nonnull MongoTemplate mongoTemplate,
            @Nonnull String collectionName,
            @Nonnull Pageable pageable,
            @Nonnull Criteria criteria,
            @Nonnull Sort sort,
            @Nullable List<String> excludedFields) {
        List<String> fieldsToExclude = excludedFields != null ? excludedFields : Collections.emptyList();

        MatchOperation matchOperation = Aggregation.match(criteria);

        SortOperation sortOperation = Aggregation.sort(sort);

        AggregationOperation facetOperation = context -> new Document("$facet",
                new Document("metadata", List.of(new Document("$count", "count")))
                        .append("results",
                                Arrays.asList(
                                        new Document("$skip", pageable.getPageNumber() * pageable.getPageSize()),
                                        new Document("$limit", pageable.getPageSize()))));
        Aggregation aggregation;
        if (!fieldsToExclude.isEmpty()) {
            AggregationOperation projectOperation = context -> {
                Document projectFields = new Document();
                for (String field : fieldsToExclude) {
                    projectFields.append(field, 0); // Exclude the field by setting it to 0
                }
                return new Document("$project", projectFields);
            };
            aggregation = Aggregation.newAggregation(
                    matchOperation,
                    sortOperation,
                    projectOperation,
                    facetOperation);
        } else {
            aggregation = Aggregation.newAggregation(
                    matchOperation,
                    sortOperation,
                    facetOperation);
        }

        // Execute the aggregation
        @SuppressWarnings("rawtypes")
        AggregationResults<AggregationResult> results = mongoTemplate
                .aggregate(aggregation, collectionName, AggregationResult.class);

        // Extract the results
        @SuppressWarnings("unchecked")
        AggregationResult<T> result = results.getUniqueMappedResult();
        if (result == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<T> data = result.getResults();
        long totalElements = result.getCounts().getFirst();

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
        return new PageImpl<>(Collections.singletonList(latest));
    }

    /**
     * Create a pageable object based on the given page and size. If size is null,
     * return null.
     * 
     * @param page the page number to use, nullable
     * @param size the size of the page to use, nullable. If null, no pageable
     *             object is returned
     * @return a pageable object based on the given page and size, or null if either
     *         is null
     */
    default Pageable createNullablePage(@Nullable Integer page, @Nullable Integer size) {
        if (size == null) {
            return null;
        }
        return PageRequest.of(page == null ? 0 : page, size);
    }
}
