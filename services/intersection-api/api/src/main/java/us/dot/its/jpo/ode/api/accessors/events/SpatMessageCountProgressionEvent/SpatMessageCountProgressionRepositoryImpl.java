package us.dot.its.jpo.ode.api.accessors.events.SpatMessageCountProgressionEvent;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Sort;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@Component
public class SpatMessageCountProgressionRepositoryImpl implements SpatMessageCountProgressionEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    ConflictMonitorApiProperties props;

    private final String collectionName = "CmSpatMessageCountProgressionEvents";

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

        query.addCriteria(Criteria.where("eventGeneratedAt").gte(startTimeDate).lte(endTimeDate));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "eventGeneratedAt"));
            query.limit(1);
        } else {
            query.limit(props.getMaximumResponseSize());
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, SpatMessageCountProgressionEvent.class, collectionName);
    }

    public long getQueryFullCount(Query query) {
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, SpatMessageCountProgressionEvent.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<SpatMessageCountProgressionEvent> find(Query query) {
        return mongoTemplate.find(query, SpatMessageCountProgressionEvent.class, collectionName);
    }

    @Override
    public void add(SpatMessageCountProgressionEvent item) {
        mongoTemplate.save(item, collectionName);
    }
}
