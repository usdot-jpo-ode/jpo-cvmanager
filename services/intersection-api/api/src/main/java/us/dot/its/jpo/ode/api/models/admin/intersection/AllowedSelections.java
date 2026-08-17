package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * The set of organizations and RSUs the requesting user is allowed to assign to an intersection.
 * Superusers receive all orgs/RSUs; non-superusers receive only those within their qualified organizations.
 */
@Schema(description = "Organizations and RSUs the requesting user is authorized to assign to an intersection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllowedSelections {
    @Schema(description = "Organization names available for assignment")
    @JsonProperty("organizations")
    private List<String> organizations;

    @Schema(description = "RSU IP addresses available for assignment")
    @JsonProperty("rsus")
    private List<String> rsus;
}
