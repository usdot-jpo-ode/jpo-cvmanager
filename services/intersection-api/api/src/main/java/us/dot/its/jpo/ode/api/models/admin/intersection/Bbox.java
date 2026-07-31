package us.dot.its.jpo.ode.api.models.admin.intersection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Axis-aligned bounding box defined by two WGS-84 corners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bbox {
    @Schema(description = "Latitude of the first corner", example = "39.74")
    @NotNull
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    @JsonProperty("latitude1")
    private Double latitude1;

    @Schema(description = "Longitude of the first corner", example = "-104.99")
    @NotNull
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    @JsonProperty("longitude1")
    private Double longitude1;

    @Schema(description = "Latitude of the second corner", example = "39.73")
    @NotNull
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    @JsonProperty("latitude2")
    private Double latitude2;

    @Schema(description = "Longitude of the second corner", example = "-104.98")
    @NotNull
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    @JsonProperty("longitude2")
    private Double longitude2;
}
