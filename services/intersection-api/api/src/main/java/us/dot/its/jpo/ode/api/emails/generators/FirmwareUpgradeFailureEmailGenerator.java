package us.dot.its.jpo.ode.api.emails.generators;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.FirmwareUpgradeFailureEmailContents;

@Component
public class FirmwareUpgradeFailureEmailGenerator extends AbstractEmailGenerator<FirmwareUpgradeFailureEmailContents> {

    public FirmwareUpgradeFailureEmailGenerator(TemplateEngine templateEngine,
            UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        super(templateEngine, unsubscribeTokenGenerator, emailProperties);
    }

    @Override
    public EmailContent generateEmailBody(FirmwareUpgradeFailureEmailContents data) {
        String stackTrace = data.getStackTrace();
        // Limit stack trace to 1000 characters to prevent excessively long emails
        if (stackTrace != null && stackTrace.length() > 1000) {
            stackTrace = stackTrace.substring(0, 1000) + "... (truncated)";
        }

        Context context = this.generateEmailContextBasic();
        context.setVariable("preview_text", "Firmware Upgrade Failure in CV Manager");
        context.setVariable("rsu_ip", escapeHtml(data.getRsuIp()));
        context.setVariable("failure_type", escapeHtml(data.getFailureType()));
        context.setVariable("error_message", escapeHtml(data.getMessage()));
        context.setVariable("stack_trace", escapeHtml(stackTrace));
        context.setVariable("footer_address", "CV-Manager Firmware Upgrade Failure");

        String htmlContent = templateEngine.process("emails/email_template_firmware_upgrade_failure", context);

        return new EmailContent(
                "CV-Manager Firmware Upgrade Failure",
                htmlContent);
    }
}