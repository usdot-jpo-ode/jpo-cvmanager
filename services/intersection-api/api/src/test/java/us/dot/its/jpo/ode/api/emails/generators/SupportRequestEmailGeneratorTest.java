package us.dot.its.jpo.ode.api.emails.generators;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

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
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;

@ExtendWith(MockitoExtension.class)
class SupportRequestEmailGeneratorTest {

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private TemplateEngine templateEngine;

    private SupportRequestEmailGenerator supportRequestEmailGenerator;

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

        SupportRequestEmailGenerator snapshotGenerator = new SupportRequestEmailGenerator(springTemplateEngine,
                unsubscribeTokenGenerator, emailProperties);

        String subject = "Support Request: System Issues";
        String email = "admin@example.com";
        String message = """
                I'm experiencing multiple issues:

                1. Cannot access dashboard
                2. Reports are not loading
                3. Email notifications not working

                Please investigate as soon as possible.

                And a message with 'quotes', \"double quotes\", and <html> tags & ampersands
                """;

        SupportRequestEmailContents contents = new SupportRequestEmailContents(email, subject, message);

        EmailContent result = snapshotGenerator.generateEmailBody(contents);

        String snapshotPath = "emails/support_request_email_multiline_snapshot.html";
        SnapshotTestUtils.assertMatchesSnapshot(result.getBody(), snapshotPath);
    }

    @Test
    void generateEmailBody_mockedTest() {
        supportRequestEmailGenerator = new SupportRequestEmailGenerator(
                templateEngine,
                unsubscribeTokenGenerator,
                emailProperties);
        supportRequestEmailGenerator = spy(supportRequestEmailGenerator);

        Context thymeLeafContext = mock(Context.class);
        SupportRequestEmailContents data = new SupportRequestEmailContents("admin@example.com", "subject", "message");

        doCallRealMethod().when(supportRequestEmailGenerator).generateEmailBody(any());

        when(supportRequestEmailGenerator.generateEmailContextBasic()).thenReturn(thymeLeafContext);
        doNothing().when(thymeLeafContext).setVariable(anyString(), any());

        when(templateEngine.process("emails/email_template", thymeLeafContext)).thenReturn("HTML CONTENT");

        EmailContent result = supportRequestEmailGenerator.generateEmailBody(data);

        EmailContent expectedResult = new EmailContent("subject", "HTML CONTENT");
        assertEquals(expectedResult, result);

        verify(thymeLeafContext, times(3)).setVariable(anyString(), any());
        verify(thymeLeafContext).setVariable("preview_text", "New Support Request in CV Manager");
        verify(thymeLeafContext).setVariable("content_1",
                "<p>New support request from admin@example.com:<br><br>message</p>");
        verify(thymeLeafContext).setVariable("footer_address", "CV-Manager User-Submitted Support Request");
        verify(templateEngine).process("emails/email_template", thymeLeafContext);
    }
}