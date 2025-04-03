package us.dot.its.jpo.ode.api.accessors.reports;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.api.models.ReportDocument;

@Component
public class ReportRepositoryImpl
        implements ReportRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmReport";
    private final String DATE_FIELD = "reportGeneratedAt";
    private final String INTERSECTION_ID_FIELD = "intersectionID";
    private final String REPORT_NAME_FIELD = "reportName";

    @Autowired
    public ReportRepositoryImpl(MongoTemplate mongoTemplate) {
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
            String reportName,
            Integer intersectionID,
            Long startTime,
            Long endTime,
            @Nullable Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(REPORT_NAME_FIELD, reportName)
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime);
        Query query = Query.query(criteria);
        if (pageable != null) {
            query = query.with(pageable);
        }
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
    public Page<ReportDocument> findLatest(
            String reportName,
            Integer intersectionID,
            Long startTime,
            Long endTime,
            boolean includeReportContents) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(REPORT_NAME_FIELD, reportName)
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime);
        Query query = Query.query(criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        // TODO: Add exclusion for reportContents if !includeReportContents
        return wrapSingleResultWithPage(
                mongoTemplate.findOne(
                        query.with(sort),
                        ReportDocument.class,
                        collectionName));
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
    public Page<ReportDocument> find(
            String reportName,
            Integer intersectionID,
            Long startTime,
            Long endTime,
            boolean includeReportContents,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(REPORT_NAME_FIELD, reportName)
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .withinTimeWindow(DATE_FIELD, startTime, endTime);
        Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
        // TODO: Add exclusion for reportContents if !includeReportContents
        return findPage(mongoTemplate, collectionName, pageable, criteria, sort, null);
    }

    @Override
    public void add(ReportDocument item) {
        mongoTemplate.insert(item, collectionName);
    }
}