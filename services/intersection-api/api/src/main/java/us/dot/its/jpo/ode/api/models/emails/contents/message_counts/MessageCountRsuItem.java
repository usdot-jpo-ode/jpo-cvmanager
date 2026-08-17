package us.dot.its.jpo.ode.api.models.emails.contents.message_counts;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Message counts for a specific RSU, broken up by message type")
@Data
public class MessageCountRsuItem {
    @Schema(description = "IP address of the RSU", example = "192.168.1.1")
    @JsonProperty("rsu_ip")
    @NotEmpty(message = "RSU IP cannot be empty")
    @NotNull(message = "RSU IP cannot be null")
    private String rsuIp;
    @Schema(description = "Primary route associated with the RSU", example = "C470")
    @JsonProperty("primary_route")
    @NotEmpty(message = "Primary route cannot be empty")
    @NotNull(message = "Primary route cannot be null")
    private String primaryRoute;
    @Schema(description = "Message counts by message type for the RSU")
    @JsonProperty("counts")
    @NotEmpty(message = "Message counts by type cannot be empty")
    @NotNull(message = "Message counts by type cannot be null")
    private Map<String, MessageCountCountsItem> messageCountsByType;
}
