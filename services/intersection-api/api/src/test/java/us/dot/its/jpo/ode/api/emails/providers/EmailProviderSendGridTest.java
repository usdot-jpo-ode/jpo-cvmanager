package us.dot.its.jpo.ode.api.emails.providers;

import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Email;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailProviderSendGridTest {

    @Mock
    private EmailProperties emailProperties;
    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private EmailProviderSendGrid provider;

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
    void testSendBatchedEmailsSuccess() throws IOException {
        Response response = new Response();
        response.setStatusCode(200);
        response.setBody("OK");
        when(sendGrid.api(any())).thenReturn(response);

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(1, results.size());
        assertEquals(200, results.get(0).getStatusCode());
        assertEquals("OK", results.get(0).getMessage());
        verify(sendGrid, times(1)).api(any());
    }

    @Test
    void testSendBatchedEmailsExitWhenNoRecipients() throws Exception {
        List<EmailSendResponse> results = provider.sendBatchedEmails(List.of(), content);

        assertEquals(0, results.size());
        verify(sendGrid, times(0)).api(any());
    }

    @Test
    void testSendBatchedEmailsThrowsIOException() throws IOException {
        when(sendGrid.api(any())).thenThrow(new IOException("Network error"));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("Internal Server Error", results.get(0).getMessage());
        assertEquals(500, results.get(1).getStatusCode());
        assertEquals("Internal Server Error", results.get(1).getMessage());
        verify(sendGrid, times(1)).api(any());
    }

    @Test
    void testSendBatchedEmailsUnsubscribeUrlGenerationFails() {
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString()))
                .thenThrow(new RuntimeException("Token generation failed"));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("Failed to generate unsubscribe URL", results.get(0).getMessage());
        assertEquals(500, results.get(1).getStatusCode());
        assertEquals("Failed to generate unsubscribe URL", results.get(1).getMessage());
        verifyNoInteractions(sendGrid);
    }

    @Test
    void testSendBatchedEmailsUnsubscribeUrlReturnsNull() {
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString())).thenReturn(null);

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertTrue(results.get(0).getMessage().contains("Failed to generate unsubscribe URL"));
        verifyNoInteractions(sendGrid);
    }

    @Test
    void testSendBatchedEmailsUnsubscribeUrlReturnsEmpty() {
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString())).thenReturn("");

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertTrue(results.get(0).getMessage().contains("Failed to generate unsubscribe URL"));
        verifyNoInteractions(sendGrid);
    }

    @Test
    void testSendBatchedEmailsNonSuccessStatusCode() throws IOException {
        Response response = new Response();
        response.setStatusCode(400);
        response.setBody("Bad Request");
        when(sendGrid.api(any())).thenReturn(response);

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(1, results.size());
        assertEquals(400, results.get(0).getStatusCode());
        assertEquals("Bad Request", results.get(0).getMessage());
        verify(sendGrid, times(1)).api(any());
    }

    @Test
    void testEmailRecipientToSendGridEmail() {
        EmailRecipient testRecipient = new EmailRecipient("test@example.com", "Test Name");
        Email sendGridEmail = testRecipient.toSendGridEmail();

        assertNotNull(sendGridEmail);
        assertEquals("test@example.com", sendGridEmail.getEmail());
        assertEquals("Test Name", sendGridEmail.getName());
    }

    @Test
    void testEmailRecipientToSendGridEmailWithoutName() {
        EmailRecipient testRecipient = new EmailRecipient("test@example.com", null);
        Email sendGridEmail = testRecipient.toSendGridEmail();

        assertNotNull(sendGridEmail);
        assertEquals("test@example.com", sendGridEmail.getEmail());
        assertNull(sendGridEmail.getName());
    }
}
