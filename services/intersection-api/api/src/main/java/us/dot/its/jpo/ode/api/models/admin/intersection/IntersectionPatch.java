package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body for PATCH /admin/intersections.
 */
@Schema(description = "Request body for updating an intersection's properties and organization/RSU associations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntersectionPatch {
    @Schema(description = "Current intersection number that identifies the record to update", example = "12109", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("orig_intersection_id")
    private Integer origIntersectionId;

    @Schema(description = "New intersection number (may equal orig_intersection_id)", example = "12109", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("intersection_id")
    private Integer intersectionId;

    @Schema(description = "New reference point (WGS-84) for the intersection stop bar", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    @JsonProperty("ref_pt")
    private RefPt refPt;

    @Schema(description = "New bounding box; omit to leave unchanged")
    @Valid
    @JsonProperty("bbox")
    private Bbox bbox;

    @Schema(description = "New human-readable name; omit to leave unchanged", example = "Main St & 1st Ave")
    @JsonProperty("intersection_name")
    private String intersectionName;

    @Schema(description = "New origin IP address; omit to leave unchanged", example = "192.168.1.1")
    @JsonProperty("origin_ip")
    private String originIp;

    @Schema(description = "Organizations to add to this intersection", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("organizations_to_add")
    private List<String> organizationsToAdd;

    @Schema(description = "Organizations to remove from this intersection", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("organizations_to_remove")
    private List<String> organizationsToRemove;

    @Schema(description = "RSU IP addresses to associate with this intersection", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("rsus_to_add")
    private List<@Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "must be a valid IPv4 address") String> rsusToAdd;

    @Schema(description = "RSU IP addresses to disassociate from this intersection", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("rsus_to_remove")
    private List<@Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "must be a valid IPv4 address") String> rsusToRemove;
}
