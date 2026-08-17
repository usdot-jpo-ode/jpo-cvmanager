package us.dot.its.jpo.ode.api.emails.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import us.dot.its.jpo.ode.api.SnapshotTestUtils;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;

@ExtendWith(MockitoExtension.class)
class ApiErrorEmailGeneratorTest {

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private TemplateEngine templateEngine;

    private ApiErrorEmailGenerator apiErrorEmailGenerator;

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

        ApiErrorEmailGenerator snapshotGenerator = new ApiErrorEmailGenerator(springTemplateEngine,
                unsubscribeTokenGenerator,
                emailProperties);

        Instant timestamp = Instant.parse("2024-02-11T10:30:00Z");
        String logsLink = "https://cvmanager.com/logs";
        String errorMessage = """
                NullPointerException occurred

                And a message with 'quotes', \"double quotes\", and <html> tags & ampersands
                """;
        String stackTrace = """
                at com.example.Service.method(Service.java:42)
                at com.example.Controller.handle(Controller.java:27)""";

        ApiErrorEmailContents contents = new ApiErrorEmailContents(errorMessage, stackTrace, timestamp, logsLink);

        EmailContent result = snapshotGenerator.generateEmailBody(contents);

        String snapshotPath = "emails/api_error_email_snapshot.html";
        SnapshotTestUtils.assertMatchesSnapshot(result.getBody(), snapshotPath);
    }

    @Test
    void generateEmailBody_mockedTest() {
        apiErrorEmailGenerator = new ApiErrorEmailGenerator(
                templateEngine,
                unsubscribeTokenGenerator,
                emailProperties);
        apiErrorEmailGenerator = spy(apiErrorEmailGenerator);

        Context thymeLeafContext = mock(Context.class);
        Instant ts = Instant.parse("2024-02-11T10:30:00Z");
        ApiErrorEmailContents data = new ApiErrorEmailContents("Error message", "stack trace", ts,
                "https://cvmanager.com/logs");

        doCallRealMethod().when(apiErrorEmailGenerator).generateEmailBody(any());

        when(apiErrorEmailGenerator.generateEmailContextBasic()).thenReturn(thymeLeafContext);
        doNothing().when(thymeLeafContext).setVariable(anyString(), any());

        when(templateEngine.process("emails/email_template", thymeLeafContext)).thenReturn("HTML CONTENT");

        EmailContent result = apiErrorEmailGenerator.generateEmailBody(data);

        EmailContent expectedResult = new EmailContent("CV-Manager API Error", "HTML CONTENT");
        assertEquals(expectedResult, result);

        verify(thymeLeafContext, times(3)).setVariable(anyString(), any());
        verify(thymeLeafContext).setVariable("preview_text", "CV-Manager API Error");
        verify(thymeLeafContext).setVariable("content_1",
                "<p>A critical API Error has occurred at 2024-02-11T10:30:00Z. To View API error logs, navigate to <a href=\"https://cvmanager.com/logs\">API Error Logs</a><br><strong>Error message:</strong> Error message<br><strong>Stack Trace:</strong> stack trace</p>");
        verify(thymeLeafContext).setVariable("footer_address", "API Error Notification");
        verify(templateEngine).process("emails/email_template", thymeLeafContext);
    }
}