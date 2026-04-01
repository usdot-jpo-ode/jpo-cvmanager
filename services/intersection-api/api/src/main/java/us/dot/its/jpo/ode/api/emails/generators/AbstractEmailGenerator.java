package us.dot.its.jpo.ode.api.emails.generators;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;

/**
 * Abstract base class for generating email content from templates.
 * 
 * <p>
 * This class provides common functionality for all email generators, including:
 * <ul>
 * <li>Thymeleaf template processing</li>
 * <li>Unsubscribe token generation</li>
 * <li>Common email context variables (styling, buttons, footer)</li>
 * <li>Standardized date/time formatting in UTC</li>
 * </ul>
 * 
 * <p>
 * Subclasses must implement {@link #generateEmailBody(Object)} to provide
 * specific email generation logic for their notification type.
 * </p>
 *
 * @param <T> The type of data object required to generate the email content
 */
@Component
public abstract class AbstractEmailGenerator<T> {

    protected final TemplateEngine templateEngine;
    protected final UnsubscribeTokenGenerator unsubscribeTokenGenerator;
    protected final EmailProperties emailProperties;
    /**
     * Date/time formatter for consistent UTC timestamp formatting across all emails
     */
    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"));

    private static final String BACKGROUND_COLOR = "#f4f5f6";
    private static final String CONTENT_BACKGROUND_COLOR = "#fff";
    private static final String BORDER_COLOR = "#eaebed";
    private static final String FOOTER_FONT_COLOR = "#9a9ea6";
    private static final String BUTTON_COLOR = "#0867ec";
    private static final String BUTTON_HOVER_COLOR = "#ec8208ff";
    private static final String BUTTON_FONT_COLOR = "#fff";

    /**
     * Constructs an AbstractEmailGenerator with required dependencies.
     *
     * @param templateEngine            The Thymeleaf template engine for rendering
     *                                  email templates
     * @param unsubscribeTokenGenerator Generator for creating JWT-based unsubscribe
     *                                  tokens
     * @param emailProperties           Configuration properties for email settings
     */
    public AbstractEmailGenerator(TemplateEngine templateEngine, UnsubscribeTokenGenerator unsubscribeTokenGenerator,
            EmailProperties emailProperties) {
        this.templateEngine = templateEngine;
        this.unsubscribeTokenGenerator = unsubscribeTokenGenerator;
        this.emailProperties = emailProperties;
    }

    /**
     * Generates a basic Thymeleaf context with common email template variables.
     * 
     * <p>
     * This method populates the context with standardized variables including:
     * </p>
     * <ul>
     * <li><b>Content:</b> greeting, signature, action button text and links</li>
     * <li><b>Unsubscribe:</b> unsubscribe link text and placeholder URL</li>
     * <li><b>Styling:</b> background colors, button colors, hover effects, font
     * colors</li>
     * </ul>
     * 
     * <p>
     * Subclasses can extend this context by adding additional variables specific
     * to their email type. The unsubscribe_href contains a placeholder
     * "{{unsubscribe_url}}"
     * that should be replaced with the actual unsubscribe URL for each recipient.
     * </p>
     *
     * @return A Thymeleaf Context populated with common email variables
     */
    public Context generateEmailContextBasic() {

        Context context = new Context();
        context.setVariable("greeting", "Hello CV-Manager User,");
        context.setVariable("action_button_text", "Navigate to the CV-Manager");
        context.setVariable("action_button_href",
                String.format("%s", emailProperties.getCvmgrFrontEndUri()));
        context.setVariable("content_2",
                "If not actionable, please forward this request on to the relevant party.");
        context.setVariable("signature",
                "This was an automated email from the CV Manager. Please do not reply to this email.");
        context.setVariable("unsubscribe_pre_text", "If you no longer wish to receive these emails, please ");
        context.setVariable("unsubscribe_link_text", "Unsubscribe");
        context.setVariable("unsubscribe_href", "{{unsubscribe_url}}");
        context.setVariable("backgroundColor", BACKGROUND_COLOR);
        context.setVariable("contentBackgroundColor", BACKGROUND_COLOR);
        context.setVariable("tableMainBackgroundColor", CONTENT_BACKGROUND_COLOR);
        context.setVariable("tableMainBorderColor", BORDER_COLOR);
        context.setVariable("tableHoverColor", BUTTON_HOVER_COLOR);
        context.setVariable("tableButtonColor", BUTTON_COLOR);
        context.setVariable("btnColor", BUTTON_COLOR);
        context.setVariable("btnFontColor", BUTTON_FONT_COLOR);
        context.setVariable("btnHoverColor", BUTTON_HOVER_COLOR);
        context.setVariable("footerFontColor", FOOTER_FONT_COLOR);

        return context;
    }

    /**
     * Template method to generate the email body and complete email content.
     * 
     * <p>
     * Subclasses must implement this method to provide specific logic for their
     * notification type. Implementations should:
     * </p>
     * <ol>
     * <li>Extract necessary data from the input parameter</li>
     * <li>Create a Thymeleaf context (typically using
     * {@link #generateEmailContextBasic()})</li>
     * <li>Add email-specific variables to the context</li>
     * <li>Process the Thymeleaf template to generate HTML body</li>
     * <li>Return an EmailContent object with subject, body, and recipients</li>
     * </ol>
     *
     * @param data The data object containing all information needed to generate the
     *             email.
     *             The specific type depends on the concrete implementation.
     * @return EmailContent object containing the subject line, HTML body, and
     *         recipient list
     */
    public abstract EmailContent generateEmailBody(T data);
}