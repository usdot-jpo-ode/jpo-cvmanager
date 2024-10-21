package us.dot.its.jpo.ode.api.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.exception.PostmarkException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.EmailFrequency;
import us.dot.its.jpo.ode.api.models.EmailSettings;

@Service
public class EmailService{

    @Autowired
    private JavaMailSender mailSender;

    private SendGrid sendGrid;

    @Autowired
    private ApiClient postmark; 

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    ConflictMonitorApiProperties props;

    @Autowired
    public EmailService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
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
                Response response = this.sendGrid.api(request);
                System.out.println("Message Sent via Sendgrid to:" + to);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    public void sendEmailViaPostmark(String to, String subject, String text){

        String htmlText = text.replaceAll("\n", "<br>");

        Message message = new Message(
                props.getEmailFromAddress(),
                to,
                subject,
                htmlText);
        try {
            postmark.deliverMessage(message);
            System.out.println("Message Sent via Postmark to:" + to);
        } catch (PostmarkException | IOException e) {
            System.out.println("Unable to send message" + e);
        }
    }

    public void sendEmailViaSpringMail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        System.out.println("Message Sent Via SMTP to: " + to);
    }



    public void sendSimpleMessage(String to, String subject, String text) {
        System.out.println("Sending Simple Message");
        if(props.getEmailBroker().equals("sendgrid")){
            sendEmailViaSendGrid(to, subject, text);
        }else if (props.getEmailBroker().equals("postmark")){
            sendEmailViaPostmark(to, subject, text);
        }
        else{
            sendEmailViaSpringMail(to, subject, text);
        }
    }

    public void emailList(List<UserRepresentation> users, String subject, String text){
        for(UserRepresentation user : users){
            if(user.getEmail() != null){
                sendSimpleMessage(user.getEmail(), subject, text);
            }
            
        }
    }

    // Gets Users based upon a Single Group requirement, and notification type
    public List<UserRepresentation> getSimpleEmailList(String notificationType, String emailGroup, EmailFrequency frequency){
        ArrayList<String> notificationTypes = new ArrayList<>();
        notificationTypes.add(notificationType);

        ArrayList<String> emailGroups = new ArrayList<>();
        emailGroups.add(emailGroup);

        ArrayList<EmailFrequency> emailFrequencies = new ArrayList<>();
        emailFrequencies.add(frequency);

        return getEmailList(notificationTypes, emailGroups, emailFrequencies);

    }

    // Gets Users based upon a Notification Frequency Only
    public List<UserRepresentation> getNotificationEmailList(EmailFrequency frequency){
        ArrayList<String> notificationTypes = new ArrayList<>();
        notificationTypes.add("notification");

        ArrayList<String> emailGroups = new ArrayList<>();
        emailGroups.add("ADMIN");
        emailGroups.add("USER");

        ArrayList<EmailFrequency> emailFrequencies = new ArrayList<>();
        emailFrequencies.add(frequency);

        return getEmailList(notificationTypes, emailGroups, emailFrequencies);

    }

    // Gets users based upon multiple groups or notification types. A user is accepted if they are apart of at least 1 correct group, and have at least 1 correct notification type.
    public List<UserRepresentation> getEmailList(List<String> notificationTypes, List<String> emailGroups, List<EmailFrequency> frequency){

        // Get all Users
        Set<UserRepresentation> users = new HashSet<>();
        List<UserRepresentation> emailList = new ArrayList<>();


        // This is a workaround to get around some broken keycloak API calls. Hopefully future versions of keycloak will fix this.
        for(String group : emailGroups){
            for(GroupRepresentation groupRep : keycloak.realm(realm).groups().groups()){
                if(group.equals(groupRep.getName())){
                    users.addAll(keycloak.realm(realm).groups().group(groupRep.getId()).members());
                }
            }
        }
        

        for(UserRepresentation user : users){
            
            Map<String, List<String>> attributes = user.getAttributes();

            EmailSettings settings = EmailSettings.fromAttributes(attributes);


            

            // Skip if user has no attributes
            if(attributes == null){
                continue;
            }

            boolean shouldReceive = false;
            for(String notificationType : notificationTypes){
                if(notificationType == "annoncements" && settings.isReceiveAnnouncements()){
                    shouldReceive = true;
                }else if (notificationType == "ceaseBroadcastRecommendations" && settings.isReceiveCeaseBroadcastRecommendations()){
                    shouldReceive = true;
                }else if(notificationType == "criticalErrorMessages" && settings.isReceiveCriticalErrorMessages()){
                    shouldReceive = true;
                }else if(notificationType == "receiveNewUserRequests" && settings.isReceiveNewUserRequests()){
                    shouldReceive = true;
                }else if(notificationType == "notification" && settings.getNotificationFrequency() != EmailFrequency.NEVER){
                    for(EmailFrequency freq : frequency){
                        if(settings.getNotificationFrequency() == freq){
                            shouldReceive = true;
                        }
                    }
                }
            }

            if (!shouldReceive){
                continue;
            }

            // Add the user
            emailList.add(user);

            
        }

        System.out.println("Returning" + emailList.size() + "Users");

        return emailList;

    }
}