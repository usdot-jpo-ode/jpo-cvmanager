package us.dot.its.jpo.ode.api.emails.providers;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;

@Slf4j
@Component
@ConditionalOnProperty(name = "email.broker", havingValue = "SENDGRID")
@RequiredArgsConstructor
public class EmailProviderSendGrid implements EmailProvider {

    private final EmailProperties emailProperties;
    private final UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    private final SendGrid sendGrid;

    @Override
    public List<EmailSendResponse> sendBatchedEmails(List<EmailRecipient> recipients, EmailContent content) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients provided for email batch. No emails will be sent.");
            return List.of();
        }
        try {
            log.info("Sending SendGrid Batched Emails to {} recipients", recipients.size());
            Mail mail = getMail(recipients.get(0), content);
            recipients.stream().forEach(r -> mail.addPersonalization(getPersonalization(r)));

            Response response = sendGrid.api(generateRequest(mail));
            return List.of(new EmailSendResponse(response.getStatusCode(), response.getBody()));
        } catch (IllegalStateException e) {
            // Unsubscribe URL generation failed - don't send any emails in batch
            log.error("Cannot send batch emails due to unsubscribe URL generation failure: {}", e.getMessage());
            return recipients.stream()
                    .map(r -> new EmailSendResponse(500, "Failed to generate unsubscribe URL"))
                    .toList();
        } catch (IOException e) {
            log.error("Exception sending sendgrid email batch", e);
            return recipients.stream()
                    .map(r -> new EmailSendResponse(500, "Internal Server Error"))
                    .toList();
        }
    }

    private Mail getMail(EmailRecipient recipient, EmailContent content) {
        Email fromEmail = new Email(emailProperties.getSenderAddress());
        Content sendGridContent = new Content("text/html", content.getBody());

        return new Mail(
                fromEmail,
                content.getSubject(),
                recipient.toSendGridEmail(),
                sendGridContent);
    }

    /**
     * Creates a Personalization object with unsubscribe URL for SendGrid.
     * 
     * @param recipient The email recipient
     * @return Personalization object with recipient and unsubscribe URL
     * @throws IllegalStateException if unsubscribe URL generation fails (indicates
     *                               system misconfiguration)
     */
    private Personalization getPersonalization(EmailRecipient recipient) {
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

        Personalization personalization = new Personalization();
        personalization.addTo(recipient.toSendGridEmail());
        personalization.addDynamicTemplateData("unsubscribe_url", unsubscribeUrl);
        personalization.addHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">");
        return personalization;
    }

    private Request generateRequest(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return request;
    }

    private String getUnsubscribeUrl(String email) {
        return unsubscribeTokenGenerator.generateUnsubscribeUrl(email);
    }
}
