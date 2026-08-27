package us.dot.its.jpo.ode.api.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.accessors.notifications.active_notification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.emails.generators.IntersectionNotificationSummaryEmailGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailCategory;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.EmailFrequency;
import us.dot.its.jpo.ode.api.models.emails.EmailRecipient;
import us.dot.its.jpo.ode.api.services.EmailService;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EmailTaskTest {

    private IntersectionNotificationSummaryEmailGenerator emailGenerator;
    private EmailService emailService;
    private ActiveNotificationRepository activeNotificationRepo;
    private EmailTask emailTask;

    private final int maximumResponseSize = 10;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        activeNotificationRepo = mock(ActiveNotificationRepository.class);
        emailGenerator = mock(IntersectionNotificationSummaryEmailGenerator.class);
        emailTask = new EmailTask(emailService, activeNotificationRepo, maximumResponseSize, emailGenerator);
    }

    Notification createNotification(String key, String heading, String text, int intersectionId, long generatedAt) {
        Notification n = new ConnectionOfTravelNotification();
        n.key = key;
        n.setNotificationHeading(heading);
        n.setNotificationText(text);
        n.setIntersectionID(intersectionId);
        n.setNotificationGeneratedAt(generatedAt);
        return n;
    }

    @Test
    void testGetActiveNotificationsReturnsContent() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, Instant.now().toEpochMilli());
        Notification n2 = createNotification("k2", "h2", "t2", 2, Instant.now().toEpochMilli());
        List<Notification> notifications = Arrays.asList(n1, n2);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        List<Notification> result = emailTask.getActiveNotifications();
        assertThat(result).containsExactly(n1, n2);
    }

    @Test
    void testGetNewNotificationsFindsNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        List<Notification> result = emailTask.getNewNotifications(newList, oldList);
        assertThat(result).containsExactly(new1);
    }

    @Test
    void testGetNewNotificationsNoneNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Collections.singletonList(old1);

        List<Notification> result = emailTask.getNewNotifications(newList, oldList);
        assertThat(result).isEmpty();
    }

    @Test
    void testSendHourlyNotificationsFirstRunSetsLastHourList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendHourlyNotifications();

        // Should set lastHourList and not send email
        verify(emailService, never()).sendEmails(anyList(), any());
    }

    @Test
    void testSendHourlyNotificationsSendsEmailOnNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        Page<Notification> page1 = new PageImpl<>(oldList, PageRequest.of(0, maximumResponseSize), oldList.size());
        Page<Notification> page2 = new PageImpl<>(newList, PageRequest.of(0, maximumResponseSize), newList.size());

        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize)))
                .thenReturn(page1)
                .thenReturn(page2);

        emailTask.sendHourlyNotifications();

        List<EmailRecipient> recipients = List.of(new EmailRecipient("email", "name"));
        when(emailService.getUsersForNotificationType(EmailCategory.INTERSECTION_NOTIFICATION_SUMMARY,
                EmailFrequency.ONCE_PER_HOUR)).thenReturn(recipients);

        EmailContent content = new EmailContent("subject", "body");
        when(emailGenerator.generateEmailBody(any())).thenReturn(content);

        emailTask.sendHourlyNotifications();

        verify(emailService).sendEmails(eq(recipients), eq(content));
    }

    @Test
    void testDoNotSendHourlyNotificationsWhenNoRecipients() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        Page<Notification> page1 = new PageImpl<>(oldList, PageRequest.of(0, maximumResponseSize), oldList.size());
        Page<Notification> page2 = new PageImpl<>(newList, PageRequest.of(0, maximumResponseSize), newList.size());

        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize)))
                .thenReturn(page1)
                .thenReturn(page2);

        emailTask.sendHourlyNotifications();

        List<EmailRecipient> recipients = Collections.emptyList();
        when(emailService.getUsersForNotificationType(EmailCategory.INTERSECTION_NOTIFICATION_SUMMARY,
                EmailFrequency.ONCE_PER_HOUR)).thenReturn(recipients);

        EmailContent content = new EmailContent("subject", "body");
        when(emailGenerator.generateEmailBody(any())).thenReturn(content);

        emailTask.sendHourlyNotifications();

        verify(emailService, never()).sendEmails(anyList(), any(EmailContent.class));
    }


    @Test
    void testSendDailyNotificationsFirstRunSetsLastDayList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendDailyNotifications();

        verify(emailService, never()).sendEmails(anyList(), any());
    }

    @Test
    void testSendDailyNotificationsSendsEmailOnNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        Page<Notification> page1 = new PageImpl<>(oldList, PageRequest.of(0, maximumResponseSize), oldList.size());
        Page<Notification> page2 = new PageImpl<>(newList, PageRequest.of(0, maximumResponseSize), newList.size());

        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize)))
                .thenReturn(page1)
                .thenReturn(page2);

        emailTask.sendDailyNotifications();

        List<EmailRecipient> recipients = List.of(new EmailRecipient("email", "name"));
        when(emailService.getUsersForNotificationType(EmailCategory.INTERSECTION_NOTIFICATION_SUMMARY,
                EmailFrequency.ONCE_PER_DAY)).thenReturn(recipients);

        EmailContent content = new EmailContent("subject", "body");
        when(emailGenerator.generateEmailBody(any())).thenReturn(content);

        emailTask.sendDailyNotifications();

        verify(emailService).sendEmails(eq(recipients), eq(content));
    }

    @Test
    void testSendWeeklyNotificationsFirstRunSetsLastWeekList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendWeeklyNotifications();

        verify(emailService, never()).sendEmails(anyList(), any());
    }

    @Test
    void testSendWeeklyNotificationsSendsEmailOnNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        Page<Notification> page1 = new PageImpl<>(oldList, PageRequest.of(0, maximumResponseSize), oldList.size());
        Page<Notification> page2 = new PageImpl<>(newList, PageRequest.of(0, maximumResponseSize), newList.size());

        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize)))
                .thenReturn(page1)
                .thenReturn(page2);

        emailTask.sendWeeklyNotifications();

        List<EmailRecipient> recipients = List.of(new EmailRecipient("email", "name"));
        when(emailService.getUsersForNotificationType(EmailCategory.INTERSECTION_NOTIFICATION_SUMMARY,
                EmailFrequency.ONCE_PER_WEEK)).thenReturn(recipients);

        EmailContent content = new EmailContent("subject", "body");
        when(emailGenerator.generateEmailBody(any())).thenReturn(content);

        emailTask.sendWeeklyNotifications();

        verify(emailService).sendEmails(eq(recipients), eq(content));
    }

    @Test
    void testSendMonthlyNotificationsFirstRunSetsLastMonthList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendMonthlyNotifications();

        verify(emailService, never()).sendEmails(anyList(), any());
    }

    @Test
    void testSendMonthlyNotificationsSendsEmailOnNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        Page<Notification> page1 = new PageImpl<>(oldList, PageRequest.of(0, maximumResponseSize), oldList.size());
        Page<Notification> page2 = new PageImpl<>(newList, PageRequest.of(0, maximumResponseSize), newList.size());

        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize)))
                .thenReturn(page1)
                .thenReturn(page2);

        emailTask.sendMonthlyNotifications();

        List<EmailRecipient> recipients = List.of(new EmailRecipient("email", "name"));
        when(emailService.getUsersForNotificationType(EmailCategory.INTERSECTION_NOTIFICATION_SUMMARY,
                EmailFrequency.ONCE_PER_MONTH)).thenReturn(recipients);

        EmailContent content = new EmailContent("subject", "body");
        when(emailGenerator.generateEmailBody(any())).thenReturn(content);

        emailTask.sendMonthlyNotifications();

        verify(emailService).sendEmails(eq(recipients), eq(content));
    }
}