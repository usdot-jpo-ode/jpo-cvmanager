package us.dot.its.jpo.ode.api.models.geojson;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class GeoJsonFeatureCollection {
    @JsonProperty("type")
    private final String type = "FeatureCollection";

    @JsonProperty("features")
    private List<GeoJsonFeature> features;
}