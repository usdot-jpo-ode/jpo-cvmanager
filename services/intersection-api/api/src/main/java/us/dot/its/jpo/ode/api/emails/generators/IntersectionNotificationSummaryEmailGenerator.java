package us.dot.its.jpo.ode.api.emails.generators;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;

@Component
public class IntersectionNotificationSummaryEmailGenerator
        extends AbstractEmailGenerator<IntersectionNotificationSummaryEmailContents> {

    private final String EMAIL_TEMPLATE = "emails/email_template";

    public IntersectionNotificationSummaryEmailGenerator(TemplateEngine templateEngine,
            UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        super(templateEngine, unsubscribeTokenGenerator, emailProperties);
    }

    @Override
    public EmailContent generateEmailBody(IntersectionNotificationSummaryEmailContents data) {

        Context context = this.generateEmailContextBasic();
        context.setVariable("preview_text", "New Notifications in CV Manager");
        context.setVariable("content_1", String.format("<p>%s</p>", getEmailText(data.getNotifications())));
        context.setVariable("footer_address", "CV-Manager Automated Notifications");

        String htmlContent = templateEngine.process(EMAIL_TEMPLATE, context);

        return new EmailContent(
                "New CV-Manager Intersection Notifications",
                htmlContent);
    }

    public String getEmailText(List<Notification> notifications) {

        StringBuilder messageBody = new StringBuilder(
                "There are new Intersection Conflict-Monitor generated Notifications to review. Please review the Notifications below, or log into the CV-Manager to Analyze these notifications<br>");

        for (Notification notification : notifications) {
            messageBody.append("<br><strong>Heading:</strong> ")
                    .append(escapeHtml(notification.getNotificationHeading())).append("<br>");
            messageBody.append("<strong>Description:</strong> ").append(escapeHtml(notification.getNotificationText()))
                    .append("<br>");
            messageBody.append("<strong>Intersection ID:</strong> ")
                    .append(escapeHtml(String.valueOf(notification.getIntersectionID()))).append("<br>");
            messageBody.append("<strong>Generated At:</strong> ").append(escapeHtml(dateTimeFormatter.format(
                    Instant.ofEpochMilli(notification.getNotificationGeneratedAt())))).append("<br>");
        }

        return messageBody.toString();
    }
}