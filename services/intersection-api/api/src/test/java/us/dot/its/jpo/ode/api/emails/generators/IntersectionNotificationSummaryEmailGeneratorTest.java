package us.dot.its.jpo.ode.api.emails.generators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.SnapshotTestUtils;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntersectionNotificationSummaryEmailGeneratorTest {

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private TemplateEngine templateEngine;

    private IntersectionNotificationSummaryEmailGenerator intersectionNotificationSummaryEmailGenerator;

    @Test
    void testGenerateEmailBody_SnapshotTest() throws IOException {
        when(emailProperties.getCvmgrFrontEndUri()).thenReturn("https://cvmanager.com");

        // Configure the template resolver
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/"); // Path to your templates directory
        templateResolver.setSuffix(".html"); // Template file extension
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        // Configure the SpringTemplateEngine
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.setTemplateResolver(templateResolver);

        this.templateEngine = springTemplateEngine;

        IntersectionNotificationSummaryEmailGenerator snapshotGenerator = new IntersectionNotificationSummaryEmailGenerator(
                templateEngine, unsubscribeTokenGenerator,
                emailProperties);

        Notification notification = new ConnectionOfTravelNotification();
        notification.setIntersectionID(0);
        notification.setKey("connection-of-travel-notification");
        notification.setNotificationText("Test notification text with special characters: ' \" < > &");
        notification.setNotificationHeading("Test Notification Heading");
        notification.setNotificationGeneratedAt(1770830034000L); // 2025-02-11T10:30:34Z

        List<Notification> notifications = List.of(notification);

        IntersectionNotificationSummaryEmailContents contents = new IntersectionNotificationSummaryEmailContents(
                notifications);

        EmailContent result = snapshotGenerator.generateEmailBody(contents);

        String snapshotPath = "emails/intersection_notification_summary_email_snapshot.html";
        SnapshotTestUtils.assertMatchesSnapshot(result.getBody(), snapshotPath);
    }

    @Test
    void generateEmailBody_mockedTest() {
        intersectionNotificationSummaryEmailGenerator = new IntersectionNotificationSummaryEmailGenerator(
                templateEngine,
                unsubscribeTokenGenerator,
                emailProperties);
        intersectionNotificationSummaryEmailGenerator = spy(intersectionNotificationSummaryEmailGenerator);

        Context thymeLeafContext = mock(Context.class);

        Notification notification = new ConnectionOfTravelNotification();
        notification.setIntersectionID(0);
        notification.setKey("connection-of-travel-notification");
        notification.setNotificationText("Test notification text with special characters: ' \" < > &");
        notification.setNotificationHeading("Test Notification Heading");
        notification.setNotificationGeneratedAt(1770830034000L); // 2025-02-11T10:30:34Z

        List<Notification> notifications = List.of(notification);
        IntersectionNotificationSummaryEmailContents data = new IntersectionNotificationSummaryEmailContents(
                notifications);

        doCallRealMethod().when(intersectionNotificationSummaryEmailGenerator).generateEmailBody(any());

        when(intersectionNotificationSummaryEmailGenerator.generateEmailContextBasic()).thenReturn(thymeLeafContext);
        when(intersectionNotificationSummaryEmailGenerator.getEmailText(notifications))
                .thenReturn("notification content");
        doNothing().when(thymeLeafContext).setVariable(anyString(), any());

        when(templateEngine.process("emails/email_template", thymeLeafContext)).thenReturn("HTML CONTENT");

        EmailContent result = intersectionNotificationSummaryEmailGenerator.generateEmailBody(data);

        EmailContent expectedResult = new EmailContent("New CV-Manager Intersection Notifications", "HTML CONTENT");
        assertEquals(expectedResult, result);

        verify(thymeLeafContext, times(3)).setVariable(anyString(), any());
        verify(thymeLeafContext).setVariable("preview_text", "New Notifications in CV Manager");
        verify(thymeLeafContext).setVariable("content_1", "<p>notification content</p>");
        verify(thymeLeafContext).setVariable("footer_address", "CV-Manager Automated Notifications");
        verify(templateEngine).process("emails/email_template", thymeLeafContext);
    }
}