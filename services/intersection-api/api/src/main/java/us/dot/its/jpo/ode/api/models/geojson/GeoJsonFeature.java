package us.dot.its.jpo.ode.api.models.geojson;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Data
public class GeoJsonFeature {
    @JsonProperty("type")
    private final String type = "Feature";

    private GeoJsonPoint geometry;
    private Object properties;
}