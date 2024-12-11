
package us.dot.its.jpo.ode.api.accessors.config.DefaultConfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;

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

            mongoTemplate.upsert(query, update, "CmDefaultConfig");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
