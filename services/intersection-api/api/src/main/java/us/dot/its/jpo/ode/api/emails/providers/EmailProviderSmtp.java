package us.dot.its.jpo.ode.api.emails.providers;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;

@Slf4j
@Component
@ConditionalOnProperty(name = "email.broker", havingValue = "SMTP", matchIfMissing = true)
@RequiredArgsConstructor
public class EmailProviderSmtp implements EmailProvider {

    private final EmailProperties emailProperties;
    private final UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    private final JavaMailSender mailSender;

    @Override
    public List<EmailSendResponse> sendBatchedEmails(List<EmailRecipient> recipients, EmailContent content) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients provided for email batch. No emails will be sent.");
            return List.of();
        }
        try {
            log.info("Sending SMTP Batched Emails to: {}",
                    String.join(", ", recipients.stream().map(r -> r.getEmail()).toList()));
            MimeMessage[] messages = recipients.stream().map(r -> getMessage(r, content)).filter((v) -> v != null)
                    .toArray(MimeMessage[]::new);
            mailSender.send(messages);
            return List.of(new EmailSendResponse(200, "Emails sent successfully: " + recipients.size()));
        } catch (IllegalStateException e) {
            // Unsubscribe URL generation failed - don't send any emails in batch
            log.error("Cannot send batch emails due to unsubscribe URL generation failure: {}", e.getMessage());
            return recipients.stream()
                    .map(r -> new EmailSendResponse(500, "Failed to generate unsubscribe URL"))
                    .toList();
        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("SMTP authentication failed for batch: {}", e.getMessage());
            return List.of(new EmailSendResponse(500, "SMTP authentication failed"));
        } catch (org.springframework.mail.MailSendException e) {
            log.error("Failed to send batch emails: {}", e.getMessage());
            return List.of(new EmailSendResponse(500, "Failed to send batch emails"));
        } catch (Exception e) {
            log.error("Unexpected error sending batch emails: {}", e.getMessage());
            return List.of(new EmailSendResponse(500, "Unknown error"));
        }
    }

    /**
     * Constructs a MimeMessage for sending via SMTP.
     * 
     * @param recipient The email recipient
     * @param content   The email content
     * @return Constructed MimeMessage
     * @throws IllegalStateException if unsubscribe URL generation fails (indicates
     *                               system misconfiguration)
     */
    private MimeMessage getMessage(EmailRecipient recipient, EmailContent content) {
        String unsubscribeUrl;
        try {
            unsubscribeUrl = getUnsubscribeUrl(recipient.getEmail());
            if (unsubscribeUrl == null || unsubscribeUrl.isEmpty()) {
                throw new IllegalStateException("Unsubscribe URL generation returned null or empty");
            }
        } catch (Exception e) {
            log.error("Failed to generate unsubscribe URL for email: {}. Email will not be sent.", recipient.getEmail(),
                    e);
            throw new IllegalStateException("Cannot send email without valid unsubscribe URL (CAN-SPAM compliance)", e);
        }

        String htmlText = replacePlaceholders(content.getBody(), unsubscribeUrl);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getSenderAddress());
            helper.setTo(recipient.getEmail());
            helper.setSubject(content.getSubject());
            helper.setSentDate(new java.util.Date());
            helper.setText(htmlText, true); // true = HTML content

            return message;
        } catch (MessagingException e) {
            log.error("Failed to create email message for {}: {}", recipient.getEmail(), e.getMessage());
            return null;
        }
    }

    private String replacePlaceholders(String htmlContents, String unsubscribeUrl) {
        return htmlContents.replaceAll("\\{\\{unsubscribe_url\\}\\}", unsubscribeUrl);
    }

    private String getUnsubscribeUrl(String email) {
        return unsubscribeTokenGenerator.generateUnsubscribeUrl(email);
    }
}
