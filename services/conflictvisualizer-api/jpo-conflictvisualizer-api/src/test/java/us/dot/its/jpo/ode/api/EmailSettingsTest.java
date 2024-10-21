package us.dot.its.jpo.ode.api;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.dot.its.jpo.ode.api.models.EmailSettings;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailSettingsTest {

    @Test
    public void testEmailAttributesEncodeDecode() {
        
        EmailSettings settings = new EmailSettings();

        settings.setReceiveCeaseBroadcastRecommendations(false);
        // settings.setNotificationFrequency(EmailFrequency.NEVER);
        settings.setReceiveNewUserRequests(false);
        settings.setReceiveCriticalErrorMessages(false);
        settings.setReceiveAnnouncements(false);

        Map<String, List<String>> attributes = settings.toAttributes();

        EmailSettings decodedSettings = EmailSettings.fromAttributes(attributes);

        assertEquals(decodedSettings.getNotificationFrequency(), settings.getNotificationFrequency());
        assertEquals(decodedSettings.isReceiveAnnouncements(), settings.isReceiveAnnouncements());
        assertEquals(decodedSettings.isReceiveCeaseBroadcastRecommendations(), settings.isReceiveCeaseBroadcastRecommendations());
        assertEquals(decodedSettings.isReceiveCriticalErrorMessages(), settings.isReceiveCriticalErrorMessages());
        assertEquals(decodedSettings.isReceiveNewUserRequests(), settings.isReceiveNewUserRequests());
    
    }
}
