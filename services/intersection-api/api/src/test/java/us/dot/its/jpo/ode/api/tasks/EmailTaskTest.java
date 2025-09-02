package us.dot.its.jpo.ode.api.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.accessors.notifications.active_notification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.models.EmailFrequency;
import us.dot.its.jpo.ode.api.services.EmailService;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EmailTaskTest {

    private EmailService emailService;
    private ActiveNotificationRepository activeNotificationRepo;
    private EmailTask emailTask;

    private final int maximumResponseSize = 10;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        activeNotificationRepo = mock(ActiveNotificationRepository.class);
        emailTask = new EmailTask(emailService, activeNotificationRepo, maximumResponseSize);
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
    void testGetEmailHeadingFormat() {
        String heading = emailTask.getEmailHeading();
        assertThat(heading).startsWith("New Conflict Monitor Notifications: ");
    }

    @Test
    void testGetEmailTextFormat() {
        Notification n1 = createNotification("k1", "heading", "text", 1, 1234567890000L);
        List<Notification> notifications = Collections.singletonList(n1);
        String text = emailTask.getEmailText(notifications);

        assertThat(text).contains("Notification : heading");
        assertThat(text).contains("text");
        assertThat(text).contains("Intersection ID: 1");
        assertThat(text).contains("Generated At: ");
    }

    @Test
    void testSendAlwaysNotificationsFirstRunSetsLastAlwaysList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendAlwaysNotifications();

        // Should set lastAlwaysList and not send email
        verify(emailService, never()).emailList(anyList(), anyString(), anyString());
    }

    @Test
    void testSendAlwaysNotificationsSendsEmailOnNew() {
        Notification old1 = createNotification("k1", "h1", "t1", 1, 1000);
        Notification new1 = createNotification("k2", "h2", "t2", 2, 2000);
        List<Notification> oldList = Collections.singletonList(old1);
        List<Notification> newList = Arrays.asList(old1, new1);

        Page<Notification> page1 = new PageImpl<>(oldList, PageRequest.of(0, maximumResponseSize), oldList.size());
        Page<Notification> page2 = new PageImpl<>(newList, PageRequest.of(0, maximumResponseSize), newList.size());

        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize)))
                .thenReturn(page1) // first call
                .thenReturn(page2); // second call

        emailTask.sendAlwaysNotifications(); // sets lastAlwaysList
        List<UserRepresentation> recipients = Collections.singletonList(new UserRepresentation());
        when(emailService.getNotificationEmailList(EmailFrequency.ALWAYS)).thenReturn(recipients);

        emailTask.sendAlwaysNotifications(); // should send email

        verify(emailService).emailList(eq(recipients), anyString(), contains("Notification : h2"));
    }

    @Test
    void testSendHourlyNotificationsFirstRunSetsLastHourList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendHourlyNotifications();

        verify(emailService, never()).emailList(anyList(), anyString(), anyString());
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
        List<UserRepresentation> recipients = Collections.singletonList(new UserRepresentation());
        when(emailService.getNotificationEmailList(EmailFrequency.ALWAYS)).thenReturn(recipients);

        emailTask.sendHourlyNotifications();

        verify(emailService).emailList(eq(recipients), anyString(), contains("Notification : h2"));
    }

    @Test
    void testSendDailyNotificationsFirstRunSetsLastDayList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendDailyNotifications();

        verify(emailService, never()).emailList(anyList(), anyString(), anyString());
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
        List<UserRepresentation> recipients = Collections.singletonList(new UserRepresentation());
        when(emailService.getNotificationEmailList(EmailFrequency.ALWAYS)).thenReturn(recipients);

        emailTask.sendDailyNotifications();

        verify(emailService).emailList(eq(recipients), anyString(), contains("Notification : h2"));
    }

    @Test
    void testSendWeeklyNotificationsFirstRunSetsLastWeekList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendWeeklyNotifications();

        verify(emailService, never()).emailList(anyList(), anyString(), anyString());
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
        List<UserRepresentation> recipients = Collections.singletonList(new UserRepresentation());
        when(emailService.getNotificationEmailList(EmailFrequency.ALWAYS)).thenReturn(recipients);

        emailTask.sendWeeklyNotifications();

        verify(emailService).emailList(eq(recipients), anyString(), contains("Notification : h2"));
    }

    @Test
    void testSendMonthlyNotificationsFirstRunSetsLastMonthList() {
        Notification n1 = createNotification("k1", "h1", "t1", 1, 1000);
        List<Notification> notifications = Collections.singletonList(n1);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, maximumResponseSize),
                notifications.size());
        when(activeNotificationRepo.find(null, null, null, PageRequest.of(0, maximumResponseSize))).thenReturn(page);

        emailTask.sendMonthlyNotifications();

        verify(emailService, never()).emailList(anyList(), anyString(), anyString());
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
        List<UserRepresentation> recipients = Collections.singletonList(new UserRepresentation());
        when(emailService.getNotificationEmailList(EmailFrequency.ALWAYS)).thenReturn(recipients);

        emailTask.sendMonthlyNotifications();

        verify(emailService).emailList(eq(recipients), anyString(), contains("Notification : h2"));
    }
}