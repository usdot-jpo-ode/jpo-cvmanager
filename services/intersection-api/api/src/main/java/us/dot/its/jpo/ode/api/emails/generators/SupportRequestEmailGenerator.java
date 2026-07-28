package us.dot.its.jpo.ode.api.emails.generators;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;

@Component
public class SupportRequestEmailGenerator extends AbstractEmailGenerator<SupportRequestEmailContents> {

    public SupportRequestEmailGenerator(TemplateEngine templateEngine,
            UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        super(templateEngine, unsubscribeTokenGenerator, emailProperties);
    }

    @Override
    public EmailContent generateEmailBody(SupportRequestEmailContents data) {

        Context context = this.generateEmailContextBasic();
        context.setVariable("preview_text", "New Support Request in CV Manager");
        context.setVariable("content_1",
                String.format("<p>New support request from %s:<br><br>%s</p>",
                        escapeHtml(data.getEmail()),
                        escapeHtml(data.getMessage())));
        context.setVariable("footer_address", "CV-Manager User-Submitted Support Request");

        String htmlContent = templateEngine.process("emails/email_template", context);

        return new EmailContent(
                data.getSubject(),
                htmlContent);
    }
}