package us.dot.its.jpo.ode.api.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.data.model.message.MessageResponse;
import com.postmarkapp.postmark.client.exception.PostmarkException;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.EmailFrequency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SendGrid sendGrid;

    @Mock
    private ApiClient postmark;

    @Mock
    private ConflictMonitorApiProperties props;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(props.getEmailFromAddress()).thenReturn("test@example.com");
    }

    @Test
    void testSendEmailViaSendGrid() throws IOException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        when(sendGrid.api(any(Request.class))).thenReturn(new Response());

        // Act
        emailService.sendEmailViaSendGrid(to, subject, text);

        // Assert
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test
    void testSendEmailViaPostmark() throws IOException, PostmarkException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        when(postmark.deliverMessage(any(Message.class))).thenReturn(null);

        // Act
        emailService.sendEmailViaPostmark(to, subject, text);

        // Assert
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(postmark, times(1)).deliverMessage(captor.capture());
        Message sentMessage = captor.getValue();
        assert sentMessage.getTo().equals(to);
        assert sentMessage.getSubject().equals(subject);
    }

    @Test
    void testSendEmailViaSpringMail() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        // Act
        emailService.sendEmailViaSpringMail(to, subject, text);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assert sentMessage.getTo()[0].equals(to);
        assert sentMessage.getSubject().equals(subject);
        assert sentMessage.getText().equals(text);
    }

    @Test
    void testSendSimpleMessageWithSendGrid() throws IOException {
        // Arrange
        when(props.getEmailBroker()).thenReturn("sendgrid");
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        when(sendGrid.api(any(Request.class))).thenReturn(new Response());

        // Act
        emailService.sendSimpleMessage(to, subject, text);

        // Assert
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test
    void testSendSimpleMessageWithPostmark() throws IOException, PostmarkException {
        // Arrange
        when(props.getEmailBroker()).thenReturn("postmark");
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        when(postmark.deliverMessage(any(Message.class))).thenReturn(new MessageResponse());

        // Act
        emailService.sendSimpleMessage(to, subject, text);

        // Assert
        verify(postmark, times(1)).deliverMessage(any(Message.class));
    }

    @Test
    void testSendSimpleMessageWithSpringMail() {
        // Arrange
        when(props.getEmailBroker()).thenReturn("other");
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        // Act
        emailService.sendSimpleMessage(to, subject, text);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEmailList() {
        // Arrange
        List<UserRepresentation> users = new ArrayList<>();
        UserRepresentation user1 = new UserRepresentation();
        user1.setEmail("user1@example.com");
        users.add(user1);

        UserRepresentation user2 = new UserRepresentation();
        user2.setEmail("user2@example.com");
        users.add(user2);

        String subject = "Test Subject";
        String text = "Test Body";

        when(props.getEmailBroker()).thenReturn("other");

        // Act
        emailService.emailList(users, subject, text);

        // Assert
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testGetNotificationEmailList() {
        // Act
        List<UserRepresentation> result = emailService.getNotificationEmailList(EmailFrequency.ONCE_PER_DAY);

        // Assert
        assert result.isEmpty();
    }
}