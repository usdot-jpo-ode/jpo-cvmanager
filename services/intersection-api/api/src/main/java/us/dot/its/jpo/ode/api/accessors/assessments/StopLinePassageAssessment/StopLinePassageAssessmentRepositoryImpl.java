
package us.dot.its.jpo.ode.api.accessors.assessments.StopLinePassageAssessment;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@Component
public class StopLinePassageAssessmentRepositoryImpl implements StopLinePassageAssessmentRepository {

    private final MongoTemplate mongoTemplate;
    private final ConflictMonitorApiProperties props;

    private String collectionName = "StopLinePassageAssessment";

    @Autowired
    public StopLinePassageAssessmentRepositoryImpl(MongoTemplate mongoTemplate,
            ConflictMonitorApiProperties props) {
        this.mongoTemplate = mongoTemplate;
        this.props = props;
    }

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        Date startTimeDate = new Date(0);
        Date endTimeDate = new Date();

        if (startTime != null) {
            startTimeDate = new Date(startTime);
        }
        if (endTime != null) {
            endTimeDate = new Date(endTime);
        }

        query.addCriteria(Criteria.where("assessmentGeneratedAt").gte(startTimeDate).lte(endTimeDate));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "assessmentGeneratedAt"));
            query.limit(1);
        } else {
            query.limit(props.getMaximumResponseSize());
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, StopLinePassageAssessment.class, collectionName);
    }

    public long getQueryFullCount(Query query) {
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, StopLinePassageAssessment.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<StopLinePassageAssessment> find(Query query) {
        return mongoTemplate.find(query, StopLinePassageAssessment.class, collectionName);
    }

    @Override
    public void add(StopLinePassageAssessment item) {
        mongoTemplate.insert(item, collectionName);
    }

}
