
package us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;

@Component
public class LaneDirectionOfTravelAssessmentRepositoryImpl implements LaneDirectionOfTravelAssessmentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        if (startTime == null) {
            startTime = 0L;
        }
        if (endTime == null) {
            endTime = Instant.now().toEpochMilli();
        }

        query.addCriteria(Criteria.where("assessmentGeneratedAt").gte(startTime).lte(endTime));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "assessmentGeneratedAt"));
            query.limit(1);
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, LaneDirectionOfTravelAssessment.class, "CmLaneDirectionOfTravelAssessment");
    }

    public List<LaneDirectionOfTravelAssessment> find(Query query) {
        return mongoTemplate.find(query, LaneDirectionOfTravelAssessment.class, "CmLaneDirectionOfTravelAssessment");
    }

}
