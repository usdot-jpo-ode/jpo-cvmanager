package us.dot.its.jpo.ode.api.emails.providers;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailProviderSmtpTest {

    @Mock
    private EmailProperties emailProperties;
    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailProviderSmtp provider;

    private EmailRecipient recipient;
    private EmailContent content;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(emailProperties.getSenderAddress()).thenReturn("sender@example.com");
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString())).thenReturn("http://unsubscribe");
        
        // Create a real MimeMessage with a mock session
        Session session = Session.getInstance(new Properties());
        mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        recipient = new EmailRecipient("to@example.com", null);
        content = new EmailContent("subject", "body with {{unsubscribe_url}}");
    }

    @Test
    void testSendBatchedEmailsSuccess() {
        doNothing().when(mailSender).send(any(MimeMessage[].class));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(1, results.size());
        assertEquals(200, results.get(0).getStatusCode());
        assertTrue(results.get(0).getMessage().contains("2"));
        verify(mailSender, times(1)).send(any(MimeMessage[].class));
    }

    @Test
    void testSendBatchedEmailsExitWhenNoRecipients() {
        List<EmailSendResponse> results = provider.sendBatchedEmails(List.of(), content);

        assertEquals(1, results.size());
        verifyNoInteractions(mailSender);
    }

    @Test
    void testSendBatchedEmailsMailAuthenticationException() {
        doThrow(new MailAuthenticationException("Auth failed"))
                .when(mailSender).send(any(MimeMessage[].class));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(1, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("SMTP authentication failed", results.get(0).getMessage());
        verify(mailSender, times(1)).send(any(MimeMessage[].class));
    }

    @Test
    void testSendBatchedEmailsMailSendException() {
        doThrow(new MailSendException("Send failed"))
                .when(mailSender).send(any(MimeMessage[].class));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(1, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("Failed to send batch emails", results.get(0).getMessage());
        verify(mailSender, times(1)).send(any(MimeMessage[].class));
    }

    @Test
    void testSendBatchedEmailsUnknownException() {
        doThrow(new RuntimeException("Unknown error"))
                .when(mailSender).send(any(MimeMessage[].class));

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(1, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertEquals("Unknown error", results.get(0).getMessage());
        verify(mailSender, times(1)).send(any(MimeMessage[].class));
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
        verify(mailSender, never()).send(any(MimeMessage[].class));
    }

    @Test
    void testSendBatchedEmailsUnsubscribeUrlReturnsNull() {
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString())).thenReturn(null);

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertTrue(results.get(0).getMessage().contains("Failed to generate unsubscribe URL"));
        verify(mailSender, never()).send(any(MimeMessage[].class));
    }

    @Test
    void testSendBatchedEmailsUnsubscribeUrlReturnsEmpty() {
        when(unsubscribeTokenGenerator.generateUnsubscribeUrl(anyString())).thenReturn("");

        List<EmailSendResponse> results = provider.sendBatchedEmails(
                List.of(recipient, new EmailRecipient("to2@example.com", null)), content);

        assertEquals(2, results.size());
        assertEquals(500, results.get(0).getStatusCode());
        assertTrue(results.get(0).getMessage().contains("Failed to generate unsubscribe URL"));
        verify(mailSender, never()).send(any(MimeMessage[].class));
    }

    @Test
    void testReplacePlaceholders() throws Exception {
        // Use reflection to access the private method
        var method = provider.getClass().getDeclaredMethod("replacePlaceholders", String.class, String.class);
        method.setAccessible(true);

        String input = "Click here: {{unsubscribe_url}} to unsubscribe";
        String unsubUrl = "http://unsubscribe";
        String result = (String) method.invoke(provider, input, unsubUrl);

        assertTrue(result.contains(unsubUrl));
        assertFalse(result.contains("{{unsubscribe_url}}"));
        assertEquals("Click here: http://unsubscribe to unsubscribe", result);
    }
}
