package us.dot.its.jpo.ode.api.models.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoJsonPointDto {
    @JsonProperty("type")
    private final String type = "Point";

    /**
     * GeoJSON coordinates in [longitude, latitude] order (SRID 4326).
     */
    @JsonProperty("coordinates")
    private double[] coordinates;
}
