
package us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

@Component
public class LaneDirectionOfTravelAssessmentRepositoryImpl implements LaneDirectionOfTravelAssessmentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private String collectionName = "CmLaneDirectionOfTravelAssessment";

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
        }else{
            query.limit(props.getMaximumResponseSize());
        }

        return query;
    }



    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, LaneDirectionOfTravelAssessment.class, collectionName);
    }

    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, LaneDirectionOfTravelAssessment.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<LaneDirectionOfTravelAssessment> find(Query query) {
        return mongoTemplate.find(query, LaneDirectionOfTravelAssessment.class, collectionName);
    }

    public List<LaneDirectionOfTravelAssessment> getLaneDirectionOfTravelOverTime(int intersectionID, long startTime, long endTime){

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("intersectionID").is(intersectionID)),
            Aggregation.match(Criteria.where("timestamp").gte(startTime).lte(endTime))
        );

        AggregationResults<LaneDirectionOfTravelAssessment> result = mongoTemplate.aggregate(aggregation, collectionName, LaneDirectionOfTravelAssessment.class);
        List<LaneDirectionOfTravelAssessment> results = result.getMappedResults();

        return results;
    }

    @Override
    public void add(LaneDirectionOfTravelAssessment item) {
        mongoTemplate.save(item, collectionName);
    }

}
