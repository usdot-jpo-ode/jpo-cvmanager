package us.dot.its.jpo.ode.api.accessors.haas;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.models.haas.HaasLocationResult;

public interface HaasLocationDataRepository extends DataLoader<HaasLocation> {
    HaasLocationResult findWithLimit(boolean activeOnly, Long startTime, Long endTime, int limit);
}