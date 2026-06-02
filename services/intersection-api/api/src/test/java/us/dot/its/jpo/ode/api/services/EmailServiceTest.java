package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import us.dot.its.jpo.ode.api.emails.generators.*;
import us.dot.its.jpo.ode.api.emails.providers.EmailProvider;
import us.dot.its.jpo.ode.api.models.emails.*;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.FirmwareUpgradeFailureEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountEmailContents;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.repositories.UserEmailNotificationRepository;

import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private EmailProvider emailProvider;
    @Mock
    private UserEmailNotificationRepository userEmailNotificationRepository;
    @Mock
    private IntersectionNotificationSummaryEmailGenerator intersectionNotificationSummaryEmailGenerator;
    @Mock
    private PermissionService permissionService;
    @Mock
    private SupportRequestEmailGenerator supportRequestEmailGenerator;
    @Mock
    private MessageCountEmailGenerator messageCountEmailGenerator;
    @Mock
    private FirmwareUpgradeFailureEmailGenerator firmwareUpgradeFailureEmailGenerator;
    @Mock
    private ApiErrorEmailGenerator apiErrorEmailGenerator;
    @Mock
    private RsuErrorSummaryEmailGenerator rsuErrorSummaryEmailGenerator;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendEmails() {
        List<EmailRecipient> recipients = List.of(new EmailRecipient("test@example.com", null));
        EmailContent content = new EmailContent("subject", "body");
        doReturn(List.of()).when(emailProvider).sendBatchedEmails(recipients, content);

        emailService.sendEmails(recipients, content);

        verify(emailProvider, times(1)).sendBatchedEmails(recipients, content);
    }

    @Test
    void testGetUsersForNotificationType() {
        when(userEmailNotificationRepository.findUsersByNotificationType("Support Requests", "IMMEDIATE"))
                .thenReturn(List.of("user1@example.com", "user2@example.com"));

        List<EmailRecipient> recipients = emailService.getUsersForNotificationType(
                EmailCategory.SUPPORT_REQUEST, EmailFrequency.IMMEDIATE);

        assertEquals(2, recipients.size());
        assertEquals("user1@example.com", recipients.get(0).getEmail());
        assertEquals("user2@example.com", recipients.get(1).getEmail());
    }

    @Test
    void testGetUsersForNotificationTypeByRsu() throws Throwable {
        when(userEmailNotificationRepository.findUsersByNotificationTypeAndRsu("Support Requests",
                "IMMEDIATE", InetAddress.getByName("1.1.1.1")))
                .thenReturn(List.of("user1@example.com", "user2@example.com"));

        List<EmailRecipient> recipients = emailService.getUsersForNotificationTypeByRsu(
                EmailCategory.SUPPORT_REQUEST, "1.1.1.1", EmailFrequency.IMMEDIATE);

        assertEquals(2, recipients.size());
        assertEquals("user1@example.com", recipients.get(0).getEmail());
        assertEquals("user2@example.com", recipients.get(1).getEmail());
    }

    @Test
    void testGetUsersForNotificationTypeByOrganization() {
        when(userEmailNotificationRepository.findUsersByNotificationTypeAndOrganization("Support Requests", "IMMEDIATE",
                "Test Org"))
                .thenReturn(List.of("user1@example.com", "user2@example.com"));

        List<EmailRecipient> recipients = emailService.getUsersForNotificationTypeByOrganization(
                EmailCategory.SUPPORT_REQUEST, "Test Org", EmailFrequency.IMMEDIATE);

        assertEquals(2, recipients.size());
        assertEquals("user1@example.com", recipients.get(0).getEmail());
        assertEquals("user2@example.com", recipients.get(1).getEmail());
    }

    @Test
    void testSendIntersectionNotificationSummaryEmailSendResponses() {
        IntersectionNotificationSummaryEmailContents data = new IntersectionNotificationSummaryEmailContents();
        EmailContent content = new EmailContent("subject", "body");
        List<EmailRecipient> recipients = List.of(new EmailRecipient("test@example.com", null));
        List<EmailSendResponse> responses = List.of(new EmailSendResponse(0, "OK"));

        when(intersectionNotificationSummaryEmailGenerator.generateEmailBody(data)).thenReturn(content);
        when(userEmailNotificationRepository.findUsersByNotificationType(anyString(), any()))
                .thenReturn(List.of("test@example.com"));
        when(emailProvider.sendBatchedEmails(recipients, content)).thenReturn(responses);

        List<EmailSendResponse> result = emailService.sendIntersectionNotificationSummaryEmailSendResponses(data);

        assertEquals(responses, result);
        verify(emailProvider).sendBatchedEmails(recipients, content);
    }

    @Test
    void testSendSupportRequest() {
        SupportRequestEmailContents data = new SupportRequestEmailContents();
        EmailContent content = new EmailContent("subject", "body");
        List<EmailRecipient> recipients = List.of(new EmailRecipient("test@example.com", null));
        List<EmailSendResponse> responses = List.of(new EmailSendResponse(0, "OK"));

        when(supportRequestEmailGenerator.generateEmailBody(data)).thenReturn(content);
        when(userEmailNotificationRepository.findUsersByNotificationType(anyString(), any()))
                .thenReturn(List.of("test@example.com"));
        when(emailProvider.sendBatchedEmails(recipients, content)).thenReturn(responses);

        List<EmailSendResponse> result = emailService.sendSupportRequest(data);

        assertEquals(responses, result);
        verify(emailProvider).sendBatchedEmails(recipients, content);
    }

    @Test
    void testSendMessageCounts() {
        MessageCountEmailContents data = new MessageCountEmailContents();
        EmailContent content = new EmailContent("subject", "body");
        List<EmailRecipient> recipients = List.of(new EmailRecipient("test@example.com", null));
        List<EmailSendResponse> responses = List.of(new EmailSendResponse(0, "OK"));

        when(messageCountEmailGenerator.generateEmailBody(data)).thenReturn(content);
        when(userEmailNotificationRepository.findUsersByNotificationTypeAndOrganization(any(), any(),
                any()))
                .thenReturn(List.of("test@example.com"));
        when(emailProvider.sendBatchedEmails(recipients, content)).thenReturn(responses);

        List<EmailSendResponse> result = emailService.sendMessageCounts(data);

        assertEquals(responses, result);
        verify(emailProvider).sendBatchedEmails(recipients, content);
    }

    @Test
    void testSendFirmwareUpgradeEmail() {
        FirmwareUpgradeFailureEmailContents data = new FirmwareUpgradeFailureEmailContents();
        EmailContent content = new EmailContent("subject", "body");
        List<EmailRecipient> recipients = List.of(new EmailRecipient("test@example.com", null));
        List<EmailSendResponse> responses = List.of(new EmailSendResponse(0, "OK"));

        when(firmwareUpgradeFailureEmailGenerator.generateEmailBody(data)).thenReturn(content);
        when(userEmailNotificationRepository.findUsersByNotificationTypeAndRsu(anyString(), anyString(), any()))
                .thenReturn(List.of("test@example.com"));
        when(emailProvider.sendBatchedEmails(recipients, content)).thenReturn(responses);

        List<EmailSendResponse> result = emailService.sendFirmwareUpgradeFailure(data);

        assertEquals(responses, result);
        verify(emailProvider).sendBatchedEmails(recipients, content);
    }

    @Test
    void testSendApiError() {
        ApiErrorEmailContents data = new ApiErrorEmailContents();
        EmailContent content = new EmailContent("subject", "body");
        List<EmailRecipient> recipients = List.of(new EmailRecipient("test@example.com", null));
        List<EmailSendResponse> responses = List.of(new EmailSendResponse(0, "OK"));

        when(apiErrorEmailGenerator.generateEmailBody(data)).thenReturn(content);
        when(userEmailNotificationRepository.findUsersByNotificationType(anyString(), any()))
                .thenReturn(List.of("test@example.com"));
        when(emailProvider.sendBatchedEmails(recipients, content)).thenReturn(responses);

        List<EmailSendResponse> result = emailService.sendApiError(data);

        assertEquals(responses, result);
        verify(emailProvider).sendBatchedEmails(recipients, content);
    }

    @Test
    void testSendRsuErrorSummary() {
        RsuErrorSummaryEmailContents data = new RsuErrorSummaryEmailContents("subject", "message");
        EmailContent content = new EmailContent("subject", "body");
        List<EmailSendResponse> responses = List.of(new EmailSendResponse(0, "OK"));

        CvManagerAuthToken authToken = mock(CvManagerAuthToken.class);
        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
        when(authToken.getEmail()).thenReturn("test@example.com");

        when(rsuErrorSummaryEmailGenerator.generateEmailBody(data)).thenReturn(content);
        when(userEmailNotificationRepository.findUsersByNotificationType(anyString(), any()))
                .thenReturn(List.of("test@example.com"));
        when(emailProvider.sendBatchedEmails(anyList(), eq(content))).thenReturn(responses);

        List<EmailSendResponse> result = emailService.sendRsuErrorSummary(data);

        assertEquals(responses, result);
        verify(emailProvider).sendBatchedEmails(anyList(), eq(content));
    }
}