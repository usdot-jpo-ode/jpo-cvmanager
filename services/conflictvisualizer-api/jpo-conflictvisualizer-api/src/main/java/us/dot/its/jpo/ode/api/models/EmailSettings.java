package us.dot.its.jpo.ode.api.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailSettings {
    public boolean receiveAnnouncements;
    public boolean receiveCeaseBroadcastRecommendations;
    public boolean receiveCriticalErrorMessages;
    public boolean receiveNewUserRequests;
    public EmailFrequency notificationFrequency;

    private static final Logger logger = LoggerFactory.getLogger(EmailSettings.class);

    public EmailSettings(){
        this.receiveAnnouncements = true;
        this.receiveCeaseBroadcastRecommendations = true;
        this.receiveCriticalErrorMessages = true;
        this.receiveNewUserRequests = true;
        this.notificationFrequency = EmailFrequency.ALWAYS;
    }

    public static EmailSettings fromAttributes(Map<String, List<String>> attributes){
        

        List<String> notifications = attributes.get("NotificationSettings");
        
        if(notifications != null && notifications.size() > 0){
            ObjectMapper mapper = DateJsonMapper.getInstance();
            // mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            EmailSettings settings;
            try {
                settings = mapper.readValue(notifications.get(0), EmailSettings.class);
                return settings;
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }


        System.out.println("No Settings found Returning Default");
        return new EmailSettings(); 
    }


    public Map<String, List<String>> toAttributes(){
        Map<String, List<String>> attributes = new HashMap<>();

        List<String> notifications = new ArrayList<>();
        // List<String> notificationFrequency = new ArrayList<>();
        notifications.add(this.toString());
        attributes.put("NotificationSettings", notifications);

        return attributes;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = DateJsonMapper.getInstance();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Exception serializing JSON", e);
        }
        return "";
    }




    public boolean isReceiveAnnouncements() {
        return receiveAnnouncements;
    }




    public void setReceiveAnnouncements(boolean receiveAnnouncements) {
        this.receiveAnnouncements = receiveAnnouncements;
    }




    public boolean isReceiveCeaseBroadcastRecommendations() {
        return receiveCeaseBroadcastRecommendations;
    }




    public void setReceiveCeaseBroadcastRecommendations(boolean receiveCeaseBroadcastRecommendations) {
        this.receiveCeaseBroadcastRecommendations = receiveCeaseBroadcastRecommendations;
    }




    public boolean isReceiveCriticalErrorMessages() {
        return receiveCriticalErrorMessages;
    }




    public void setReceiveCriticalErrorMessages(boolean receiveCriticalErrorMessages) {
        this.receiveCriticalErrorMessages = receiveCriticalErrorMessages;
    }




    public boolean isReceiveNewUserRequests() {
        return receiveNewUserRequests;
    }




    public void setReceiveNewUserRequests(boolean receiveNewUserRequests) {
        this.receiveNewUserRequests = receiveNewUserRequests;
    }




    public EmailFrequency getNotificationFrequency() {
        return notificationFrequency;
    }




    public void setNotificationFrequency(EmailFrequency notificationFrequency) {
        this.notificationFrequency = notificationFrequency;
    }




    public static Logger getLogger() {
        return logger;
    }

}
