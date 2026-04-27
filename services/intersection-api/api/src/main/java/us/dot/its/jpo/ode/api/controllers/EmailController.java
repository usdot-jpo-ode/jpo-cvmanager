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
import us.dot.its.jpo.ode.api.models.emails.EmailApiResponse;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;
import us.dot.its.jpo.ode.api.services.EmailService;
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
    @RequestMapping(value = "/intersection-notifications", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All emails sent successfully"),
            @ApiResponse(responseCode = "207", description = "Partial success - some emails sent, some failed"),
            @ApiResponse(responseCode = "400", description = "Invalid message body"),
            @ApiResponse(responseCode = "500", description = "All emails failed to send"),
    })
    public @ResponseBody EmailApiResponse sendIntersectionNotificationSummaryEmails(
            @RequestBody IntersectionNotificationSummaryEmailContents body) {

        return EmailSendResponse
                .getCombinedResponseEntity(emailService.sendIntersectionNotificationSummaryEmailSendResponses(body));
    }

    @Operation(summary = "API Error Summary", description = "Sends an email with a summary of API errors.")
    @RequestMapping(value = "/api-errors", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || hasRole('ROLE_SEND_CRITICAL_ERROR_MESSAGE_EMAILS')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "207", description = "Partial success - some emails sent, some failed"),
            @ApiResponse(responseCode = "400", description = "Invalid message body"),
    })
    public @ResponseBody EmailApiResponse sendApiErrorEmails(
            @RequestBody ApiErrorEmailContents body) {

        return EmailSendResponse.getCombinedResponseEntity(emailService.sendApiError(body));
    }

    @Operation(summary = "Rsu Error Summary", description = "Sends an email with a summary of RSU errors.")
    @RequestMapping(value = "/rsu-errors", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "207", description = "Partial success - some emails sent, some failed"),
            @ApiResponse(responseCode = "400", description = "Invalid message body"),
    })
    public @ResponseBody EmailApiResponse sendRsuErrorSummaryEmails(
            @RequestBody RsuErrorSummaryEmailContents body) {

        return EmailSendResponse.getCombinedResponseEntity(emailService.sendRsuErrorSummary(body));
    }

    @Operation(summary = "Send Support Request Email", description = "Send a support request email")
    @RequestMapping(value = "/support-requests", method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "207", description = "Partial success - some emails sent, some failed"),
            @ApiResponse(responseCode = "400", description = "Invalid message body"),
    })
    public @ResponseBody EmailApiResponse sendSupportRequestEmails(
            @RequestBody SupportRequestEmailContents body) {

        return EmailSendResponse.getCombinedResponseEntity(emailService.sendSupportRequest(body));
    }
}