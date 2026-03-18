package us.dot.its.jpo.ode.api.models.emails;

import java.util.List;

import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailSendResponse {
    private Integer statusCode;
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
    public static ResponseEntity<String> getCombinedResponseEntity(List<EmailSendResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return ResponseEntity.ok("No emails to send");
        }

        // Separate successful and failed responses
        List<EmailSendResponse> successfulResponses = responses.stream()
                .filter(resp -> resp != null && resp.getMappedStatusCode() == 200)
                .toList();

        List<EmailSendResponse> failedResponses = responses.stream()
                .filter(resp -> resp != null && resp.getMappedStatusCode() != 200)
                .toList();

        int totalCount = responses.size();
        int successCount = successfulResponses.size();
        int failureCount = failedResponses.size();

        // All succeeded
        if (failureCount == 0) {
            return ResponseEntity.ok(
                    String.format("Successfully sent %d email(s)", successCount));
        }

        // All failed
        if (successCount == 0) {
            String errorMessages = failedResponses.stream()
                    .map(EmailSendResponse::getMessage)
                    .collect(java.util.stream.Collectors.joining("; "));
            return ResponseEntity.status(500)
                    .body(String.format("Failed to send %d email(s): %s", failureCount, errorMessages));
        }

        // Partial success - use 207 Multi-Status
        String errorMessages = failedResponses.stream()
                .map(EmailSendResponse::getMessage)
                .collect(java.util.stream.Collectors.joining("; "));

        return ResponseEntity.status(207)
                .body(String.format(
                        "Sent %d of %d email(s) successfully. %d failed: %s",
                        successCount, totalCount, failureCount, errorMessages));
    }
}
