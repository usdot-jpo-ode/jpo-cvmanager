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
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.repositories.UserEmailNotificationRepository;
import us.dot.its.jpo.ode.api.emails.generators.IntersectionNotificationSummaryEmailGenerator;
import us.dot.its.jpo.ode.api.emails.providers.EmailProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailProvider emailProvider;
    private final UserEmailNotificationRepository userEmailNotificationRepository;
    private final IntersectionNotificationSummaryEmailGenerator intersectionNotificationSummaryEmailGenerator;

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
}