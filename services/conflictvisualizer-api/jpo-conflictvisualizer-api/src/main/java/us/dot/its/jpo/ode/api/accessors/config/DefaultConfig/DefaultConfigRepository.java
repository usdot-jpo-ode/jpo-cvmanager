
package us.dot.its.jpo.ode.api.accessors.config.DefaultConfig;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;

public interface DefaultConfigRepository {
    Query getQuery(String key);

    long getQueryResultCount(Query query);

    List<DefaultConfig> find(Query query);

    void save(DefaultConfig config);

}
