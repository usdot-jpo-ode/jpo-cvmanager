package us.dot.its.jpo.ode.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RestController
@ConditionalOnProperty(name = { "enable.api", "enable.email" }, havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @Operation(summary = "Intersection Notification Summary", description = "Sends an email with a summary of intersection notifications.")
    @RequestMapping(value = "/send-intersection-notification-summary", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All emails sent successfully"),
            @ApiResponse(responseCode = "207", description = "Partial success - some emails sent, some failed"),
            @ApiResponse(responseCode = "400", description = "Invalid message body"),
            @ApiResponse(responseCode = "500", description = "All emails failed to send"),
    })
    public @ResponseBody ResponseEntity<String> sendIntersectionNotificationSummaryEmails(
            @RequestBody IntersectionNotificationSummaryEmailContents body) {

        return EmailSendResponse
                .getCombinedResponseEntity(emailService.sendIntersectionNotificationSummaryEmailSendResponses(body));
    }
}