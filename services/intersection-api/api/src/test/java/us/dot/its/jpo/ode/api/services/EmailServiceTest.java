package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import us.dot.its.jpo.ode.api.emails.generators.*;
import us.dot.its.jpo.ode.api.emails.providers.EmailProvider;
import us.dot.its.jpo.ode.api.mappers.UserEmailNotificationMapper;
import us.dot.its.jpo.ode.api.models.emails.EmailCategory;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailFrequency;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;
import us.dot.its.jpo.ode.api.models.emails.UserEmailNotificationDto;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.FirmwareUpgradeFailureEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.postgres.tables.EmailType;
import us.dot.its.jpo.ode.api.models.postgres.tables.Role;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserEmailNotification;
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountEmailContents;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.repositories.UserEmailNotificationRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import us.dot.its.jpo.ode.api.repositories.EmailTypeRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;

import java.net.InetAddress;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private EmailProvider emailProvider;
    @Mock
    private EmailTypeRepository emailTypeRepository;
    @Mock
    private UserEmailNotificationRepository userEmailNotificationRepository;
    @Mock
    private IntersectionNotificationSummaryEmailGenerator intersectionNotificationSummaryEmailGenerator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserEmailNotificationMapper userEmailNotificationMapper;
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

    private static final String TEST_EMAIL = "user@example.com";
    private static final User TEST_USER = new User();
    private static final Role ROLE_USER = new Role();
    private static final Role ROLE_ADMIN = new Role();
    private static final List<EmailType> VALID_EMAIL_TYPES = Arrays.asList(
            createEmailType("Support Requests", "Receive support requests from users", "admin",
                    true, true, false, false, false),
            createEmailType("Intersection Notification Summary",
                    "Receive automated intersection notification summary emails",
                    "user",
                    true, true, true, true, true),
            createEmailType("Daily Message Counts", "Receive automated daily message count emails", "user",
                    true, false, false, false, false),
            createEmailType("Access Requests", "Receive organization access requests from users", "admin",
                    true, false, false, false, false));

    private static UserEmailNotification createUserEmailNotification(String category, String description,
            String roleName,
            boolean immediate, boolean hourly, boolean daily, boolean weekly, boolean monthly,
            boolean supports_immediate, boolean supports_hourly, boolean supports_daily, boolean supports_weekly,
            boolean supports_monthly) {
        EmailType emailType = createEmailType(category, description, roleName, supports_immediate, supports_hourly,
                supports_daily, supports_weekly,
                supports_monthly);

        UserEmailNotification notification = new UserEmailNotification();
        notification.setEmailType(emailType);
        notification.setUser(TEST_USER);
        notification.setImmediate(immediate);
        notification.setHourly(hourly);
        notification.setDaily(daily);
        notification.setWeekly(weekly);
        notification.setMonthly(monthly);

        return notification;
    }

    public static EmailType createEmailType(String category, String description,
            String roleName, boolean supports_immediate, boolean supports_hourly, boolean supports_daily,
            boolean supports_weekly,
            boolean supports_monthly) {
        EmailType emailType = new EmailType();
        emailType.setEmailType(category);
        emailType.setDescription(description);
        if (roleName.equals("user")) {
            emailType.setRequiredRole(ROLE_USER);
        } else if (roleName.equals("admin")) {
            emailType.setRequiredRole(ROLE_ADMIN);
        }
        emailType.setSupportsImmediate(supports_immediate);
        emailType.setSupportsHourly(supports_hourly);
        emailType.setSupportsDaily(supports_daily);
        emailType.setSupportsWeekly(supports_weekly);
        emailType.setSupportsMonthly(supports_monthly);

        return emailType;
    }

    /**
     * Custom assertion method with optional message.
     */
    private void assertNotificationEquals(UserEmailNotification expected, UserEmailNotification actual,
            String message) {
        String prefix = message != null ? message + ": " : "";

        assertNotNull(actual, prefix + "Actual notification should not be null");
        assertNotNull(expected, prefix + "Expected notification should not be null");

        // Compare email type
        assertEquals(expected.getEmailType().getEmailType(), actual.getEmailType().getEmailType(),
                prefix + "Email type mismatch");
        assertEquals(expected.getEmailType().getDescription(), actual.getEmailType().getDescription(),
                prefix + "Description mismatch");
        assertEquals(expected.getEmailType().getRequiredRole().getName(),
                actual.getEmailType().getRequiredRole().getName(),
                prefix + "Required role mismatch");

        // Compare frequency flags
        assertEquals(expected.getImmediate(), actual.getImmediate(), prefix + "Immediate flag mismatch");
        assertEquals(expected.getHourly(), actual.getHourly(), prefix + "Hourly flag mismatch");
        assertEquals(expected.getDaily(), actual.getDaily(), prefix + "Daily flag mismatch");
        assertEquals(expected.getWeekly(), actual.getWeekly(), prefix + "Weekly flag mismatch");
        assertEquals(expected.getMonthly(), actual.getMonthly(), prefix + "Monthly flag mismatch");

        // Compare supports flags
        assertEquals(expected.getEmailType().getSupportsImmediate(),
                actual.getEmailType().getSupportsImmediate(),
                prefix + "Supports immediate mismatch");
        assertEquals(expected.getEmailType().getSupportsHourly(),
                actual.getEmailType().getSupportsHourly(),
                prefix + "Supports hourly mismatch");
        assertEquals(expected.getEmailType().getSupportsDaily(),
                actual.getEmailType().getSupportsDaily(),
                prefix + "Supports daily mismatch");
        assertEquals(expected.getEmailType().getSupportsWeekly(),
                actual.getEmailType().getSupportsWeekly(),
                prefix + "Supports weekly mismatch");
        assertEquals(expected.getEmailType().getSupportsMonthly(),
                actual.getEmailType().getSupportsMonthly(),
                prefix + "Supports monthly mismatch");
    }

    /**
     * Compare lists of UserEmailNotification objects.
     */
    private void assertNotificationListEquals(List<UserEmailNotification> expected,
            List<UserEmailNotification> actual) {
        assertEquals(expected.size(), actual.size(), "List sizes should match");

        for (int i = 0; i < expected.size(); i++) {
            assertNotificationEquals(expected.get(i), actual.get(i),
                    "Notification at index " + i);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        TEST_USER.setEmail(TEST_EMAIL);
        ROLE_USER.setName("user");
        ROLE_ADMIN.setName("admin");

        // Mapper is mocked in this test class; provide behavior for frequency updates.
        lenient().when(userEmailNotificationMapper.updateFrequency(any(UserEmailNotificationDto.class),
                any(UserEmailNotification.class))).thenAnswer(invocation -> {
                    UserEmailNotificationDto dto = invocation.getArgument(0);
                    UserEmailNotification entity = invocation.getArgument(1);
                    if (dto == null || entity == null) {
                        return null;
                    }
                    entity.setImmediate(dto.getImmediate());
                    entity.setHourly(dto.getHourly());
                    entity.setDaily(dto.getDaily());
                    entity.setWeekly(dto.getWeekly());
                    entity.setMonthly(dto.getMonthly());
                    return entity;
                });
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

    @Test
    void testUpdateEmailSubscriptions_NoChange() {

        List<UserEmailNotification> SUBSCRIPTION_LIST = Arrays.asList(
                createUserEmailNotification(
                        "Support Requests", "Receive support requests from users", "admin",
                        true, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Intersection Notification Summary",
                        "Receive automated intersection notification summary emails",
                        "user",
                        true, false, false, false, false,
                        true, true, true, true, true),
                createUserEmailNotification(
                        "Daily Message Counts", "Receive automated daily message count emails", "user",
                        false, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Access Requests", "Receive organization access requests from users", "admin",
                        false, false, false, false, false,
                        true, false, false, false, false));

        UserEmailNotificationDto SUPPORT_REQUEST_DTO = new UserEmailNotificationDto(
                "Support Requests", "Receive support requests from users", "admin",
                true, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotificationDto INTERSECTION_NOTIFICATION_SUMMARY_DTO = new UserEmailNotificationDto(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                true, false, false, false, false,
                true, true, true, true, true);

        UserEmailNotificationDto DAILY_MESSAGE_COUNTS_DTO = new UserEmailNotificationDto(
                "Daily Message Counts", "Receive automated daily message count emails",
                "user",
                false, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotificationDto ACCESS_REQUESTS_DTO = new UserEmailNotificationDto(
                "Access Requests", "Receive organization access requests from users",
                "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        List<UserEmailNotification> emailSubscriptions = SUBSCRIPTION_LIST;

        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(emailSubscriptions);
        when(emailTypeRepository.findAll()).thenReturn(VALID_EMAIL_TYPES);

        emailService.updateEmailSubscriptions(TEST_EMAIL, true, true, List.of(SUPPORT_REQUEST_DTO,
                INTERSECTION_NOTIFICATION_SUMMARY_DTO, DAILY_MESSAGE_COUNTS_DTO, ACCESS_REQUESTS_DTO));

        verify(userEmailNotificationRepository, never()).deleteAll();
        verify(userEmailNotificationRepository, never()).saveAll(anyList());
        verify(userEmailNotificationRepository).findNotificationsByUser(TEST_EMAIL);
    }

    @Test
    void testUpdateEmailSubscriptions_AddSubscriptions() {

        List<UserEmailNotification> SUBSCRIPTION_LIST = Arrays.asList(
                createUserEmailNotification(
                        "Support Requests", "Receive support requests from users", "admin",
                        true, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Intersection Notification Summary",
                        "Receive automated intersection notification summary emails",
                        "user",
                        true, false, false, false, false,
                        true, true, true, true, true));

        UserEmailNotificationDto dailyMessageCountsDto = new UserEmailNotificationDto(
                "Daily Message Counts", "Receive automated daily message count emails", "user",
                true, false, false, false, false,
                true, false, false, false, false);
        UserEmailNotificationDto accessRequestDto = new UserEmailNotificationDto(
                "Access Requests", "Receive organization access requests from users", "admin",
                true, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotification dailyMessageCounts = createUserEmailNotification(
                "Daily Message Counts", "Receive automated daily message count emails", "user",
                true, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotification accessRequests = createUserEmailNotification(
                "Access Requests", "Receive organization access requests from users", "admin",
                true, false, false, false, false,
                true, false, false, false, false);

        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(SUBSCRIPTION_LIST);
        when(userEmailNotificationMapper.toEntity(dailyMessageCountsDto, TEST_USER,
                VALID_EMAIL_TYPES.get(2))).thenReturn(dailyMessageCounts);
        when(userEmailNotificationMapper.toEntity(accessRequestDto, TEST_USER,
                VALID_EMAIL_TYPES.get(3))).thenReturn(accessRequests);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(TEST_USER);
        when(emailTypeRepository.findAll()).thenReturn(VALID_EMAIL_TYPES);

        emailService.updateEmailSubscriptions(TEST_EMAIL, true, true,
                List.of(dailyMessageCountsDto, accessRequestDto));

        verify(userEmailNotificationRepository).findNotificationsByUser(TEST_EMAIL);
        verify(userEmailNotificationRepository, never()).deleteAll(anyList());

        ArgumentCaptor<List<UserEmailNotification>> captor = ArgumentCaptor.captor();
        verify(userEmailNotificationRepository, times(1)).saveAll(captor.capture());

        List<UserEmailNotification> savedNotifications = captor.getValue();
        assertNotificationListEquals(
                List.of(dailyMessageCounts, accessRequests),
                savedNotifications);
    }

    @Test
    void testUpdateEmailSubscriptions_RemoveSubscriptions() {

        List<UserEmailNotification> SUBSCRIPTION_LIST = Arrays.asList(
                createUserEmailNotification(
                        "Support Requests", "Receive support requests from users", "admin",
                        true, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Intersection Notification Summary",
                        "Receive automated intersection notification summary emails",
                        "user",
                        true, false, false, false, false,
                        true, true, true, true, true),
                createUserEmailNotification(
                        "Daily Message Counts", "Receive automated daily message count emails", "user",
                        false, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Access Requests", "Receive organization access requests from users", "admin",
                        false, false, false, false, false,
                        true, false, false, false, false));

        UserEmailNotificationDto supportRequestsDto = new UserEmailNotificationDto(
                "Support Requests", "Receive support requests from users", "admin",
                false, false, false, false, false,
                true, false, false, false, false);
        UserEmailNotificationDto intersectionNotificationSummaryDto = new UserEmailNotificationDto(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                false, false, false, false, false,
                true, true, true, true, true);

        UserEmailNotification supportRequests = createUserEmailNotification(
                "Support Requests", "Receive support requests from users", "admin",
                true, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotification intersectionNotificationSummaries = createUserEmailNotification(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                true, false, false, false, false,
                true, true, true, true, true);

        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(SUBSCRIPTION_LIST);
        when(userEmailNotificationMapper.toEntity(supportRequestsDto, TEST_USER,
                VALID_EMAIL_TYPES.get(0))).thenReturn(supportRequests);
        when(userEmailNotificationMapper.toEntity(intersectionNotificationSummaryDto, TEST_USER,
                VALID_EMAIL_TYPES.get(1)))
                .thenReturn(intersectionNotificationSummaries);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(TEST_USER);
        when(emailTypeRepository.findAll()).thenReturn(VALID_EMAIL_TYPES);

        emailService.updateEmailSubscriptions(TEST_EMAIL, true, true,
                List.of(supportRequestsDto, intersectionNotificationSummaryDto));

        verify(userEmailNotificationRepository).findNotificationsByUser(TEST_EMAIL);
        verify(userEmailNotificationRepository, never()).saveAll(anyList());

        ArgumentCaptor<List<UserEmailNotification>> captor = ArgumentCaptor.captor();
        verify(userEmailNotificationRepository, times(1)).deleteAll(captor.capture());

        List<UserEmailNotification> deletedNotifications = captor.getValue();
        assertNotificationListEquals(
                List.of(supportRequests, intersectionNotificationSummaries),
                deletedNotifications);
    }

    @Test
    void testUpdateEmailSubscriptions_UpdateSubscriptions() {

        List<UserEmailNotification> SUBSCRIPTION_LIST = Arrays.asList(
                createUserEmailNotification(
                        "Support Requests", "Receive support requests from users", "admin",
                        true, false, false, false, false,
                        true, true, false, false, false),
                createUserEmailNotification(
                        "Intersection Notification Summary",
                        "Receive automated intersection notification summary emails",
                        "user",
                        true, false, false, false, false,
                        true, true, true, true, true),
                createUserEmailNotification(
                        "Daily Message Counts", "Receive automated daily message count emails", "user",
                        false, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Access Requests", "Receive organization access requests from users", "admin",
                        false, false, false, false, false,
                        true, false, false, false, false));

        UserEmailNotificationDto supportRequestsDto = new UserEmailNotificationDto(
                "Support Requests", "Receive support requests from users", "admin",
                true, true, false, false, false,
                true, true, false, false, false);
        UserEmailNotificationDto intersectionNotificationSummaryDto = new UserEmailNotificationDto(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                true, false, true, false, false,
                true, true, true, true, true);

        UserEmailNotification supportRequests = createUserEmailNotification(
                "Support Requests", "Receive support requests from users", "admin",
                true, true, false, false, false,
                true, true, false, false, false);

        UserEmailNotification intersectionNotificationSummaries = createUserEmailNotification(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                true, false, true, false, false,
                true, true, true, true, true);

        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(SUBSCRIPTION_LIST);
        when(userEmailNotificationMapper.toEntity(supportRequestsDto, TEST_USER,
                SUBSCRIPTION_LIST.get(0).getEmailType())).thenReturn(supportRequests);
        when(userEmailNotificationMapper.toEntity(intersectionNotificationSummaryDto, TEST_USER,
                SUBSCRIPTION_LIST.get(1).getEmailType()))
                .thenReturn(intersectionNotificationSummaries);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(TEST_USER);
        when(emailTypeRepository.findByEmailType("Support Requests"))
                .thenReturn(SUBSCRIPTION_LIST.get(0).getEmailType());
        when(emailTypeRepository.findByEmailType("Intersection Notification Summary"))
                .thenReturn(SUBSCRIPTION_LIST.get(1).getEmailType());
        when(emailTypeRepository.findAll()).thenReturn(VALID_EMAIL_TYPES);

        emailService.updateEmailSubscriptions(TEST_EMAIL, true, true,
                List.of(supportRequestsDto, intersectionNotificationSummaryDto));

        verify(userEmailNotificationRepository).findNotificationsByUser(TEST_EMAIL);
        verify(userEmailNotificationRepository, never()).deleteAll(anyList());

        ArgumentCaptor<List<UserEmailNotification>> captor = ArgumentCaptor.captor();
        verify(userEmailNotificationRepository, times(1)).saveAll(captor.capture());

        List<UserEmailNotification> updatedNotifications = captor.getValue();
        assertNotificationListEquals(
                List.of(supportRequests, intersectionNotificationSummaries),
                updatedNotifications);
    }

    @Test
    void testUpdateEmailSubscriptions_AllUpdateTypes() {

        List<UserEmailNotification> SUBSCRIPTION_LIST = Arrays.asList(
                createUserEmailNotification(
                        "Support Requests", "Receive support requests from users", "admin",
                        true, false, false, false, false,
                        true, true, false, false, false),
                createUserEmailNotification(
                        "Intersection Notification Summary",
                        "Receive automated intersection notification summary emails",
                        "user",
                        true, false, false, false, false,
                        true, true, true, true, true));

        UserEmailNotificationDto supportRequestsDto = new UserEmailNotificationDto(
                "Support Requests", "Receive support requests from users", "admin",
                true, true, false, false, false,
                true, true, false, false, false);
        UserEmailNotificationDto intersectionNotificationSummaryDto = new UserEmailNotificationDto(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                false, false, false, false, false,
                true, true, true, true, true);
        UserEmailNotificationDto dailyMessageCountsDto = new UserEmailNotificationDto(
                "Daily Message Counts", "Receive automated daily message count emails", "user",
                true, false, false, false, false,
                true, false, false, false, false);
        UserEmailNotificationDto accessRequestDto = new UserEmailNotificationDto(
                "Access Requests", "Receive organization access requests from users", "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotification supportRequests = createUserEmailNotification(
                "Support Requests", "Receive support requests from users", "admin",
                true, true, false, false, false,
                true, true, false, false, false);
        UserEmailNotification intersectionNotificationSummaries = createUserEmailNotification(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                true, false, false, false, false,
                true, true, true, true, true);
        UserEmailNotification dailyMessageCounts = createUserEmailNotification(
                "Daily Message Counts", "Receive automated daily message count emails", "user",
                false, false, false, false, false,
                true, false, false, false, false);
        UserEmailNotification accessRequests = createUserEmailNotification(
                "Access Requests", "Receive organization access requests from users", "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(SUBSCRIPTION_LIST);
        when(userEmailNotificationMapper.toEntity(supportRequestsDto, TEST_USER,
                VALID_EMAIL_TYPES.get(0))).thenReturn(supportRequests);
        when(userEmailNotificationMapper.toEntity(intersectionNotificationSummaryDto, TEST_USER,
                VALID_EMAIL_TYPES.get(1))).thenReturn(intersectionNotificationSummaries);
        when(userEmailNotificationMapper.toEntity(dailyMessageCountsDto, TEST_USER,
                VALID_EMAIL_TYPES.get(2))).thenReturn(dailyMessageCounts);
        when(userEmailNotificationMapper.toEntity(accessRequestDto, TEST_USER,
                VALID_EMAIL_TYPES.get(3))).thenReturn(accessRequests);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(TEST_USER);
        when(emailTypeRepository.findAll()).thenReturn(VALID_EMAIL_TYPES);

        emailService.updateEmailSubscriptions(TEST_EMAIL, true, true,
                List.of(supportRequestsDto, intersectionNotificationSummaryDto, dailyMessageCountsDto,
                        accessRequestDto));

        verify(userEmailNotificationRepository).findNotificationsByUser(TEST_EMAIL);

        // Deleted Intersection Notification Summary notification
        ArgumentCaptor<List<UserEmailNotification>> deleteCaptor = ArgumentCaptor.captor();
        verify(userEmailNotificationRepository, times(1)).deleteAll(deleteCaptor.capture());

        List<UserEmailNotification> deletedNotifications = deleteCaptor.getValue();
        assertNotificationListEquals(List.of(intersectionNotificationSummaries), deletedNotifications);

        // Added Daily Message Counts notification
        ArgumentCaptor<List<UserEmailNotification>> saveCaptor = ArgumentCaptor.captor();
        verify(userEmailNotificationRepository, times(1)).saveAll(saveCaptor.capture());

        // Get all captured values
        List<List<UserEmailNotification>> allSaveInvocations = saveCaptor.getAllValues();
        assertEquals(1, allSaveInvocations.size(), "Should have 1 saveAll invocation");

        // saveAll call should be for updating Support Requests and adding Daily Message
        // Counts
        List<UserEmailNotification> updatedNotifications = allSaveInvocations.get(0);
        assertNotificationListEquals(List.of(supportRequests, dailyMessageCounts), updatedNotifications);
    }

    @Test
    void testUpdateEmailSubscriptions_NotAuthorized() {
        List<UserEmailNotification> SUBSCRIPTION_LIST = Arrays.asList(
                createUserEmailNotification(
                        "Support Requests", "Receive support requests from users", "operator",
                        true, false, false, false, false,
                        true, true, false, false, false),
                createUserEmailNotification(
                        "Intersection Notification Summary",
                        "Receive automated intersection notification summary emails",
                        "user",
                        true, false, false, false, false,
                        true, true, true, true, true),
                createUserEmailNotification(
                        "Daily Message Counts", "Receive automated daily message count emails", "user",
                        false, false, false, false, false,
                        true, false, false, false, false),
                createUserEmailNotification(
                        "Access Requests", "Receive organization access requests from users", "admin",
                        false, false, false, false, false,
                        true, false, false, false, false));

        UserEmailNotificationDto supportRequestsDto = new UserEmailNotificationDto(
                "Support Requests", "Receive support requests from users", "operator",
                true, true, false, false, false,
                true, true, false, false, false);
        UserEmailNotificationDto intersectionNotificationSummaryDto = new UserEmailNotificationDto(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                false, false, false, false, false,
                true, true, true, true, true);
        UserEmailNotificationDto dailyMessageCountsDto = new UserEmailNotificationDto(
                "Daily Message Counts", "Receive automated daily message count emails", "user",
                false, false, false, false, false,
                true, false, false, false, false);
        UserEmailNotificationDto accessRequestDto = new UserEmailNotificationDto(
                "Access Requests", "Receive organization access requests from users", "admin",
                true, false, false, false, false,
                true, false, false, false, false);

        UserEmailNotification supportRequests = createUserEmailNotification(
                "Support Requests", "Receive support requests from users", "operator",
                true, true, false, false, false,
                true, true, false, false, false);
        UserEmailNotification intersectionNotificationSummaries = createUserEmailNotification(
                "Intersection Notification Summary", "Receive automated intersection notification summary emails",
                "user",
                true, false, false, false, false,
                true, true, true, true, true);
        UserEmailNotification dailyMessageCounts = createUserEmailNotification(
                "Daily Message Counts", "Receive automated daily message count emails", "user",
                false, false, false, false, false,
                true, false, false, false, false);
        UserEmailNotification accessRequests = createUserEmailNotification(
                "Access Requests", "Receive organization access requests from users", "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(SUBSCRIPTION_LIST);
        when(userEmailNotificationMapper.toEntity(supportRequestsDto, TEST_USER,
                SUBSCRIPTION_LIST.get(0).getEmailType())).thenReturn(supportRequests);
        when(userEmailNotificationMapper.toEntity(intersectionNotificationSummaryDto, TEST_USER,
                SUBSCRIPTION_LIST.get(1).getEmailType()))
                .thenReturn(intersectionNotificationSummaries);
        when(userEmailNotificationMapper.toEntity(dailyMessageCountsDto, TEST_USER,
                SUBSCRIPTION_LIST.get(2).getEmailType())).thenReturn(dailyMessageCounts);
        when(userEmailNotificationMapper.toEntity(accessRequestDto, TEST_USER, SUBSCRIPTION_LIST.get(3).getEmailType()))
                .thenReturn(accessRequests);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(TEST_USER);
        when(emailTypeRepository.findByEmailType("Support Requests"))
                .thenReturn(SUBSCRIPTION_LIST.get(0).getEmailType());
        when(emailTypeRepository.findByEmailType("Intersection Notification Summary"))
                .thenReturn(SUBSCRIPTION_LIST.get(1).getEmailType());
        when(emailTypeRepository.findByEmailType("Daily Message Counts"))
                .thenReturn(SUBSCRIPTION_LIST.get(2).getEmailType());
        when(emailTypeRepository.findByEmailType("Access Requests"))
                .thenReturn(SUBSCRIPTION_LIST.get(3).getEmailType());
        when(emailTypeRepository.findAll()).thenReturn(VALID_EMAIL_TYPES);

        assertThrows(AccessDeniedException.class, () -> emailService.updateEmailSubscriptions(TEST_EMAIL, true, false,
                List.of(supportRequestsDto, intersectionNotificationSummaryDto, dailyMessageCountsDto,
                        accessRequestDto)));

        // No additions because Access Requests required admin role and user is not
        // admin
    }

    // -------------------------------------------------------------------------
    // filterValidSubscriptionsForUser tests (exercised via
    // updateEmailSubscriptions)
    // -------------------------------------------------------------------------

    @Test
    void testFilterValidSubscriptions_UnknownCategory_ThrowsIllegalArgumentException() {
        EmailType knownType = createEmailType("Known Category", "desc", "user",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(knownType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Unknown Category", "desc", "user",
                false, false, false, false, false,
                true, false, false, false, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
        assertTrue(ex.getMessage().contains("Invalid email category"));
    }

    @Test
    void testFilterValidSubscriptions_PlainUserSubscribesUserRoleType_Succeeds() {
        EmailType userType = createEmailType("User Notifications", "desc", "user",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(userType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        // subscribed=false (all frequency flags false) → nothing is added, no exception
        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "User Notifications", "desc", "user",
                false, false, false, false, false,
                true, false, false, false, false);

        emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto));
        verify(userEmailNotificationRepository, never()).deleteAll();
        verify(userEmailNotificationRepository, never()).saveAll(anyList());

    }

    @Test
    void testFilterValidSubscriptions_PlainUserSubscribesOperatorRoleType_ThrowsAccessDeniedException() {
        Role operatorRole = new Role();
        operatorRole.setName("operator");
        EmailType operatorType = new EmailType();
        operatorType.setEmailType("Operator Notifications");
        operatorType.setRequiredRole(operatorRole);
        operatorType.setSupportsImmediate(true);
        when(emailTypeRepository.findAll()).thenReturn(List.of(operatorType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Operator Notifications", "desc", "operator",
                false, false, false, false, false,
                true, false, false, false, false);

        assertThrows(AccessDeniedException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
    }

    @Test
    void testFilterValidSubscriptions_PlainUserSubscribesAdminRoleType_ThrowsAccessDeniedException() {
        EmailType adminType = createEmailType("Admin Notifications", "desc", "admin",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(adminType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Admin Notifications", "desc", "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        assertThrows(AccessDeniedException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
    }

    @Test
    void testFilterValidSubscriptions_OperatorSubscribesOperatorRoleType_Succeeds() {
        Role operatorRole = new Role();
        operatorRole.setName("operator");
        EmailType operatorType = new EmailType();
        operatorType.setEmailType("Operator Notifications");
        operatorType.setRequiredRole(operatorRole);
        operatorType.setSupportsImmediate(true);
        when(emailTypeRepository.findAll()).thenReturn(List.of(operatorType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Operator Notifications", "desc", "operator",
                false, false, false, false, false,
                true, false, false, false, false);

        emailService.updateEmailSubscriptions(TEST_EMAIL, true, false, List.of(dto));
        verify(userEmailNotificationRepository, never()).deleteAll();
        verify(userEmailNotificationRepository, never()).saveAll(anyList());
    }

    @Test
    void testFilterValidSubscriptions_OperatorSubscribesAdminRoleType_ThrowsAccessDeniedException() {
        EmailType adminType = createEmailType("Admin Notifications", "desc", "admin",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(adminType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Admin Notifications", "desc", "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        assertThrows(AccessDeniedException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, true, false, List.of(dto)));
    }

    @Test
    void testFilterValidSubscriptions_AdminSubscribesAdminRoleType_Succeeds() {
        EmailType adminType = createEmailType("Admin Notifications", "desc", "admin",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(adminType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Admin Notifications", "desc", "admin",
                false, false, false, false, false,
                true, false, false, false, false);

        emailService.updateEmailSubscriptions(TEST_EMAIL, false, true, List.of(dto));
        verify(userEmailNotificationRepository, never()).deleteAll();
        verify(userEmailNotificationRepository, never()).saveAll(anyList());
    }

    @Test
    void testFilterValidSubscriptions_AdminSubscribesOperatorRoleType_Succeeds() {
        Role operatorRole = new Role();
        operatorRole.setName("operator");
        EmailType operatorType = new EmailType();
        operatorType.setEmailType("Operator Notifications");
        operatorType.setRequiredRole(operatorRole);
        operatorType.setSupportsImmediate(true);
        when(emailTypeRepository.findAll()).thenReturn(List.of(operatorType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Operator Notifications", "desc", "operator",
                false, false, false, false, false,
                true, false, false, false, false);

        emailService.updateEmailSubscriptions(TEST_EMAIL, false, true, List.of(dto));
        verify(userEmailNotificationRepository, never()).deleteAll();
        verify(userEmailNotificationRepository, never()).saveAll(anyList());
    }

    @Test
    void testFilterValidSubscriptions_UnsupportedImmediateFrequency_ThrowsIllegalArgumentException() {
        // Type does not support immediate, but subscription requests immediate=true
        EmailType emailType = createEmailType("Daily Only", "desc", "user",
                false, false, true, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(emailType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Daily Only", "desc", "user",
                true, false, false, false, false, // immediate=true not supported
                false, false, true, false, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
        assertTrue(ex.getMessage().contains("Invalid subscription frequency"));
    }

    @Test
    void testFilterValidSubscriptions_UnsupportedHourlyFrequency_ThrowsIllegalArgumentException() {
        EmailType emailType = createEmailType("Immediate Only", "desc", "user",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(emailType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Immediate Only", "desc", "user",
                false, true, false, false, false, // hourly=true not supported
                true, false, false, false, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
        assertTrue(ex.getMessage().contains("Invalid subscription frequency"));
    }

    @Test
    void testFilterValidSubscriptions_UnsupportedDailyFrequency_ThrowsIllegalArgumentException() {
        EmailType emailType = createEmailType("Immediate Only", "desc", "user",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(emailType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Immediate Only", "desc", "user",
                false, false, true, false, false, // daily=true not supported
                true, false, false, false, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
        assertTrue(ex.getMessage().contains("Invalid subscription frequency"));
    }

    @Test
    void testFilterValidSubscriptions_UnsupportedWeeklyFrequency_ThrowsIllegalArgumentException() {
        EmailType emailType = createEmailType("Immediate Only", "desc", "user",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(emailType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Immediate Only", "desc", "user",
                false, false, false, true, false, // weekly=true not supported
                true, false, false, false, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
        assertTrue(ex.getMessage().contains("Invalid subscription frequency"));
    }

    @Test
    void testFilterValidSubscriptions_UnsupportedMonthlyFrequency_ThrowsIllegalArgumentException() {
        EmailType emailType = createEmailType("Immediate Only", "desc", "user",
                true, false, false, false, false);
        when(emailTypeRepository.findAll()).thenReturn(List.of(emailType));
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        UserEmailNotificationDto dto = new UserEmailNotificationDto(
                "Immediate Only", "desc", "user",
                false, false, false, false, true, // monthly=true not supported
                true, false, false, false, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of(dto)));
        assertTrue(ex.getMessage().contains("Invalid subscription frequency"));
    }

    @Test
    void testFilterValidSubscriptions_EmptySubscriptionList_ReturnsZero() {
        when(emailTypeRepository.findAll()).thenReturn(List.of());
        when(userEmailNotificationRepository.findNotificationsByUser(TEST_EMAIL)).thenReturn(List.of());

        emailService.updateEmailSubscriptions(TEST_EMAIL, false, false, List.of());
        verify(userEmailNotificationRepository, never()).saveAll(anyList());
        verify(userEmailNotificationRepository, never()).deleteAll(anyList());
    }
}