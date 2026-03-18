package us.dot.its.jpo.ode.api.emails.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Slf4j
class IntersectionNotificationSummaryEmailGeneratorTest {
    private TemplateEngine templateEngine;

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    private IntersectionNotificationSummaryEmailGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(emailProperties.getCvmgrFrontEndUri()).thenReturn("https://cvmanager.example.com");

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

        generator = new IntersectionNotificationSummaryEmailGenerator(templateEngine, unsubscribeTokenGenerator,
                emailProperties);
    }

    @Test
    void testGenerateEmailBody() {
        Notification notification = new ConnectionOfTravelNotification();
        List<Notification> notifications = Collections.singletonList(notification);

        IntersectionNotificationSummaryEmailContents data = new IntersectionNotificationSummaryEmailContents(
                notifications);

        EmailContent emailContent = generator.generateEmailBody(data);

        assertEquals("New CV-Manager Intersection Notifications",
                emailContent.getSubject());
        assertFalse(emailContent.getBody().isEmpty());
    }
}