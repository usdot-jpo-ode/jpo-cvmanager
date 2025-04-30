package us.dot.its.jpo.ode.api.models.geojson;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class GeoJsonFeature {
    @JsonProperty("type")
    private final String type = "Feature";

    private GeoJsonGeometry geometry;
    private Object properties;
}