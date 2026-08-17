package us.dot.its.jpo.ode.api.models.scms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Response wrapper for SCMS health status endpoint.
 * <p>Using a dedicated response class (rather than returning {@code Map} directly) provides:</p>
 * <ul>
 *   <li>Clear API contract with explicit field names</li>
 *   <li>Easier extensibility if additional response fields are needed</li>
 *   <li>Better OpenAPI/Swagger documentation</li>
 * </ul>
 */
@Schema(description = "Response containing SCMS health records for RSUs in an organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScmsHealthResponse {

    @Schema(description = "Map of RSU IPv4 addresses to their SCMS health status. " +
            "Null values indicate RSUs with no health record available.")
    private Map<String, ScmsHealthDto> scmsHealthByIp;
}

