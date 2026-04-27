package us.dot.its.jpo.ode.api.models.emails;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailApiResponse {
    @Schema(description = "List of individual email send responses")
    private List<EmailSendResponse> responses;
    @Schema(description = "Number of successfully sent emails")
    private int successCount;
    @Schema(description = "Number of failed email sends")
    private int failureCount;
}
