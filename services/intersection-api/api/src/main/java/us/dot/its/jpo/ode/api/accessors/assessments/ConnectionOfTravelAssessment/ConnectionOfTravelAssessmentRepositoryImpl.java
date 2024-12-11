package us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@Component
public class ConnectionOfTravelAssessmentRepositoryImpl implements ConnectionOfTravelAssessmentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private String collectionName = "CmConnectionOfTravelAssessment";

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
        return mongoTemplate.count(query, ConnectionOfTravelAssessment.class, collectionName);
    }

    // Removes the result limit on a count query. 
    public long getQueryFullCount(Query query){
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, ConnectionOfTravelAssessment.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<ConnectionOfTravelAssessment> find(Query query) {
        return mongoTemplate.find(query, ConnectionOfTravelAssessment.class, collectionName);
    }

    @Override
    public void add(ConnectionOfTravelAssessment item) {
        mongoTemplate.save(item, collectionName);
    }

}
