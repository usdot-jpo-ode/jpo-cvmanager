package us.dot.its.jpo.ode.api.models.emails.contents;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Contents of RSU error summary email, including a list of recipient email addresses, email subject, and email message body that contains details about recent RSU errors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RsuErrorSummaryEmailContents {
    @Schema(description = "Email subject")
    @JsonProperty("subject")
    @NotEmpty(message = "Email subject cannot be empty")
    @NotNull(message = "Email subject cannot be null")
    private String subject;
    @Schema(description = "Email message body")
    @JsonProperty("message")
    @NotEmpty(message = "Email message cannot be empty")
    @NotNull(message = "Email message cannot be null")
    private String message;
}
