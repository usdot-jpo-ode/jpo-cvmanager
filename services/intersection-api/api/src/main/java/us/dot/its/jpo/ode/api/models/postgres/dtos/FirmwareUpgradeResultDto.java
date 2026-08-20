package us.dot.its.jpo.ode.api.models.postgres.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for individual RSU firmware upgrade result in start response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirmwareUpgradeResultDto implements Serializable {
    @Schema(description = "HTTP status code for this RSU's upgrade operation", example = "200")
    @NotNull
    Integer code;

    @Schema(description = "Result data or error message for this RSU", example = "{\"message\": \"Upgrade started\"}")
    Object data;
}
