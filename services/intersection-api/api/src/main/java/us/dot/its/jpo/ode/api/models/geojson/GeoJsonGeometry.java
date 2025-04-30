package us.dot.its.jpo.ode.api.models.geojson;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class GeoJsonGeometry {
    @JsonProperty("type")
    private String type;

    @JsonProperty("coordinates")
    private double[] coordinates;
}