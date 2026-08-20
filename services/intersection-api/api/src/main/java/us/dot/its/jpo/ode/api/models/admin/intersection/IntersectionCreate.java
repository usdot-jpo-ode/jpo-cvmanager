package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body for POST /admin/intersections.
 */
@Schema(description = "Request body for creating a new intersection with organization and RSU associations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntersectionCreate {
    @Schema(description = "Intersection number identifier", example = "12109", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("intersection_id")
    private Integer intersectionId;

    @Schema(description = "Reference point (WGS-84) for the intersection stop bar", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    @JsonProperty("ref_pt")
    private RefPt refPt;

    @Schema(description = "Organizations to associate with this intersection", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Size(min = 1, message = "At least one organization is required")
    @JsonProperty("organizations")
    private List<String> organizations;

    @Schema(description = "RSU IP addresses to associate with this intersection", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("rsus")
    private List<@Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "must be a valid IPv4 address") String> rsus;

    @Schema(description = "Bounding box; omit if not applicable")
    @Valid
    @JsonProperty("bbox")
    private Bbox bbox;

    @Schema(description = "Human-readable name for the intersection", example = "Main St & 1st Ave")
    @JsonProperty("intersection_name")
    private String intersectionName;

    @Schema(description = "Origin IP address of the intersection controller", example = "10.0.0.1")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "must be a valid IPv4 address")
    @JsonProperty("origin_ip")
    private String originIp;
}
