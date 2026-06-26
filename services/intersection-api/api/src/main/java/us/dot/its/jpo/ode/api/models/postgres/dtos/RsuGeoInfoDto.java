package us.dot.its.jpo.ode.api.models.postgres.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonPointDto;

/**
 * GeoJSON Feature DTO returned by the /devices/rsus/info endpoint.
 * Mirrors the GeoJSON Feature produced by the Python rsuinfo.get_rsu_data
 * query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RsuGeoInfoDto {

    @JsonProperty("type")
    private final String type = "Feature";

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("geometry")
    private GeoJsonPointDto geometry;

    @JsonProperty("properties")
    private RsuGeoInfoPropertiesDto properties;
}
