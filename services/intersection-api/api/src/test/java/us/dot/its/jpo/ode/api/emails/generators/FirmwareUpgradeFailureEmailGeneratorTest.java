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
import us.dot.its.jpo.ode.api.models.emails.contents.FirmwareUpgradeFailureEmailContents;

@ExtendWith(MockitoExtension.class)
class FirmwareUpgradeFailureEmailGeneratorTest {

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private TemplateEngine templateEngine;

    private FirmwareUpgradeFailureEmailGenerator firmwareUpgradeFailureEmailGenerator;

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

        FirmwareUpgradeFailureEmailGenerator snapshotGenerator = new FirmwareUpgradeFailureEmailGenerator(
                springTemplateEngine,
                unsubscribeTokenGenerator, emailProperties);

        FirmwareUpgradeFailureEmailContents contents = new FirmwareUpgradeFailureEmailContents();
        contents.setRsuIp("10.0.0.78");
        contents.setMessage("ConnectionError: SNMP timeout");
        contents.setFailureType("Firmware Upgrader");
        contents.setStackTrace(
                "Traceback (most recent call last):\n  File \"/usr/local/bin/rsu_firmware_upgrade.py\", line 23, in <module>");

        EmailContent result = snapshotGenerator.generateEmailBody(contents);

        String snapshotPath = "emails/firmware_upgrade_failure_snapshot.html";
        SnapshotTestUtils.assertMatchesSnapshot(result.getBody(), snapshotPath);
    }

    @Test
    void generateEmailBody_mockedTest() {
        firmwareUpgradeFailureEmailGenerator = new FirmwareUpgradeFailureEmailGenerator(
                templateEngine,
                unsubscribeTokenGenerator,
                emailProperties);
        firmwareUpgradeFailureEmailGenerator = spy(firmwareUpgradeFailureEmailGenerator);

        FirmwareUpgradeFailureEmailContents contents = new FirmwareUpgradeFailureEmailContents();
        contents.setRsuIp("10.0.0.78");
        contents.setMessage("ConnectionError: SNMP timeout");
        contents.setFailureType("Firmware Upgrader");
        contents.setStackTrace(
                "Traceback (most recent call last):\n  File \"/usr/local/bin/rsu_firmware_upgrade.py\", line 23, in <module>");

        Context thymeLeafContext = mock(Context.class);

        doCallRealMethod().when(firmwareUpgradeFailureEmailGenerator).generateEmailBody(any());

        when(firmwareUpgradeFailureEmailGenerator.generateEmailContextBasic()).thenReturn(thymeLeafContext);
        doNothing().when(thymeLeafContext).setVariable(anyString(), any());

        when(templateEngine.process("emails/email_template_firmware_upgrade_failure", thymeLeafContext))
                .thenReturn("HTML CONTENT");

        EmailContent result = firmwareUpgradeFailureEmailGenerator.generateEmailBody(contents);

        EmailContent expectedResult = new EmailContent("CV-Manager Firmware Upgrade Failure", "HTML CONTENT");
        assertEquals(expectedResult, result);

        verify(thymeLeafContext, times(6)).setVariable(anyString(), any());
        verify(thymeLeafContext).setVariable("preview_text", "Firmware Upgrade Failure in CV Manager");
        verify(thymeLeafContext).setVariable("footer_address", "CV-Manager Firmware Upgrade Failure");
        verify(thymeLeafContext).setVariable("rsu_ip", "10.0.0.78");
        verify(thymeLeafContext).setVariable("failure_type", "Firmware Upgrader");
        verify(thymeLeafContext).setVariable("error_message", "ConnectionError: SNMP timeout");
        verify(thymeLeafContext).setVariable("stack_trace",
                "Traceback (most recent call last):<br>  File &quot;/usr/local/bin/rsu_firmware_upgrade.py&quot;, line 23, in &lt;module&gt;");
        verify(templateEngine).process("emails/email_template_firmware_upgrade_failure", thymeLeafContext);
    }
}