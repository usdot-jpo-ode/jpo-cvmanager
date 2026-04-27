package us.dot.its.jpo.ode.api.models.emails.contents;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorEmailContents {
    @JsonProperty("error_message")
    private String errorMessage;
    @JsonProperty("stack_trace")
    private String stackTrace;
    @JsonProperty("timestamp")
    private Instant timestamp;
    @JsonProperty("logs_link")
    private String logsLink;
}
