package us.dot.its.jpo.ode.api.models.postgres.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for firmware upgrade check response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirmwareUpgradeCheckResponseDto implements Serializable {
    @Schema(description = "Whether a firmware upgrade is available for the RSU", example = "true")
    @JsonProperty("upgrade_available")
    @NotNull
    Boolean upgradeAvailable;

    @Schema(description = "ID of the available upgrade firmware image", example = "1")
    @JsonProperty("upgrade_id")
    @NotNull
    Long upgradeId;

    @Schema(description = "Name of the available upgrade firmware image", example = "firmware-v2.0")
    @JsonProperty("upgrade_name")
    @NotNull
    String upgradeName;

    @Schema(description = "Version of the available upgrade firmware", example = "2.0.0")
    @JsonProperty("upgrade_version")
    @NotNull
    String upgradeVersion;
}
