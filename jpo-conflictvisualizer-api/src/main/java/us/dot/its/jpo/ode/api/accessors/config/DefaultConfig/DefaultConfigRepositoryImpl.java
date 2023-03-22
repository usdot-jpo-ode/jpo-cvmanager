
package us.dot.its.jpo.ode.api.accessors.config.DefaultConfig;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.UpdateType;

@Component
public class DefaultConfigRepositoryImpl implements DefaultConfigRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Query getQuery(String key) {
        Query query = new Query();

        if (key != null) {
            query.addCriteria(Criteria.where("_id").is(key));
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, DefaultConfig.class, "CmDefaultConfig");
    }

    public List<DefaultConfig> find(Query query) {
        return mongoTemplate.find(query, DefaultConfig.class, "CmDefaultConfig");
    }

    @Override
    public void save(DefaultConfig config) {
        Query query = getQuery(config.getKey());
        query.addCriteria(Criteria.where("updateType").is(UpdateType.DEFAULT));
        Update update = new Update();
        update.set("value", config.getValue());
        update.set("category", config.getCategory());
        update.set("key", config.getKey());
        mongoTemplate.upsert(query, update, "CmDefaultConfig");
    }

}
