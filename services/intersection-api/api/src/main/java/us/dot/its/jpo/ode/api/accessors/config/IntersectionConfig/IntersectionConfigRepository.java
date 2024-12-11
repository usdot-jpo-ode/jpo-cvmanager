
package us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfig;

public interface IntersectionConfigRepository {
    Query getQuery(String key, Integer roadRegulatorID, Integer intersectionID);

    long getQueryResultCount(Query query);

    List<IntersectionConfig> find(Query query);

    void delete(Query query);

    void save(IntersectionConfig config);
}
