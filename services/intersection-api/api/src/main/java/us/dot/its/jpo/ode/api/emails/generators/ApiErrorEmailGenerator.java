package us.dot.its.jpo.ode.api.emails.generators;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;

@Component
public class ApiErrorEmailGenerator extends AbstractEmailGenerator<ApiErrorEmailContents> {

    public ApiErrorEmailGenerator(TemplateEngine templateEngine,
            UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        super(templateEngine, unsubscribeTokenGenerator, emailProperties);
    }

    @Override
    public EmailContent generateEmailBody(ApiErrorEmailContents data) {

        Context context = this.generateEmailContextBasic();
        context.setVariable("preview_text", "CV-Manager API Error");
        context.setVariable("content_1", String.format(
                "<p>A critical API Error has occurred at %s. To View API error logs, navigate to <a href=\"%s\">API Error Logs</a><br><strong>Error message:</strong> %s<br><strong>Stack Trace:</strong> %s</p>",
                escapeHtml(data.getTimestamp().toString()),
                escapeHtml(data.getLogsLink()),
                escapeHtml(data.getErrorMessage()),
                escapeHtml(data.getStackTrace())));
        context.setVariable("footer_address", "API Error Notification");

        String htmlContent = templateEngine.process("emails/email_template", context);

        return new EmailContent(
                "CV-Manager API Error",
                htmlContent);
    }
}