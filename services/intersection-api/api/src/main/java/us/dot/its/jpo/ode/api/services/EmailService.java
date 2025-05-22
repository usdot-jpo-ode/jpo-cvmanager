package us.dot.its.jpo.ode.api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.io.IOException;
import java.util.*;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.exception.PostmarkException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.EmailFrequency;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SendGrid sendGrid;
    private final ApiClient postmark;
    private final ConflictMonitorApiProperties props;

    @Autowired
    public EmailService(JavaMailSender mailSender, SendGrid sendGrid, ApiClient postmark,
            ConflictMonitorApiProperties props) {
        this.mailSender = mailSender;
        this.sendGrid = sendGrid;
        this.postmark = postmark;
        this.props = props;
    }

    public void sendEmailViaSendGrid(String to, String subject, String text) {
        Email fromEmail = new Email(props.getEmailFromAddress());
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            this.sendGrid.api(request);
        } catch (IOException e) {
            log.error("Exception sending sendgrid email", e);
        }
    }

    public void sendEmailViaPostmark(String to, String subject, String text) {

        String htmlText = text.replaceAll("\n", "<br>");

        Message message = new Message(
                props.getEmailFromAddress(),
                to,
                subject,
                htmlText);
        try {
            postmark.deliverMessage(message);
        } catch (PostmarkException | IOException e) {
            log.error("Exception sending postmark email", e);
        }
    }

    public void sendEmailViaSpringMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        if (props.getEmailBroker().equals("sendgrid")) {
            sendEmailViaSendGrid(to, subject, text);
        } else if (props.getEmailBroker().equals("postmark")) {
            sendEmailViaPostmark(to, subject, text);
        } else {
            sendEmailViaSpringMail(to, subject, text);
        }
    }

    public void emailList(List<UserRepresentation> users, String subject, String text) {
        for (UserRepresentation user : users) {
            if (user.getEmail() != null) {
                sendSimpleMessage(user.getEmail(), subject, text);
            }

        }
    }

    // Gets Users based upon a Notification Frequency Only
    public List<UserRepresentation> getNotificationEmailList(EmailFrequency frequency) {
        // TODO: Pull email list from Postgres
        return new ArrayList<>();
    }
}