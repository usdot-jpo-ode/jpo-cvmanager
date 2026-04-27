package us.dot.its.jpo.ode.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.springframework.stereotype.Service;

import us.dot.its.jpo.ode.api.models.emails.EmailCategory;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailFrequency;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;
import us.dot.its.jpo.ode.api.emails.generators.ApiErrorEmailGenerator;
import us.dot.its.jpo.ode.api.repositories.UserEmailNotificationRepository;
import us.dot.its.jpo.ode.api.emails.generators.IntersectionNotificationSummaryEmailGenerator;
import us.dot.its.jpo.ode.api.emails.generators.RsuErrorSummaryEmailGenerator;
import us.dot.its.jpo.ode.api.emails.generators.SupportRequestEmailGenerator;
import us.dot.its.jpo.ode.api.emails.providers.EmailProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailProvider emailProvider;
    private final UserEmailNotificationRepository userEmailNotificationRepository;
    private final IntersectionNotificationSummaryEmailGenerator intersectionNotificationSummaryEmailGenerator;
    private final PermissionService permissionService;
    private final SupportRequestEmailGenerator supportRequestEmailGenerator;
    private final ApiErrorEmailGenerator apiErrorEmailGenerator;
    private final RsuErrorSummaryEmailGenerator rsuErrorSummaryEmailGenerator;

    public void sendEmails(List<EmailRecipient> recipients, EmailContent content) {
        emailProvider.sendBatchedEmails(recipients, content);
    }

    public List<EmailRecipient> getUsersForNotificationType(EmailCategory category, EmailFrequency frequency) {
        return userEmailNotificationRepository
                .findUsersByNotificationType(category.getCategoryKey(), frequency.toString()).stream()
                .map(email -> new EmailRecipient(email, null))
                .toList();
    }

    public List<EmailRecipient> getUsersForNotificationTypeByRsu(EmailCategory category, String rsuIp,
            EmailFrequency frequency) {
        try {
            return userEmailNotificationRepository
                    .findUsersByNotificationTypeAndRsu(category.getCategoryKey(), frequency.toString(),
                            InetAddress.getByName(rsuIp))
                    .stream()
                    .map(email -> new EmailRecipient(email, null))
                    .toList();
        } catch (UnknownHostException e) {
            log.error("Invalid RSU IP address: {}", rsuIp, e);
            return Collections.emptyList();
        }
    }

    public List<EmailRecipient> getUsersForNotificationTypeByOrganization(EmailCategory category, String orgName,
            EmailFrequency frequency) {
        return userEmailNotificationRepository
                .findUsersByNotificationTypeAndOrganization(category.getCategoryKey(), frequency.toString(), orgName)
                .stream()
                .map(email -> new EmailRecipient(email, null))
                .toList();
    }

    public List<EmailSendResponse> sendIntersectionNotificationSummaryEmailSendResponses(
            IntersectionNotificationSummaryEmailContents data) {
        EmailContent content = intersectionNotificationSummaryEmailGenerator.generateEmailBody(data);
        List<EmailRecipient> recipients = getUsersForNotificationType(EmailCategory.INTERSECTION_NOTIFICATION_SUMMARY,
                EmailFrequency.IMMEDIATE);
        ArrayList<EmailRecipient> newRecipients = new ArrayList<>(recipients);
        return emailProvider.sendBatchedEmails(newRecipients, content);
    }

    public List<EmailSendResponse> sendSupportRequest(SupportRequestEmailContents data) {
        EmailContent content = supportRequestEmailGenerator.generateEmailBody(data);
        List<EmailRecipient> recipients = getUsersForNotificationType(EmailCategory.SUPPORT_REQUEST,
                EmailFrequency.IMMEDIATE);
        return emailProvider.sendBatchedEmails(recipients, content);
    }

    public List<EmailSendResponse> sendApiError(ApiErrorEmailContents data) {
        EmailContent content = apiErrorEmailGenerator.generateEmailBody(data);
        List<EmailRecipient> recipients = getUsersForNotificationType(EmailCategory.CRITICAL_ERROR_MESSAGE,
                EmailFrequency.IMMEDIATE);
        return emailProvider.sendBatchedEmails(recipients, content);
    }

    public List<EmailSendResponse> sendRsuErrorSummary(RsuErrorSummaryEmailContents data) {
        EmailContent content = rsuErrorSummaryEmailGenerator.generateEmailBody(data);
        String email = permissionService.getCvManagerAuthToken().getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Unable to send RSU error summary: authenticated user token does not contain a valid email");
            throw new IllegalArgumentException("Authenticated user email is missing or blank");
        }
        List<EmailRecipient> recipients = List.of(new EmailRecipient(email, ""));
        return emailProvider.sendBatchedEmails(recipients, content);
    }
}