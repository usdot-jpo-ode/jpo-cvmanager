package us.dot.its.jpo.ode.api.emails.providers;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.data.model.message.MessageResponse;
import com.postmarkapp.postmark.client.exception.PostmarkException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;

@Slf4j
@Component
@ConditionalOnProperty(name = "email.broker", havingValue = "POSTMARK")
@RequiredArgsConstructor
public class EmailProviderPostmark implements EmailProvider {

    private final EmailProperties emailProperties;
    private final UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    private final ApiClient postmark;

    @Override
    public List<EmailSendResponse> sendBatchedEmails(List<EmailRecipient> recipients, EmailContent content) {
        try {
            List<Message> messages = recipients.stream().map(r -> getMessage(r, content)).toList();
            List<MessageResponse> responses = postmark.deliverMessage(messages);
            return responses.stream().map(r -> new EmailSendResponse(r.getErrorCode(), r.getMessage())).toList();
        } catch (IllegalStateException e) {
            // Unsubscribe URL generation failed - don't send any emails in batch
            log.error("Cannot send batch emails due to unsubscribe URL generation failure: {}", e.getMessage());
            return recipients.stream()
                    .map(r -> new EmailSendResponse(500, "Failed to generate unsubscribe URL"))
                    .toList();
        } catch (PostmarkException | IOException e) {
            log.error("Exception sending postmark email batch", e);
            return recipients.stream()
                    .map(r -> new EmailSendResponse(500, "Internal Server Error"))
                    .toList();
        }
    }

    /**
     * Constructs a Message object for sending via Postmark.
     * 
     * @param recipient The email recipient
     * @param content   The email content
     * @return Constructed Message object
     * @throws IllegalStateException if unsubscribe URL generation fails (indicates
     *                               system misconfiguration)
     */
    private Message getMessage(EmailRecipient recipient, EmailContent content) {
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

        Message message = new Message(
                emailProperties.getSenderAddress(),
                recipient.getEmail(),
                content.getSubject(),
                htmlText);

        // Add the List-Unsubscribe header (required for CAN-SPAM compliance)
        message.addHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">");

        return message;
    }

    private String replacePlaceholders(String htmlContents, String unsubscribeUrl) {
        return htmlContents.replaceAll("\\{\\{unsubscribe_url\\}\\}", unsubscribeUrl).replaceAll("\n", "<br>");
    }

    private String getUnsubscribeUrl(String email) {
        return unsubscribeTokenGenerator.generateUnsubscribeUrl(email);
    }
}
