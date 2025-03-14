package us.dot.its.jpo.ode.api.accessors.spat;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@Component
public class ProcessedSpatRepositoryImpl implements ProcessedSpatRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private final String collectionName = "ProcessedSpat";

    public Query getQuery(Integer intersectionID, Long startTime, Long endTime, boolean latest, boolean compact) {
        Query query = new Query();

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionId").is(intersectionID));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if (startTime != null) {
            startTimeString = Instant.ofEpochMilli(startTime).toString();
        }
        if (endTime != null) {
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }

        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "utcTimeStamp"));
            query.limit(1);
        } else {
            query.limit(props.getMaximumResponseSize());
        }

        if (compact) {
            query.fields().exclude("recordGeneratedAt", "validationMessages");
        } else {
            query.fields().exclude("recordGeneratedAt");
        }

        query.addCriteria(Criteria.where("utcTimeStamp").gte(startTimeString).lte(endTimeString));
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, ProcessedSpat.class, collectionName);
    }

    public long getQueryFullCount(Query query) {
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, ProcessedSpat.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<ProcessedSpat> findProcessedSpats(Query query) {
        return mongoTemplate.find(query, ProcessedSpat.class, collectionName);
    }

    @Override
    public void add(ProcessedSpat item) {
        mongoTemplate.insert(item, collectionName);
    }
}
