package us.dot.its.jpo.ode.api;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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

import us.dot.its.jpo.ode.api.models.EmailSettings;

@Service
public class EmailService{

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        System.out.println("Message Sent to: " + to);
    }

    public void emailList(List<UserRepresentation> users, String subject, String text){
        for(UserRepresentation user : users){
            sendSimpleMessage(user.getEmail(), subject, text);
        }
    }

    // Gets Users based upon a Single Group requirement, and notification type
    public List<UserRepresentation> getSimpleEmailList(String notificationType, String emailGroup){
        ArrayList<String> notificationTypes = new ArrayList<>();
        notificationTypes.add(notificationType);

        ArrayList<String> emailGroups = new ArrayList<>();
        emailGroups.add(emailGroup);

        return getEmailList(notificationTypes, emailGroups);

    }

    // Gets users based upon multiple groups or notification types. A user is accepted if they are apart of at least 1 correct group, and have at least 1 correct notification type.
    public List<UserRepresentation> getEmailList(List<String> notificationTypes, List<String> emailGroups){

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
            System.out.println("User: "+ user.getUsername());
            
            Map<String, List<String>> attributes = user.getAttributes();

            EmailSettings settings = EmailSettings.fromAttributes(attributes);


            

            // Skip if user has no attributes
            if(attributes == null){
                continue;
            }

            List<String> notifyOn = attributes.get("notifications");

            // Skip if User has no notification settings
            if(notifyOn == null || notifyOn.size() <= 0){
                System.out.println("Skipping because notification list is empty");
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