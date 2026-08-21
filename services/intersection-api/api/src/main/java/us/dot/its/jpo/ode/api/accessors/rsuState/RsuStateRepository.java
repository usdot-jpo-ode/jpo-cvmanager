package us.dot.its.jpo.ode.api.accessors.rsuState;

import java.util.List;
import us.dot.its.jpo.ode.api.models.snmp.RsuState;

public interface RsuStateRepository {
    List<RsuState> retrieveRsuStateWithinTimeInterval(String rsuIP, long start, long end);

    RsuState findLatestByRsuIP(String rsuIP);

    List<RsuState> retrieveRsuStateWithinTimeInterval(String rsuIP, long start, long end,
            int intervalMinutes);

}