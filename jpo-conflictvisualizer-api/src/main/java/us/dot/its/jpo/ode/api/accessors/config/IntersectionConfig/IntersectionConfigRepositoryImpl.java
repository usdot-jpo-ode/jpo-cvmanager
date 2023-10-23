
package us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.UpdateType;

@Component
public class IntersectionConfigRepositoryImpl implements IntersectionConfigRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Query getQuery(String key, Integer roadRegulatorID, Integer intersectionID) {
        Query query = new Query();

        if (key != null) {
            query.addCriteria(Criteria.where("_id").is(key));
        }

        // if (roadRegulatorID != null) {
        // query.addCriteria(Criteria.where("roadRegulatorID").is(roadRegulatorID));
        // }

        if (intersectionID != null) {
            query.addCriteria(Criteria.where("intersectionID").is(intersectionID));
        }

        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, IntersectionConfig.class, "CmIntersectionConfig");
    }

    public List<IntersectionConfig> find(Query query) {
        return mongoTemplate.find(query, IntersectionConfig.class, "CmIntersectionConfig");
    }

    public void delete(Query query) {
        mongoTemplate.remove(query, IntersectionConfig.class, "CmIntersectionConfig");
    }

    @Override
    public void save(IntersectionConfig config) {
        Query query = getQuery(config.getKey(), config.getRoadRegulatorID(), config.getIntersectionID());
        query.addCriteria(Criteria.where("updateType").is(UpdateType.INTERSECTION));
        Update update = new Update();
        update.set("key", config.getKey());
        update.set("category", config.getCategory());
        update.set("value", config.getValue());
        update.set("type", config.getType());
        update.set("units", config.getUnits());
        update.set("description", config.getDescription());
        update.set("updateType", config.getUpdateType());
        update.set("value", config.getValue());
        update.set("intersectionID", config.getIntersectionID());
        update.set("roadRegulatorID", config.getRoadRegulatorID());
        update.set("category", config.getCategory());
        mongoTemplate.upsert(query, update, "CmIntersectionConfig");
    }

}
