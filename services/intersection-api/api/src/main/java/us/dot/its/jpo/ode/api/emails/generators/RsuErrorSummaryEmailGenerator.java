package us.dot.its.jpo.ode.api.emails.generators;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;

@Component
public class RsuErrorSummaryEmailGenerator extends AbstractEmailGenerator<RsuErrorSummaryEmailContents> {

    public RsuErrorSummaryEmailGenerator(TemplateEngine templateEngine,
            UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        super(templateEngine, unsubscribeTokenGenerator, emailProperties);
    }

    @Override
    public EmailContent generateEmailBody(RsuErrorSummaryEmailContents data) {

        Context context = this.generateEmailContextBasic();
        context.setVariable("preview_text", "RSU Error Summary from CV Manager");
        context.setVariable("content_1", data.getMessage());
        context.setVariable("footer_address", "RSU Error Summary Report");
        context.setVariable("showUnsubscribeLink", false);
        // NOTE: Not escaping HTML in message because message is in HTML format

        String htmlContent = templateEngine.process("emails/email_template", context);

        return new EmailContent(
                data.getSubject(),
                htmlContent);
    }
}