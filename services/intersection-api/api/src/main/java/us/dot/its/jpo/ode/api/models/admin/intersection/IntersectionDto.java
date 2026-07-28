package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a single intersection record as returned by GET /admin/intersections.
 */
@Schema(description = "A single intersection record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntersectionDto {
    @Schema(description = "Intersection number", example = "12109")
    @JsonProperty("intersection_id")
    private Integer intersectionId;

    @Schema(description = "Reference point (WGS-84) of the intersection stop bar")
    @JsonProperty("ref_pt")
    private RefPt refPt;

    @Schema(description = "Bounding box of the intersection area; omitted when not set")
    @JsonProperty("bbox")
    private Bbox bbox;

    @Schema(description = "Human-readable name of the intersection", example = "Main St & 1st Ave")
    @JsonProperty("intersection_name")
    private String intersectionName;

    @Schema(description = "Origin IP address of the intersection controller", example = "192.168.1.1")
    @JsonProperty("origin_ip")
    private String originIp;

    @Schema(description = "Organizations associated with this intersection")
    @JsonProperty("organizations")
    private List<String> organizations;

    @Schema(description = "RSU IP addresses associated with this intersection")
    @JsonProperty("rsus")
    private List<String> rsus;
}
