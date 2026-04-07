package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "WGS-84 geographic reference point")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefPt {
    @Schema(description = "Latitude in decimal degrees", example = "39.7392", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("latitude")
    private Double latitude;

    @Schema(description = "Longitude in decimal degrees", example = "-104.9903", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonProperty("longitude")
    private Double longitude;
}
