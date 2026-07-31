package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response shape for GET /admin/intersections/{intersectionId}.
 */
@Schema(description = "Response for GET /admin/intersections/{intersectionId} — single intersection with allowed selections for UI dropdowns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntersectionSingleResponse {
    @Schema(description = "The intersection record; an empty object if the intersection was not found")
    @JsonProperty("intersection_data")
    private IntersectionDto intersectionDto;

    @Schema(description = "Organizations and RSUs the requesting user may assign to this intersection")
    @JsonProperty("allowed_selections")
    private AllowedSelections allowedSelections;
}
