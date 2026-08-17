package us.dot.its.jpo.ode.api.emails.generators;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountCountsItem;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountRsuItem;

@Slf4j
@Component
public class MessageCountEmailGenerator extends AbstractEmailGenerator<MessageCountEmailContents> {

    public MessageCountEmailGenerator(TemplateEngine templateEngine,
            UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        super(templateEngine, unsubscribeTokenGenerator, emailProperties);
    }

    @Override
    public EmailContent generateEmailBody(MessageCountEmailContents data) {

        Context context = this.generateEmailContextBasic();
        context.setVariable("preview_text", "Message Counts from CV Manager");
        context.setVariable("footer_address", "CV-Manager Message Counts");
        context.setVariable("organizationName", data.getOrganizationName());
        context.setVariable("deploymentTitle", data.getDeploymentTitle());
        context.setVariable("startDate", data.getStartDate().toString());
        context.setVariable("endDate", data.getEndDate().toString());
        context.setVariable("messageTypes", data.getMessageTypeList());
        context.setVariable("messageCounts", data.getRsuCounts());

        String htmlContent = templateEngine.process("emails/email_template_message_counts", context);

        return new EmailContent(
                "CDOT-CV " + data.getDeploymentTitle() + " ODE Counts",
                htmlContent);
    }

    String getContent(MessageCountEmailContents data) {
        String content = String.format(
                "<p>This is an automated email reporting yesterday's J2735 message counts for messages processed by the ODE.</p>"
                        + "<p><strong>Organization:</strong> %s<br>"
                        + "<strong>Deployment:</strong> %s<br>"
                        + "<strong>Start Date:</strong> %s UTC<br>"
                        + "<strong>End Date:</strong> %s UTC</p>"
                        + "<p>Each cell in the table below shows: <strong>[Inbound Message Count] / [Outbound Message Count]</strong>.<br>"
                        + "Cells are <span style=\"background-color: #a4ffa1; color: #000; padding: 2px 8px; border-radius: 3px;\">green</span> if inbound and outbound counts are within 5%% of each other, and <span style=\"background-color: #ff7373; color: #000; padding: 2px 8px; border-radius: 3px;\">red</span> if the difference is greater than 5%%.</p>"
                        + "<p><strong>Inbound Message Count</strong>: Number of encoded messages received by the ODE from the load balancer.<br>"
                        + "<strong>Outbound Message Count</strong>: Number of decoded messages output by the ODE in JSON format, available for querying in MongoDB.<br>"
                        + "Ideally, these counts should match, but small differences can occur due to timing.<br>"
                        + "Note: Map and TIM outbound counts are deduplicated (one per hour), so these may be lower. The deviation calculation accounts for this.</p>"
                        + "<h3>RSU Message Counts</h3>"
                        + "<div style=\"margin: 16px 0; padding: 12px; background-color: #f5f5f5; border-radius: 4px; display: inline-block;\">"
                        + "<strong>Legend:</strong>&nbsp;&nbsp;"
                        + "<span style=\"background-color: #a4ffa1; color: #000; padding: 4px 12px; margin: 0 4px; border-radius: 3px;\">Green: ≤5%% deviation</span>"
                        + "<span style=\"background-color: #ff7373; color: #000; padding: 4px 12px; margin: 0 4px; border-radius: 3px;\">Red: >5%% deviation</span>"
                        + "</div>",
                escapeHtml(data.getOrganizationName()),
                escapeHtml(data.getDeploymentTitle()),
                escapeHtml(data.getStartDate().toString()),
                escapeHtml(data.getEndDate().toString()));
        String countsTable = generateCountTable(data);

        return String.format("%s %s", content, countsTable);
    }

    public static String generateTableHeader(List<String> messageTypeList) {
        StringBuilder html = new StringBuilder();
        html.append("<thead>\n")
                .append("<tr style=\"text-align: center;background-color: #b0dfff;\">\n")
                .append("<th style=\"padding: 12px;\">RSU</th>\n")
                .append("<th style=\"padding: 12px;\">Road</th>\n");

        for (String type : messageTypeList) {
            html.append("<th style=\"padding: 12px;\">").append(escapeHtml(type)).append("</th>\n");
        }

        html.append("</tr>\n</thead>\n");
        return html.toString();
    }

    public static String generateTableRow(
            String rsuIp,
            MessageCountRsuItem rsuCountsItem,
            String rowStyle,
            List<String> messageTypeList) {
        StringBuilder html = new StringBuilder();
        html.append("<tr style=\"").append(rowStyle).append("\">\n")
                .append("<td>").append(escapeHtml(rsuIp)).append("</td>\n")
                .append("<td>").append(escapeHtml(rsuCountsItem.getPrimaryRoute())).append("</td>\n");

        Map<String, MessageCountCountsItem> counts = rsuCountsItem.getMessageCountsByType();
        for (String type : messageTypeList) {
            MessageCountCountsItem countsItem = counts.get(type);
            html.append("<td style=\"background-color: ")
                    .append(diffToColor(countsItem.getDiffPercent()))
                    .append(";\">")
                    .append(countsItem.getIn()).append(" / ").append(countsItem.getOut()).append("</td>\n");
        }

        html.append("</tr>\n");
        return html.toString();
    }

    public static String generateCountTable(
            MessageCountEmailContents countsData) {
        if (countsData == null || countsData.getRsuCounts() == null || countsData.getRsuCounts().isEmpty()) {
            log.error("RSU dictionary is empty. Most likely an issue with PostgreSQL");
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<table class=\"dataframe\">\n")
                .append(generateTableHeader(countsData.getMessageTypeList()))
                .append("<tbody>\n");

        boolean styleSwitch = false;
        for (MessageCountRsuItem rsuItem : countsData.getRsuCounts()) {
            String rsuIp = rsuItem.getRsuIp();
            Map<String, MessageCountCountsItem> countEntry = rsuItem.getMessageCountsByType();
            String rowStyle = styleSwitch
                    ? "text-align: center;background-color: #f2f2f2;"
                    : "text-align: center;";
            styleSwitch = !styleSwitch;

            // Calculate differences between In and Out counts (%)
            for (String type : countEntry.keySet()) {
                MessageCountCountsItem countsItem = countEntry.get(type);
                int inCount = countsItem.getIn();
                int outCount = countsItem.getOut();

                double diffPercent;
                if (type.equalsIgnoreCase("bsm") || type.equalsIgnoreCase("tim")) {
                    // For unique deduplication situations, don't validate counts unless zero
                    // Assign the percentage difference between the in and out counts as pass or
                    // fail since no validation is occurring
                    // 6 being 6% and 0 being 0% difference. The 6% is enough to flag the table cell
                    // value
                    diffPercent = (inCount != 0 && outCount == 0) || (outCount > inCount) ? 6 : 0;
                } else {
                    // Normalize the diff_percent depending on message types that are deduplicated
                    // to 1/hour
                    int x = type.equalsIgnoreCase("map") ? 3600 : 1;
                    // Assign the calculated percentage difference between the in and out counts
                    if (inCount != 0) {
                        diffPercent = Math.abs(outCount / Math.ceil((double) inCount / x) - 1) * 100;
                    } else {
                        // If inCount is zero, assign 6% to trigger error coloring
                        diffPercent = outCount > inCount ? 6 : 0;
                    }
                }
                countsItem.setDiffPercent(diffPercent);
            }

            html.append(generateTableRow(rsuIp, rsuItem, rowStyle, countsData.getMessageTypeList()));
        }

        html.append("</tbody>\n</table>");
        return html.toString();
    }

    private static String diffToColor(Number val) {
        return val.doubleValue() > 5 ? "#ff7373" : "#a4ffa1";
    }
}