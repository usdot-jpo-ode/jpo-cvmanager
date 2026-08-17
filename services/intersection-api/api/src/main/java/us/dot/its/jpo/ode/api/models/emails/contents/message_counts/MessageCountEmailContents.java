package us.dot.its.jpo.ode.api.models.emails.contents.message_counts;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Contents of message count summary email")
@Data
public class MessageCountEmailContents {
    @Schema(description = "Name of the organization the message counts were collected for", example = "CDOT-CV")
    @JsonProperty("org_name")
    @NotEmpty(message = "Organization name cannot be empty")
    @NotNull(message = "Organization name cannot be null")
    private String organizationName;
    @Schema(description = "Title of the deployment the message counts were collected for", example = "CDOT-OIM-CV-DEV")
    @JsonProperty("deployment_title")
    @NotEmpty(message = "Deployment title cannot be empty")
    @NotNull(message = "Deployment title cannot be null")
    private String deploymentTitle;
    @Schema(description = "Start date of the message count aggregation period")
    @JsonProperty("start_date")
    @NotNull(message = "Start date cannot be null")
    private Instant startDate;
    @Schema(description = "End date of the message count aggregation period")
    @JsonProperty("end_date")
    @NotNull(message = "End date cannot be null")
    private Instant endDate;

    @Schema(description = "List of message types included in the message counts", example = "[\"BSM\", \"SPaT\", \"MAP\"]")
    @JsonProperty("message_type_list")
    @NotEmpty(message = "Message type list cannot be empty")
    @NotNull(message = "Message type list cannot be null")
    private List<String> messageTypeList;

    @Schema(description = "RSU message counts by message type and RSU")
    @JsonProperty("rsu_counts")
    @NotEmpty(message = "RSU counts cannot be empty")
    @NotNull(message = "RSU counts cannot be null")
    private List<MessageCountRsuItem> rsuCounts;
}
