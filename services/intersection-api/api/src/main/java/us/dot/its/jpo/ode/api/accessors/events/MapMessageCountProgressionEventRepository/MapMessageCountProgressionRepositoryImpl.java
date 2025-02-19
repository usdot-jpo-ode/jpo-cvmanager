package us.dot.its.jpo.ode.api.accessors.events.MapMessageCountProgressionEventRepository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Sort;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;

@Component
public class MapMessageCountProgressionRepositoryImpl implements MapMessageCountProgressionEventRepository {

    private final MongoTemplate mongoTemplate;

    @Value("${maximumResponseSize}")
    int maximumResponseSize;

    private final String collectionName = "CmMapMessageCountProgressionEvents";

    @Autowired
    public MapMessageCountProgressionRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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

        query.addCriteria(Criteria.where("eventGeneratedAt").gte(startTimeDate).lte(endTimeDate));
        if (latest) {
            query.with(Sort.by(Sort.Direction.DESC, "eventGeneratedAt"));
            query.limit(1);
        } else {
            query.limit(maximumResponseSize);
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, MapMessageCountProgressionEvent.class, collectionName);
    }

    public long getQueryFullCount(Query query) {
        int limit = query.getLimit();
        query.limit(-1);
        long count = mongoTemplate.count(query, MapMessageCountProgressionEvent.class, collectionName);
        query.limit(limit);
        return count;
    }

    public List<MapMessageCountProgressionEvent> find(Query query) {
        return mongoTemplate.find(query, MapMessageCountProgressionEvent.class, collectionName);
    }

    @Override
    public void add(MapMessageCountProgressionEvent item) {
        mongoTemplate.insert(item, collectionName);
    }
}
