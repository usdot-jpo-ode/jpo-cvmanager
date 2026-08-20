package us.dot.its.jpo.ode.api.emails.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
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
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;

@ExtendWith(MockitoExtension.class)
class RsuErrorSummaryEmailGeneratorTest {

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private TemplateEngine templateEngine;

    private RsuErrorSummaryEmailGenerator rsuErrorSummaryEmailGenerator;

    @BeforeEach
    void setUp() {
    }

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

        RsuErrorSummaryEmailGenerator snapshotGenerator = new RsuErrorSummaryEmailGenerator(springTemplateEngine,
                unsubscribeTokenGenerator, emailProperties);

        String subject = "RSU Error Summary for RSU 192.168.1.1";
        String message = """
                Summary of RSU errors:\n- RSU 192.168.1.1: Connection timeout\n- RSU 192.168.1.2: Authentication failed

                And a message with 'quotes', \"double quotes\", and <html> tags & ampersands
                """;

        RsuErrorSummaryEmailContents contents = new RsuErrorSummaryEmailContents(subject, message);

        EmailContent result = snapshotGenerator.generateEmailBody(contents);

        String snapshotPath = "emails/rsu_error_summary_email_snapshot.html";
        SnapshotTestUtils.assertMatchesSnapshot(result.getBody(), snapshotPath);
    }

    @Test
    void generateEmailBody_mockedTest() {
        rsuErrorSummaryEmailGenerator = new RsuErrorSummaryEmailGenerator(
                templateEngine,
                unsubscribeTokenGenerator,
                emailProperties);
        rsuErrorSummaryEmailGenerator = spy(rsuErrorSummaryEmailGenerator);

        Context thymeLeafContext = mock(Context.class);
        RsuErrorSummaryEmailContents data = new RsuErrorSummaryEmailContents("subject", "message");

        doCallRealMethod().when(rsuErrorSummaryEmailGenerator).generateEmailBody(any());

        when(rsuErrorSummaryEmailGenerator.generateEmailContextBasic()).thenReturn(thymeLeafContext);
        doNothing().when(thymeLeafContext).setVariable(anyString(), any());

        when(templateEngine.process("emails/email_template", thymeLeafContext)).thenReturn("HTML CONTENT");

        EmailContent result = rsuErrorSummaryEmailGenerator.generateEmailBody(data);

        EmailContent expectedResult = new EmailContent("subject", "HTML CONTENT");
        assertEquals(expectedResult, result);

        verify(thymeLeafContext, times(4)).setVariable(anyString(), any());
        verify(thymeLeafContext).setVariable("preview_text", "RSU Error Summary from CV Manager");
        verify(thymeLeafContext).setVariable("content_1", "message");
        verify(thymeLeafContext).setVariable("footer_address", "RSU Error Summary Report");
        verify(thymeLeafContext).setVariable("showUnsubscribeLink", false);
        verify(templateEngine).process("emails/email_template", thymeLeafContext);
    }
}