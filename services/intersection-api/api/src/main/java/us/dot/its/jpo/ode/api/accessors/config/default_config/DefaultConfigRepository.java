
package us.dot.its.jpo.ode.api.accessors.config.default_config;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;

public interface DefaultConfigRepository {
    Query getQuery(String id);

    long getQueryResultCount(Query query);

    @SuppressWarnings("rawtypes")
    List<DefaultConfig> find(Query query);

    void save(DefaultConfig<?> config);

}
