package us.dot.its.jpo.ode.api.models.emails;

import java.util.List;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailSendResponse {
    @Schema(description = "HTTP status code of the email send response")
    private Integer statusCode;
    @Schema(description = "Message detailing the result of the email send attempt")
    private String message;

    public ResponseEntity<String> getResponseEntity() {
        return ResponseEntity.status(getMappedStatusCode()).body(this.message);
    }

    private Integer getMappedStatusCode() {
        // Postmark APIs can use 0 for success
        if (statusCode.equals(0))
            return 200;
        return statusCode;
    }

    /**
     * Combines multiple email send responses into a single HTTP response.
     * Uses smart status code logic:
     * - 200 OK: All emails sent successfully
     * - 207 Multi-Status: Some succeeded, some failed (partial success)
     * - 500 Internal Server Error: All emails failed
     * 
     * @param responses List of individual email send responses
     * @return Combined ResponseEntity with appropriate status code and detailed
     *         message
     */
    public static EmailApiResponse getCombinedResponseEntity(List<EmailSendResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return new EmailApiResponse(List.of(), 0, 0);
        }

        responses = responses.stream().filter(resp -> resp != null).toList();

        // Separate successful and failed responses
        List<EmailSendResponse> successfulResponses = responses.stream()
                .filter(resp -> resp.getMappedStatusCode() == 200)
                .toList();

        List<EmailSendResponse> failedResponses = responses.stream()
                .filter(resp -> resp.getMappedStatusCode() != 200)
                .toList();

        int successCount = successfulResponses.size();
        int failureCount = failedResponses.size();

        EmailApiResponse apiResponse = new EmailApiResponse(responses, successCount, failureCount);

        // All succeeded
        if (failureCount == 0) {
            return apiResponse;
        }

        // All failed
        if (successCount == 0) {
            throw EmailResponseException.internalServerError(apiResponse);
        }

        throw EmailResponseException.multiStatus(apiResponse);
    }
}
