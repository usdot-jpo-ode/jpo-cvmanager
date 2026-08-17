package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response shape for GET /admin/intersections (list all intersections).
 */
@Schema(description = "Response for GET /admin/intersections — list of all accessible intersections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntersectionListResponse {
    @Schema(description = "List of intersection records accessible to the requesting user")
    @JsonProperty("intersection_data")
    private List<IntersectionDto> intersectionData;
}
