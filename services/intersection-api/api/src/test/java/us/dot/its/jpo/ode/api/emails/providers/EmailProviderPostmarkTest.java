package us.dot.its.jpo.ode.api.emails.providers;

import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.MessageResponse;
import com.postmarkapp.postmark.client.exception.PostmarkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailProviderPostmarkTest {

    @Mock
    private EmailProperties emailProperties;
    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    @Mock
    private ApiClient postmark;

    @InjectMocks
    private EmailProviderPostmark provider;

    private EmailRecipient recipient;
    private EmailContent content;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(emailProperties.getSenderAddress()).thenReturn("sender@example.com");
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString())).thenReturn("http://unsubscribe");
        recipient = new EmailRecipient("to@example.com", null);
        content = new EmailContent("subject", "body with {{unsubscribe_url}}");
    }


    @Test
    void testSendBatchedEmailsSuccess() throws Exception {
        MessageResponse resp1 = new MessageResponse();
        resp1.setErrorCode(0);
        resp1.setMessage("OK1");
        MessageResponse resp2 = new MessageResponse();
        resp2.setErrorCode(0);
        resp2.setMessage("OK2");
        when(postmark.deliverMessage(anyList())).thenReturn(List.of(resp1, resp2));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals("OK1", results.get(0).getMessage());
        assertEquals("OK2", results.get(1).getMessage());
        verify(postmark, times(1)).deliverMessage(anyList());
    }

    @Test
    void testSendBatchedEmailsExitWhenNoRecipients() throws Exception {
        List<EmailSendResponse> results = provider.sendBatchedEmails(List.of(), content);

        assertEquals(1, results.size());
        verify(postmark, times(0)).deliverMessage(anyList());
    }

    @Test
    void testSendBatchedEmailsThrowsPostmarkException() throws Exception {
        when(postmark.deliverMessage(anyList()))
                .thenThrow(new PostmarkException("fail", 500));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("Internal Server Error", results.get(0).getMessage());
        verify(postmark, times(1)).deliverMessage(anyList());
    }

    @Test
    void testSendBatchedEmailsThrowsIOException() throws Exception {
        when(postmark.deliverMessage(anyList()))
                .thenThrow(new IOException("fail"));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("Internal Server Error", results.get(0).getMessage());
        verify(postmark, times(1)).deliverMessage(anyList());
    }

    @Test
    void testReplacePlaceholders() throws Exception {
        // Use reflection to access the private method
        var method = provider.getClass().getDeclaredMethod("replacePlaceholders", String.class, String.class);
        method.setAccessible(true);

        String input = "Click here: {{unsubscribe_url}}\nThank you!";
        String unsubUrl = "http://unsubscribe";
        String result = (String) method.invoke(provider, input, unsubUrl);

        assertTrue(result.contains(unsubUrl));
        assertTrue(result.contains("<br>"));
        assertFalse(result.contains("{{unsubscribe_url}}"));
    }
}