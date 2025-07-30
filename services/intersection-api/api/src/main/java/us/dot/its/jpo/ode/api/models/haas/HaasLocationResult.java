package us.dot.its.jpo.ode.api.models.haas;

import lombok.Data;
import java.util.List;

@Data
public class HaasLocationResult {
    private final List<HaasLocation> locations;
    private final boolean hasMoreResults;

    public HaasLocationResult(List<HaasLocation> locations, boolean hasMoreResults) {
        this.locations = locations;
        this.hasMoreResults = hasMoreResults;
    }
}