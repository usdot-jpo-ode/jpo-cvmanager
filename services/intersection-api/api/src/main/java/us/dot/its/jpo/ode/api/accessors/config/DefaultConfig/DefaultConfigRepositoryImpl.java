
package us.dot.its.jpo.ode.api.accessors.config.DefaultConfig;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;

@Slf4j
@Component
public class DefaultConfigRepositoryImpl implements DefaultConfigRepository {

    private final String collectionName = "CmDefaultConfig";

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DefaultConfigRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Query getQuery(String id) {
        Query query = new Query();

        if (id != null) {
            query.addCriteria(Criteria.where("_id").is(id));
        }
        return query;
    }

    public long getQueryResultCount(Query query) {
        return mongoTemplate.count(query, DefaultConfig.class, collectionName);
    }

    public List<DefaultConfig> find(Query query) {
        return mongoTemplate.find(query, DefaultConfig.class, collectionName);
    }

    @Override
    public void save(DefaultConfig<?> config) {

        Class<?> type;
        try {
            Query query = getQuery(config.getKey());
            Update update = new Update();

            String typeString = config.getType();
            type = Class.forName(typeString);

            if (typeString.equals("java.lang.Integer")) {
                update.set("value", type.cast(Integer.parseInt((String) config.getValue())));
            } else if (typeString.equals("java.lang.Double")) {
                update.set("value", type.cast(Double.parseDouble((String) config.getValue())));
            } else if (typeString.equals("java.lang.Long")) {
                update.set("value", type.cast(Long.parseLong((String) config.getValue())));
            } else {
                update.set("value", type.cast(config.getValue()));
            }

            mongoTemplate.upsert(query, update, collectionName);
        } catch (ClassNotFoundException e) {
            log.error("Unable to serialize configuration parameters, class not found", e);
        }
    }

}
