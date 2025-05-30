package us.dot.its.jpo.ode.api.tasks;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.models.EmailFrequency;
import us.dot.its.jpo.ode.api.services.EmailService;

@Component
@ConditionalOnProperty(name = "enable.email", havingValue = "true", matchIfMissing = false)
public class EmailTask {

    private static final Logger log = LoggerFactory.getLogger(EmailTask.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int NOTIFICATION_EMAIL_RATE_MILLISECONDS = 10 * 1000; // 10 seconds
    private static final int HOURLY_NOTIFICATION_EMAIL_RATE_MILLISECONDS = 60 * 60 * 1000; // 1 hour
    private static final String DAILY_NOTIFICATION_CRON = "0 0 0 * * ?"; // every day at midnight
    private static final String WEEKLY_NOTIFICATION_CRON = "0 0 0 * * 0"; // every sunday at midnight
    private static final String MONTHLY_NOTIFICATION_CRON = "0 0 0 1 * ?"; // first day of the month at midnight

    private final EmailService email;
    private final ActiveNotificationRepository activeNotificationRepo;

    private List<Notification> lastAlwaysList;
    private List<Notification> lastHourList;
    private List<Notification> lastDayList;
    private List<Notification> lastWeekList;
    private List<Notification> lastMonthList;

    private final int maximumResponseSize;

    public EmailTask(EmailService email, ActiveNotificationRepository activeNotificationRepo,
            @Value("${maximumResponseSize}") int maximumResponseSize) {
        this.email = email;
        this.activeNotificationRepo = activeNotificationRepo;
        this.maximumResponseSize = maximumResponseSize;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"));

    @Scheduled(fixedRate = NOTIFICATION_EMAIL_RATE_MILLISECONDS)
    public void sendAlwaysNotifications() {
        log.info("Checking Always Notifications: {}", dateFormat.format(new Date()));
        if (lastAlwaysList == null) {
            lastAlwaysList = getActiveNotifications();
            return;
        }

        List<Notification> currentNotifications = getActiveNotifications();

        List<Notification> newNotifications = getNewNotifications(currentNotifications, lastAlwaysList);

        lastAlwaysList = currentNotifications;

        if (!newNotifications.isEmpty()) {
            List<UserRepresentation> recipients = email.getNotificationEmailList(EmailFrequency.ALWAYS);
            email.emailList(recipients, getEmailHeading(), getEmailText(newNotifications));
        }
    }

    @Scheduled(fixedRate = HOURLY_NOTIFICATION_EMAIL_RATE_MILLISECONDS)
    public void sendHourlyNotifications() {
        log.info("Checking Hourly Notifications: {}", dateFormat.format(new Date()));
        if (lastHourList == null) {
            lastHourList = getActiveNotifications();
            return;
        }

        List<Notification> currentNotifications = getActiveNotifications();

        List<Notification> newNotifications = getNewNotifications(currentNotifications, lastHourList);

        lastHourList = currentNotifications;

        if (!newNotifications.isEmpty()) {
            List<UserRepresentation> recipients = email.getNotificationEmailList(EmailFrequency.ALWAYS);
            email.emailList(recipients, getEmailHeading(), getEmailText(newNotifications));
        }
    }

    @Scheduled(cron = DAILY_NOTIFICATION_CRON)
    public void sendDailyNotifications() {
        log.info("Checking Daily Notifications: {}", dateFormat.format(new Date()));
        if (lastDayList == null) {
            lastDayList = getActiveNotifications();
            return;
        }

        List<Notification> currentNotifications = getActiveNotifications();

        List<Notification> newNotifications = getNewNotifications(currentNotifications, lastDayList);

        lastDayList = currentNotifications;

        if (!newNotifications.isEmpty()) {
            List<UserRepresentation> recipients = email.getNotificationEmailList(EmailFrequency.ALWAYS);
            email.emailList(recipients, getEmailHeading(), getEmailText(newNotifications));
        }
    }

    @Scheduled(cron = WEEKLY_NOTIFICATION_CRON)
    public void sendWeeklyNotifications() {
        log.info("Checking Weekly Notifications: {}", dateFormat.format(new Date()));
        if (lastWeekList == null) {
            lastWeekList = getActiveNotifications();
            return;
        }

        List<Notification> currentNotifications = getActiveNotifications();

        List<Notification> newNotifications = getNewNotifications(currentNotifications, lastWeekList);

        lastWeekList = currentNotifications;

        if (!newNotifications.isEmpty()) {
            List<UserRepresentation> recipients = email.getNotificationEmailList(EmailFrequency.ALWAYS);
            email.emailList(recipients, getEmailHeading(), getEmailText(newNotifications));
        }
    }

    @Scheduled(cron = MONTHLY_NOTIFICATION_CRON)
    public void sendMonthlyNotifications() {
        log.info("Checking Monthly Notifications: {}", dateFormat.format(new Date()));
        if (lastMonthList == null) {
            lastMonthList = getActiveNotifications();
            return;
        }

        List<Notification> currentNotifications = getActiveNotifications();

        List<Notification> newNotifications = getNewNotifications(currentNotifications, lastMonthList);

        lastMonthList = currentNotifications;

        if (!newNotifications.isEmpty()) {
            List<UserRepresentation> recipients = email.getNotificationEmailList(EmailFrequency.ALWAYS);
            email.emailList(recipients, getEmailHeading(), getEmailText(newNotifications));
        }

    }

    public List<Notification> getActiveNotifications() {
        Page<Notification> notifications = activeNotificationRepo
                .find(null, null, null, PageRequest.of(0, maximumResponseSize));
        return notifications.getContent();
    }

    public List<Notification> getNewNotifications(List<Notification> newList, List<Notification> oldList) {

        List<Notification> newNotifications = new ArrayList<>();

        for (Notification newNotification : newList) {
            boolean found = false;
            for (Notification oldNotification : oldList) {
                if (newNotification.key.equals(oldNotification.key)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newNotifications.add(newNotification);
            }
        }
        return newNotifications;
    }

    public String getEmailHeading() {
        return "New Conflict Monitor Notifications: " + formatter.format(Instant.now());
    }

    public String getEmailText(List<Notification> notifications) {

        String messageBody = "There are new Notifications to review in the conflict monitor application. Please review the Notifications below, or log into the Conflict Visualizer to Analyze these notifications";

        for (Notification notification : notifications) {
            messageBody += "\n\nNotification : " + notification.getNotificationHeading() + "\n";
            messageBody += "\t" + notification.getNotificationText() + "\n";
            messageBody += "\tIntersection ID: " + notification.getIntersectionID() + "\n";
            messageBody += "\tGenerated At: "
                    + formatter.format(Instant.ofEpochMilli(notification.getNotificationGeneratedAt())) + "\n";
        }

        return messageBody;
    }

}