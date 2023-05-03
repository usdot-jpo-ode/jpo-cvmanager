package us.dot.its.jpo.ode.api.models;

public class EmailSettings {
    public boolean recieveAnnoncements = true;
    public boolean receiveCeaseBroadcastRecommendations = true;
    public boolean receiveCriticalErrorMessages = true;
    public boolean receiveNewUserRequests = true;
    public EmailFrequency notificationFrequency = EmailFrequency.ALWAYS;
}
